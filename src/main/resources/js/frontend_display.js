// =================================================================
// 💰 frontend_display.js
// UI表示とクライアントサイドでの入力フィードバック
// =================================================================

/**
 * 【仕様1: パスワードマスキング】
 * 最初の3文字を表示し、残りをアスタリスク7個で埋めて合計10文字にする。
 * * @param {string} password - 実際のパスワード文字列（※表示用、セキュリティを考慮しDBから平文を直接渡すべきではない）
 * @returns {string} - マスキングされた文字列 (例: "abc*******")
 */
export function maskPasswordDisplay(password) {
    if (!password || password.length === 0) {
        return "**********"; 
    }
    const prefix = password.substring(0, 3);
    const suffix = '*******';
    return prefix + suffix;
}

// --- リアルタイムUIフィードバック用 ---

/**
 * パスワード入力中にユーザーに強度要件を満たしているかフィードバックするための簡易チェック。
 * @param {string} newPassword - 入力中のパスワード
 * @returns {{length: boolean, upper: boolean, lower: boolean, symbol: boolean}}
 */
export function checkPasswordClientSide(newPassword) {
    return {
        // 8文字以上
        length: newPassword.length >= 8,
        // 大文字を含む
        upper: /[A-Z]/.test(newPassword),
        // 小文字を含む
        lower: /[a-z]/.test(newPassword),
        // 記号を含む
        symbol: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(newPassword)
    };
}

// -----------------------------------------------------------------
// 使用例 (ブラウザコンソールなどでテスト用)
// -----------------------------------------------------------------
// console.log("マスキング表示:", maskPasswordDisplay("P@ssword123")); 
// console.log("クライアントチェック:", checkPasswordClientSide("abc"));