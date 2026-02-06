package com.example.demo.service.impl;

import com.example.demo.exception.WeeklyLimitExceededException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Department;
import com.example.demo.entity.ProfileRequest;
import com.example.demo.entity.Request;
import com.example.demo.entity.User;
import com.example.demo.entity.UserGroup; // ★追加
import com.example.demo.repository.DepartmentRepository; // ★追加
import com.example.demo.repository.ProfileRequestRepository;
import com.example.demo.repository.RequestRepository;
import com.example.demo.repository.UserGroupRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.RequestDetailDto;
import com.example.demo.service.UserDetailDto;

@Service
public class ApplicationServiceImpl implements ApplicationService {

	private final RequestRepository requestRepository;
	private final UserRepository userRepository;
	private final UserGroupRepository groupRepository;
	private final DepartmentRepository departmentRepository; // ★追加

	@Autowired
	private ProfileRequestRepository profileRequestRepository;

	public ApplicationServiceImpl(RequestRepository requestRepository, UserRepository userRepository,
			UserGroupRepository groupRepository, DepartmentRepository departmentRepository) { // ★引数を追加
		this.requestRepository = requestRepository;
		this.userRepository = userRepository;
		this.groupRepository = groupRepository;
		this.departmentRepository = departmentRepository; // ★初期化
	}

	// ====================================================================
	// ユーザー情報取得 (Finders)
	// ====================================================================

	@Override
	public Optional<UserDetailDto> findUserDetail(String userId) {
		return userRepository.findByUserId(userId)
				.map(this::mapEntityToUserDetailDto);
	}

	@Override
	public UserDetailDto findUserDetailByUserId(String userId) {
		return userRepository.findByUserId(userId)
				.map(this::mapEntityToUserDetailDto)
				.orElseThrow(() -> new RuntimeException("ユーザー詳細が見つかりません: " + userId));
	}

	@Override
	public List<UserDetailDto> printAllUsers() {
		return userRepository.findAll().stream()
				.map(this::mapEntityToUserDetailDto)
				.collect(Collectors.toList());
	}

	// ApplicationServiceImpl.java

	@Autowired
	private PasswordEncoder passwordEncoder; // SecurityConfigで定義したやつ

	@Override
	public boolean verifyUser(String userId, String password) {
		return userRepository.findByUserId(userId)
				.map(user -> passwordEncoder.matches(password, user.getPassword()))
				.orElse(false);
	}

	@Override
	@Transactional
	public void updateUserEmail(String userId, String email) {
		userRepository.findByUserId(userId).ifPresent(user -> {
			user.setEmail(email);
			userRepository.save(user);
		});
	}

	// --------------------------------------------------------------------
	// ページネーション対応
	// --------------------------------------------------------------------

	@Override
	public Page<UserDetailDto> findAllUsersPaginated(Pageable pageable) {
		// フィルタがない場合の全件取得は、フィルタなしの検索メソッドに委譲
		return this.searchUsersPaginated(null, null, null);
	}

	// 検索メソッド (ページネーション対応 - フィルタなしの既存メソッドをオーバーライド)
	// ApplicationServiceImpl.java
	// ApplicationServiceImpl.java

	@Override
	public Page<UserDetailDto> searchUsersPaginated(String search, List<Integer> groupIds, Pageable pageable) {

		// Spring Data JPAの仕様上、空のリストを渡すとエラーになることがあるため、
		// 空リストやnullの場合はnullを渡してRepositoryの「IS NULL」判定を動かします。
		List<Integer> filterIds = (groupIds != null && !groupIds.isEmpty()) ? groupIds : null;

		// Repositoryの新しいメソッドを呼び出す
		Page<User> userEntityPage = userRepository.findBySearchAndFilters(search, filterIds, pageable);

		// EntityのページをDTOのページに変換して返す
		return userEntityPage.map(this::mapEntityToUserDetailDto);
	}

	@Override
	public List<String> getAllDepartmentNames() {
		// ... (省略: 既存のロジックを維持)
		return userRepository.findAll().stream()
				.map(User::getGroupId)
				.filter(id -> id != null)
				.distinct()
				.map(Object::toString)
				.sorted()
				.collect(Collectors.toList());
	}

	@Override
	public List<Department> findAllDepartments() {
		// Departmentテーブルから重複なく部署情報を取得
		return departmentRepository.findAllDepartmentsOrderedById();
	}

	// 【★既存★】グループのリストを取得 (グループ絞り込み用)
	@Override
	public List<UserGroup> findAllGroups() {
		List<UserGroup> groups = groupRepository.findAll();
		// 部署名でソートしておくと、プルダウンがきれいになります
		groups.sort(Comparator.comparing(g -> g.getDepartment().getName()));
		return groups;
	}

	// ====================================================================
	// 申請機能
	// ====================================================================

	@Override
	@Transactional
	public Request createNewRequest(Request request, boolean ignoreLimit) {
		// 1. 基本チェック
		if (request.getStartDate() != null && request.getStartDate().isBefore(LocalDate.now())) {
			throw new RuntimeException("過去の日付で申請することはできません。");
		}
		if (request.getStartDate() != null) {
			request.setEndDate(request.getStartDate());

			// 当日申請チェック：開始日が今日なら強制的に特認申請(spApply=true)にする
			if (request.getStartDate().isEqual(LocalDate.now())) {
				request.setSpApply(true);
			}
		}

		// 2. 回数制限チェック (週の月曜日〜日曜日でカウント)
		if (request.getUserId() != null && request.getStartDate() != null) {
			User user = userRepository.findByUserId(request.getUserId()).orElse(null);
			if (user != null) {
				// 勤続年数の計算 (入社日から申請開始日まで)
				long years = 3; // デフォルトは制限を緩く(3年)
				if (user.getJoiningDate() != null) {
					years = ChronoUnit.YEARS.between(user.getJoiningDate(), request.getStartDate());
				}

				// 制限回数の決定
				int limit = (years >= 3) ? 2 : 1;

				// 該当週（月〜日）の範囲を計算
				LocalDate startOfWeek = request.getStartDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
				LocalDate endOfWeek = startOfWeek.plusDays(6);

				// 既にある申請（却下以外）を取得して重みを計算
				List<Request> existingRequests = requestRepository.findByUserIdAndStartDateBetweenAndApplyNot(
						request.getUserId(), startOfWeek, endOfWeek, 2);

				double currentWeight = existingRequests.stream()
						.mapToDouble(r -> (r.getHalfDay() != null && r.getHalfDay()) ? 0.5 : 1.0)
						.sum();

				// 今回の申請の重み
				double newWeight = (request.getHalfDay() != null && request.getHalfDay()) ? 0.5 : 1.0;

				if (!ignoreLimit && currentWeight + newWeight > (double) limit) {
					String tenureStr = (years >= 3) ? "3年以上" : "3年未満";
					throw new WeeklyLimitExceededException(
							String.format("在宅勤務の週上限に達しています。 (勤続%s: 週%d回まで / 現在の申請数: %.1f)",
									tenureStr, limit, currentWeight));
				}
			}
		}

		return requestRepository.save(request);
	}

	@Override
	public List<RequestDetailDto> findMyRequestsWithNames(String userId) {
		List<Request> requests = requestRepository.findByUserId(userId);

		return requests.stream()
				.map(this::mapEntityToRequestDetailDto)
				.sorted(Comparator.comparing(RequestDetailDto::getStartDate).reversed())
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void cancelRequest(Long requestId, String userId) {
		if (requestId == null) {
			throw new IllegalArgumentException("申請IDが指定されていません。");
		}
		Request request = requestRepository.findById(requestId)
				.orElseThrow(() -> new RuntimeException("申請ID [" + requestId + "] が見つかりません。"));

		if (!request.getUserId().equals(userId)) {
			throw new SecurityException("権限エラー: 申請ID [" + requestId + "] はあなたの申請ではありません。");
		}

		if (request.getApply() != null && request.getApply() != 0) {
			throw new IllegalStateException("申請ID [" + requestId + "] は既に処理済みのためキャンセルできません。");
		}

		request.setApply(3);
		requestRepository.save(request);
	}

	// ====================================================================
	// 承認機能
	// ... (省略: 既存のロジックを維持)
	// ====================================================================

	@Override
	public List<Request> findAllRequestsByGroup(String approverUserId) {
		// 引数の approverUserId が確実に ID (U12345) である必要がある
		User approver = userRepository.findByUserId(approverUserId)
				.orElseThrow(() -> new RuntimeException("承認者ユーザーが見つかりません ID: " + approverUserId));

		Integer groupId = approver.getGroupId();
		if (groupId == null) {
			return List.of();
		}

		return requestRepository.findAllRequestsByGroupExcludingUser(groupId, approverUserId);
	}

	@Override
	@Transactional
	public void updateApprovalStatus(Long requestId, boolean approved, String approverId) {
		if (requestId == null) {
			throw new IllegalArgumentException("申請IDが指定されていません。");
		}
		// 1. 申請データを取得
		Request req = requestRepository.findById(requestId).orElseThrow();

		// 2. 自己承認チェック
		if (req.getUserId().equals(approverId)) {
			throw new SecurityException("自身の申請は承認できません。");
		}

		// 3. ステータス更新 (1:承認, 2:却下)
		req.setApply(approved ? 1 : 2);
		requestRepository.save(req);

		// 4. 承認された場合のみ、Outlookに飛ばす
		/*
		 * キャンセル
		 * if (approved) {
		 * User user = userRepository.findByUserId(req.getUserId()).orElse(null);
		 * if (user != null && user.getEmail() != null) {
		 * outlookService.addEventToUserCalendar(req, user);
		 * }
		 * }
		 */
	}

	@Override
	public List<Request> findAll() {
		return requestRepository.findAll();
	}

	@Override
	public List<RequestDetailDto> findAllWithNames() {
		return requestRepository.findAll().stream()
				.map(this::mapEntityToRequestDetailDto)
				.filter(RequestDetailDto::isUserExists) // ユーザーが存在するものだけを表示
				.collect(Collectors.toList());
	}

	// ====================================================================
	// 管理者機能 & パスワード
	// ... (省略: 既存のロジックを維持)
	// ====================================================================

	@Override
	@Transactional
	public void toggleApprover(String userId) {
		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
		user.setApprover(!user.isApprover());
		userRepository.save(user);
	}

	@Override
	@Transactional
	public void toggleAdmin(String userId) {
		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
		user.setAdmin(!user.isAdmin());
		userRepository.save(user);
	}

	@Override
	@Transactional
	public void updateAllRoles(List<String> displayedUserIds, List<String> approverList, List<String> adminList) {
		// 1. 今回画面に表示されていた全ユーザーのIDを対象とする
		if (displayedUserIds == null || displayedUserIds.isEmpty())
			return;

		// 2. 更新対象のユーザーをDBから持ってくる

		List<User> targetUsers = userRepository.findAllById(displayedUserIds);

		for (User user : targetUsers) {
			boolean isApprover = approverList != null && approverList.contains(user.getUserId());
			boolean isAdmin = adminList != null && adminList.contains(user.getUserId());

			user.setApprover(isApprover);
			user.setAdmin(isAdmin);
		}

		userRepository.saveAll(targetUsers);
	}

	@Override
	@Transactional
	public boolean updatePassword(String userId, String oldPassword, String newPassword) {
		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("ユーザーが見つかりません: " + userId));

		// 現在のパスワードが正しいかチェック
		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
			return false;
		}

		// 新しいパスワードをハッシュ化して保存
		String hashedNewPassword = passwordEncoder.encode(newPassword);
		user.setPassword(hashedNewPassword);
		user.setMustChangePassword(false); // パスワード変更済みフラグを解除
		userRepository.save(user);

		return true;
	}
	// ====================================================================
	// マッピングヘルパーメソッド
	// ... (省略: 既存のロジックを維持)
	// ====================================================================

	private UserDetailDto mapEntityToUserDetailDto(User userEntity) {
		UserDetailDto dto = new UserDetailDto();
		dto.setUserId(userEntity.getUserId());
		dto.setLastName(userEntity.getLastName());
		dto.setFirstName(userEntity.getFirstName());
		dto.setFullName(userEntity.getLastName() + " " + userEntity.getFirstName());
		dto.setAdmin(userEntity.isAdmin());
		dto.setApprover(userEntity.isApprover());
		dto.setEmail(userEntity.getEmail()); // ★ここ！この一行がないと画面では常にnull（未連携）になります
		dto.setGroupId(userEntity.getGroupId());

		// 部署/グループ名の結合ロジック
		Integer groupId = userEntity.getGroupId();

		if (groupId != null) {
			groupRepository.findById(groupId).ifPresent(group -> {
				dto.setGroupName(group.getGroupName());

				if (group.getDepartment() != null) {
					dto.setDepartmentName(group.getDepartment().getName());
				} else {
					dto.setDepartmentName("不明");
				}
			});
		} else {
			dto.setGroupName("未設定");
			dto.setDepartmentName("未設定");
		}

		return dto;
	}

	private RequestDetailDto mapEntityToRequestDetailDto(Request request) {
		RequestDetailDto dto = new RequestDetailDto();

		dto.setRequestId(request.getRequestId());
		dto.setUserId(request.getUserId());

		// 日付/時刻のマッピング
		if (request.getStartDate() != null) {
			dto.setStartDate(request.getStartDate().toString());
		}
		if (request.getStartTime() != null) {
			dto.setStartTime(request.getStartTime().toString());
		}
		if (request.getEndDate() != null) {
			dto.setEndDate(request.getEndDate().toString());
		}
		if (request.getEndTime() != null) {
			dto.setEndTime(request.getEndTime().toString());
		}

		dto.setReason(request.getReason());
		dto.setApply(request.getApply());

		// boolean型のマッピング
		try {
			dto.setHalfDay(request.getHalfDay());
			dto.setSpApply(request.getSpApply());
		} catch (Exception e) {
			dto.setHalfDay(false);
			dto.setSpApply(false);
		}

		// 申請者氏名の結合
		userRepository.findByUserId(request.getUserId()).ifPresentOrElse(user -> {
			dto.setApplicantFullName(user.getLastName() + " " + user.getFirstName());
			dto.setUserExists(true);
		}, () -> {
			dto.setApplicantFullName("不明なユーザー");
			dto.setUserExists(false);
		});

		return dto;
	}

	// private void updateUserFromRequest(Request request) {
	// // 未使用メソッドのため削除
	// }

	// ApplicationServiceImpl.java

	@Override
	@Transactional
	public void saveProfileRequest(ProfileRequest req) {
		if (req != null) {
			profileRequestRepository.save(req);
		}
	}

	@Override
	public List<ProfileRequest> findPendingProfileRequests() {
		List<ProfileRequest> list = profileRequestRepository.findByApply(0);
		for (ProfileRequest req : list) {
			// 現在のユーザー情報を取得してセット（表示用に一時的に保持）
			userRepository.findByUserId(req.getUserId()).ifPresent(user -> {
				req.setCurrentLastName(user.getLastName());
				req.setCurrentFirstName(user.getFirstName());
				// 現在のグループ名を取得
				if (user.getGroupId() != null) {
					groupRepository.findById(user.getGroupId())
							.ifPresent(g -> req.setCurrentGroupName(g.getGroupName()));
				}
			});

			// 変更後のグループ名も取得
			if (req.getNewGroupId() != null) {
				groupRepository.findById(req.getNewGroupId()).ifPresent(g -> req.setNewGroupName(g.getGroupName()));
			}
		}
		return list;
	}

	@Override
	@Transactional
	public void approveProfileRequest(Long requestId, boolean approved) {
		if (requestId == null) {
			throw new IllegalArgumentException("申請IDが指定されていません。");
		}
		ProfileRequest pReq = profileRequestRepository.findById(requestId)
				.orElseThrow(() -> new RuntimeException("申請が見つかりません"));

		if (approved) {
			pReq.setApply(1); // 承認
			// Userテーブルも同時に更新
			User user = userRepository.findByUserId(pReq.getUserId())
					.orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
			user.setLastName(pReq.getNewLastName());
			user.setFirstName(pReq.getNewFirstName());

			// グループ変更がある場合
			if (pReq.getNewGroupId() != null) {
				user.setGroupId(pReq.getNewGroupId());
			}
			userRepository.save(user);
		} else {
			pReq.setApply(2); // 却下
		}
		profileRequestRepository.save(pReq);
	}

	// ApplicationServiceImpl.java

	@Override
	@Transactional
	public int importUsersFromCsv(InputStream is) throws Exception {
		// Excelから出力されるCSV（Shift-JIS）に対応
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "MS932"));
		String line;
		int count = 0;

		// 既存の最大IDを算出 (共通ヘルパー使用)
		int nextIdNum = calculateNextIdNumber();

		reader.readLine(); // ヘッダー（1行目）をスキップ

		while ((line = reader.readLine()) != null) {
			if (line.trim().isEmpty())
				continue;

			// カンマ区切り（簡易的）
			String[] data = line.split(",");

			// 4カラム以上あれば処理する (4カラム=自動採番, 5カラム=ID指定)
			if (data.length < 4)
				continue;

			User user = new User();

			if (data.length >= 5) {
				// 5カラム以上: ID指定あり [ID, 姓, 名, 役職, グループID, (入社日)]
				user.setUserId(data[0].trim());
				user.setLastName(data[1].trim());
				user.setFirstName(data[2].trim());
				user.setRole(data[3].trim());
				try {
					user.setGroupId(Integer.parseInt(data[4].trim()));
				} catch (NumberFormatException e) {
					user.setGroupId(null);
				}
				// 入社日 (オプション)
				if (data.length >= 6) {
					try {
						user.setJoiningDate(LocalDate.parse(data[5].trim()));
					} catch (Exception e) {
						user.setJoiningDate(null);
					}
				}
			} else {
				// 4カラム: ID自動採番 [姓, 名, 役職, グループID, (入社日)]
				// ID生成 (T00001形式)
				String newId = String.format("T%05d", nextIdNum++);
				user.setUserId(newId);

				user.setLastName(data[0].trim());
				user.setFirstName(data[1].trim());
				user.setRole(data[2].trim());
				try {
					user.setGroupId(Integer.parseInt(data[3].trim()));
				} catch (NumberFormatException e) {
					user.setGroupId(null);
				}
				// 入社日 (オプション)
				if (data.length >= 5) {
					try {
						user.setJoiningDate(LocalDate.parse(data[4].trim()));
					} catch (Exception e) {
						user.setJoiningDate(null);
					}
				}
			}

			// --- 固定・自動設定ロジック ---

			// 1. パスワードは一律 "password" で初期化
			user.setPassword(passwordEncoder.encode("password"));
			user.setMustChangePassword(true); // CSVインポートユーザーも変更強制

			// 2. 承認者フラグ：いったん全員 false
			user.setApprover(false);

			// 3. 管理者フラグ：人事(311)または採用教育(312)なら true
			if (user.getGroupId() != null && user.getGroupId() == 311) {
				user.setAdmin(true);
			} else {
				user.setAdmin(false);
			}

			// ID重複チェック (自動採番でも念のため、指定ありなら必須)
			if (user.getUserId() != null && !userRepository.existsById(user.getUserId())) {
				userRepository.save(user);
				count++;
			} else {
				// 既に存在する場合はスキップするか、Updateするか...
				// ここではスキップとしますが、自動採番でここに来るのはレア(競合時)
				// ID指定で重複した場合はスキップされます
			}
		}
		return count;
	}

	@Override
	@Transactional
	public void registerNewUser(User user) {
		// IDが指定されていない場合は自動採番
		if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
			int nextId = calculateNextIdNumber();
			user.setUserId(String.format("T%05d", nextId));
		}

		// 既にIDが存在するかチェック
		if (user.getUserId() != null && userRepository.existsById(user.getUserId())) {
			throw new RuntimeException("ユーザーID '" + user.getUserId() + "' は既に登録されています。");
		}

		// 初期パスワードをユーザーIDと同じに設定してハッシュ化
		user.setPassword(passwordEncoder.encode(user.getUserId()));

		// 初回ログイン時にパスワード変更を強制する
		user.setMustChangePassword(true);

		// グループIDによる権限自動設定 (CSVと同様)
		if (user.getGroupId() != null && user.getGroupId() == 311) {
			user.setAdmin(true);
		}

		userRepository.save(user);
	}

	@Override
	@Transactional
	public void deleteUser(String userId) {
		// 関連データの整合性を考慮しつつ削除（または論理削除）
		if (userId != null) {
			userRepository.deleteById(userId);
		}
	}

	/**
	 * 現在の最大ID数値を取得して+1した値を返すヘルパー
	 */
	private int calculateNextIdNumber() {
		int nextIdNum = 1;
		List<User> allUsers = userRepository.findAll();
		for (User u : allUsers) {
			String uid = u.getUserId();
			if (uid != null && (uid.startsWith("U") || uid.startsWith("T"))) {
				try {
					int n = Integer.parseInt(uid.substring(1));
					if (n >= nextIdNum) {
						nextIdNum = n + 1;
					}
				} catch (NumberFormatException e) {
					// UまたはTで始まっていても数字でない場合は無視
				}
			}
		}
		return nextIdNum;
	}

}