package com.example.demo.controller;

import com.example.demo.exception.WeeklyLimitExceededException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Request;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.RequestDetailDto;

import jakarta.servlet.http.HttpSession;

@Controller
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public ApplicationController(ApplicationService applicationService, UserRepository userRepository) {
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    // --- 認証・ホーム関連 ---

    @GetMapping("/")
    public String login() {
        return "login";
    }

    // ApplicationController.java に追加
    @GetMapping("/home")
    public String home(Model model, Authentication authentication, HttpSession session) {
        if (authentication == null) {
            return "redirect:/";
        }

        String userId = null;
        Object principal = authentication.getPrincipal();

        // 1. Googleログイン（OAuth2User）の場合の処理
        if (principal instanceof OAuth2User oAuth2User) {
            String googleEmail = oAuth2User.getAttribute("email");

            // メアドからDBのユーザーを特定
            userId = userRepository.findByEmail(googleEmail)
                    .map(com.example.demo.entity.User::getUserId)
                    .orElse(null);

            // --- 自動連携（紐付け）ロジック ---
            String pendingUserId = (String) session.getAttribute("PENDING_LINK_USER_ID");

            // まだDBにメアドがないが、プロフィール画面から「連携ボタン」を押して来た場合
            if (userId == null && pendingUserId != null) {
                // ① DBのemailカラムを更新
                applicationService.updateUserEmail(pendingUserId, googleEmail);
                session.removeAttribute("PENDING_LINK_USER_ID");

                // ② DBから最新の権限状態（Admin/Approverフラグ）を取得
                var userEntity = userRepository.findByUserId(pendingUserId)
                        .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません: " + pendingUserId));

                // ③ 権限リスト（Authorities）を再構築
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                if (userEntity.isAdmin()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }
                if (userEntity.isApprover()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_APPROVER"));
                }

                // ④ 現在のセッション情報を正しい権限で上書き（これで管理者メニューが復活する）
                UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(pendingUserId,
                        null, authorities);
                SecurityContextHolder.getContext().setAuthentication(newAuth);

                // 連携完了メッセージと共にプロフィールへ戻す
                return "redirect:/profile?success=google_linked";
            }

            // 連携済みだがROLE_GUEST扱いになっている場合のフォールバック（初回ログイン時など）
            if (userId != null
                    && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GUEST"))) {
                // ここでも権限を復元してあげると親切（必要に応じて実装）
            }

        } else {
            // 通常ログイン（ID/PASS）の場合
            userId = authentication.getName();
        }

        // 2. ROLE_GUEST (Googleログインしたが、まだDBにメアドがなく紐付けもしてない人)
        boolean isGuest = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GUEST"));

        if (isGuest && userId == null) {
            return "redirect:/link-account";
        }

        // 3. 正常な表示処理（ホーム画面の「山田 太郎 さん、こんにちは」用）
        if (userId != null) {
            applicationService.findUserDetail(userId).ifPresent(dto -> {
                model.addAttribute("userName", dto.getFullName());
            });
            return "home";
        }

        return "redirect:/";
    }

    /**
     * アカウント紐付け画面の表示
     */
    @GetMapping("/link-account")
    public String linkAccountPage(Authentication authentication) {
        if (authentication == null)
            return "redirect:/";

        // 既に連携済みの人が来たらホームへ戻す
        boolean isGuest = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GUEST"));
        if (!isGuest) {
            return "redirect:/home";
        }
        return "link_account";
    }

    /**
     * アカウント紐付け処理の実行
     */
    /**
     * アカウント紐付け処理の実行
     * (修正) ユーザーID入力廃止により、Googleアカウント情報から新規ユーザーを作成・登録するフローに変更
     */
    @PostMapping("/link-account")
    public String processLinkAccount(
            @RequestParam String password,
            Authentication authentication,
            Model model) {

        // OAuth2Userであることを確認してメールアドレスを取得
        if (!(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            return "redirect:/";
        }
        String email = oAuth2User.getAttribute("email");
        String lastName = oAuth2User.getAttribute("family_name");
        String firstName = oAuth2User.getAttribute("given_name");

        if (lastName == null)
            lastName = "Guest";
        if (firstName == null)
            firstName = "User";

        try {
            // 新規ユーザーとして登録
            com.example.demo.entity.User newUser = new com.example.demo.entity.User();
            newUser.setEmail(email);
            newUser.setLastName(lastName);
            newUser.setFirstName(firstName);
            newUser.setPassword(password); // Service内でハッシュ化されることを想定

            // 部署・グループは未定(null)として登録
            // (必要であれば後でプロフィールから設定などの運用)

            applicationService.registerNewUser(newUser);

            // 連携(登録)完了
            return "redirect:/?linked";

        } catch (Exception e) {
            model.addAttribute("error", "登録に失敗しました: " + e.getMessage());
            return "link_account";
        }
    }

    @GetMapping("/finish")
    public String finish() {
        return "request_finish";
    }

    // --- 申請機能 ---

    @GetMapping("/request")
    public String request(Model model) {
        model.addAttribute("request", new Request());
        return "request";
    }

    @PostMapping("/request")
    public String submitRequest(@ModelAttribute("request") Request request,
            @RequestParam(required = false) boolean confirmed,
            Authentication authentication,
            RedirectAttributes ra,
            Model model) {

        if (authentication != null) {
            String userId;
            // Googleログインか通常ログインかを判定して、DB上の正しい「ユーザーID」を取得する
            if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
                String email = oAuth2User.getAttribute("email");
                userId = userRepository.findByEmail(email)
                        .map(com.example.demo.entity.User::getUserId)
                        .orElse("GUEST_01"); // 見つからない場合のフォールバック
            } else {
                userId = authentication.getName(); // 通常ログイン(ID/PASS)ならそのまま
            }

            request.setUserId(userId); // ここで短いID（U12345等）がセットされるのでエラーが消える
        } else {
            request.setUserId("GUEST_01");
        }

        try {
            applicationService.createNewRequest(request, confirmed);
            return "redirect:/finish";
        } catch (WeeklyLimitExceededException e) {
            model.addAttribute("warning", e.getMessage() + " 所属長に相談済みですか？");
            model.addAttribute("needsConfirmation", true);
            model.addAttribute("request", request);
            return "request";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/request";
        }
    }

    // --- 申請確認機能 (/check) ---

    @GetMapping("/check")
    public String checkRequest(Model model, Authentication authentication) { // PrincipalからAuthenticationに変更
        if (authentication == null) {
            return "redirect:/";
        }

        // --- 正しいユーザーIDを特定するロジック ---
        String currentUserId;
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            // DBからメールアドレスをキーにユーザーID (U12345等) を探す
            currentUserId = userRepository.findByEmail(email)
                    .map(com.example.demo.entity.User::getUserId)
                    .orElse(null);
        } else {
            currentUserId = authentication.getName(); // 通常ログイン(ID/PASS)の場合
        }

        if (currentUserId == null) {
            return "redirect:/";
        }

        // 正しい業務IDでDBから申請リストを取得
        List<RequestDetailDto> allUserRequestsWithNames = applicationService.findMyRequestsWithNames(currentUserId);

        LocalDateTime now = LocalDateTime.now();

        // 1. 承認待ちのリスト
        List<RequestDetailDto> pendingRequests = allUserRequestsWithNames.stream()
                .filter(req -> (req.getApply() == null || req.getApply() == 0) && !isExpiredDto(req, now))
                .toList();

        // 2. 確認済みのリスト
        List<RequestDetailDto> completedRequests = allUserRequestsWithNames.stream()
                .filter(req -> req.getApply() != null && (req.getApply() == 1 || req.getApply() == 2))
                .toList();

        // 3. 期限切れのリスト
        List<RequestDetailDto> expiredRequests = allUserRequestsWithNames.stream()
                .filter(req -> (req.getApply() == null || req.getApply() == 0) && isExpiredDto(req, now))
                .toList();

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("completedRequests", completedRequests);
        model.addAttribute("expiredRequests", expiredRequests);

        return "check";
    }

    /**
     * POST /check/cancel
     * ユーザーからの申請キャンセルを受け付ける
     */
    @PostMapping("/check/cancel")
    public String cancelRequestAction(@RequestParam Long requestId, Principal principal) {
        if (principal == null) {
            return "redirect:/"; // 認証されていない場合はログインへ
        }
        String currentUserId = principal.getName();

        try {
            // Service層で、このrequestIdがcurrentUserIdの所有物であることを確認してからキャンセル処理を実行
            applicationService.cancelRequest(requestId, currentUserId);
        } catch (Exception e) {
            // エラー処理（例: ログ出力、申請が見つからないなど）
            // return "redirect:/check?error=cancel_failed";
        }

        // キャンセル後、申請確認画面に戻る
        return "redirect:/check?success=cancelled";
    }

    // 期限切れ判定用のヘルパーメソッド (DTO用)
    private boolean isExpiredDto(RequestDetailDto req, LocalDateTime now) {
        if (req.getEndDate() == null || req.getEndTime() == null) {
            return false;
        }
        try {
            // DTOはStringで時刻を持つため、DateTimeFormatterが必要
            String endDateTimeStr = req.getEndDate() + " " + req.getEndTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 適切なフォーマットを使用
            LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeStr, formatter);

            return endDateTime.isBefore(now);
        } catch (Exception e) {
            return false;
        }
    }

    // ユーザーID特定用の共通ヘルパーメソッドを追加
    // ユーザーID特定用の共通ヘルパーメソッドを追加 (削除: 未使用のため)

}