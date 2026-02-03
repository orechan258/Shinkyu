package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.http.ResponseEntity;

import com.example.demo.entity.ProfileRequest;
import com.example.demo.entity.Request;
import com.example.demo.entity.User;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.UserDetailDto;
import com.example.demo.service.impl.UsageLogCsvServiceImpl;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final ApplicationService applicationService;
	private final UsageLogCsvServiceImpl csvService;

	public AdminController(ApplicationService applicationService, UsageLogCsvServiceImpl csvService) {
		this.applicationService = applicationService;
		this.csvService = csvService;

	}

	/**
	 * 管理者ホーム画面
	 */
	@GetMapping
	public String showAdminPage(Model model) {
		// 在宅勤務申請の未承認リストを取得
		List<Request> pendingRequests = applicationService.findAll().stream()
				.filter(r -> r.getApply() != null && r.getApply() == 0)
				.collect(Collectors.toList());

		model.addAttribute("pendingRequests", pendingRequests);
		model.addAttribute("allDepartments", applicationService.findAllDepartments());
		model.addAttribute("allGroups", applicationService.findAllGroups());
		return "admin_home"; // 以前のコードの admin_home から admin に合わせています
	}

	// --- プロフィール変更申請関連 ---

	@GetMapping("/applications")
	public String showApplicationsPage(Model model) {
		List<ProfileRequest> pendingRequests = applicationService.findPendingProfileRequests();
		model.addAttribute("pendingRequests", pendingRequests);
		return "admin-applications";
	}

	@PostMapping("/approve-profile-request")
	public String approveProfileRequest(@RequestParam Long requestId,
			@RequestParam boolean approved,
			RedirectAttributes ra) {
		applicationService.approveProfileRequest(requestId, approved);
		String msg = approved ? "プロフィール変更を承認しました。" : "申請を却下しました。";
		ra.addFlashAttribute("message", msg);
		return "redirect:/admin/applications";
	}

	// --- ユーザーマスタ管理（一覧・検索） ---

	@GetMapping("/users")
	public String listUsers(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) List<Integer> groupIds,
			@PageableDefault(size = 10) Pageable pageable, Model model) {

		Page<UserDetailDto> userPage = applicationService.searchUsersPaginated(search, groupIds, pageable);

		model.addAttribute("userPage", userPage);
		model.addAttribute("searchQuery", search);
		model.addAttribute("selectedGroupIds", groupIds);
		model.addAttribute("allDepartments", applicationService.findAllDepartments());
		model.addAttribute("allGroups", applicationService.findAllGroups());

		return "admin-users";
	}

	// --- ユーザー追加関連 ---

	@GetMapping("/users/add")
	public String showAddUserForm(Model model) {
		model.addAttribute("userForm", new UserForm());
		model.addAttribute("allDepartments", applicationService.findAllDepartments());
		model.addAttribute("allGroups", applicationService.findAllGroups());
		return "user-add";
	}

	@PostMapping("/users/add")
	public String addUsers(@ModelAttribute UserForm form, RedirectAttributes ra) {
		try {
			int count = 0;
			for (User user : form.getUserList()) {
				// IDは自動採番なので、名前が入っているかで行の有効性を判断
				if (user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
					applicationService.registerNewUser(user);
					count++;
				}
			}
			ra.addFlashAttribute("message", count + "名のユーザーを登録しました。");
		} catch (Exception e) {
			ra.addFlashAttribute("error", "登録失敗: " + e.getMessage());
			return "redirect:/admin/users/add";
		}
		return "redirect:/admin/users";
	}

	// --- 削除関連 ---

	@PostMapping("/users/delete")
	public String deleteUser(@RequestParam String userId, RedirectAttributes ra) {
		applicationService.deleteUser(userId);
		ra.addFlashAttribute("message", "ユーザーを削除しました。");
		return "redirect:/admin/users";
	}

	// --- 権限管理画面 (role.html用) ---

	@GetMapping("/roles")
	public String showRolesPage(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) List<Integer> groupIds,
			@PageableDefault(size = 10) Pageable pageable, Model model) {

		Page<UserDetailDto> userPage = applicationService.searchUsersPaginated(search, groupIds, pageable);

		model.addAttribute("userPage", userPage);
		model.addAttribute("searchQuery", search);
		model.addAttribute("selectedGroupIds", groupIds);
		model.addAttribute("allDepartments", applicationService.findAllDepartments());
		model.addAttribute("allGroups", applicationService.findAllGroups());

		// ページネーション用
		int startPage = Math.max(0, userPage.getNumber() - 2);
		int endPage = Math.min(Math.max(0, userPage.getTotalPages() - 1), userPage.getNumber() + 2);
		model.addAttribute("startPageForLoop", startPage);
		model.addAttribute("endPageForLoop", endPage);

		return "role";
	}

	/**
	 * 権限の一括保存処理
	 */
	@PostMapping("/roles/save")
	public String saveAllRoles(
			@RequestParam(name = "approverStatus", required = false) List<String> approverList,
			@RequestParam(name = "adminStatus", required = false) List<String> adminList,
			RedirectAttributes ra) {

		try {
			applicationService.updateAllRoles(approverList, adminList);
			ra.addFlashAttribute("message", "権限の設定を保存しました。");
		} catch (Exception e) {
			ra.addFlashAttribute("error", "保存中にエラーが発生しました。");
		}

		return "redirect:/admin/roles";
	}

	@GetMapping("/export")
	public ResponseEntity<byte[]> exportCsv(
			@RequestParam(name = "groupIds", required = false) List<Integer> groupIds) {

		return csvService.exportByGroups(groupIds);
	}
}