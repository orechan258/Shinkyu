// =================================================================
// 🛡️ backend_auth.js
// サーバーサイドでの認証とパスワードロジック
// =================================================================

// 実際は bcrypt や Argon2 などの強力なライブラリを使用します
// ここでは非同期関数として、ハッシュ化/比較をシミュレートします
const passwordHashingLibrary = {
    // 実際のハッシュ生成ロジックをシミュレート
    hash: async (password) => { 
        // 実際のコードではここにbcrypt.hash(password, salt)などが入る
        return `HASHED_${password}_${Math.random().toString(36).substring(2, 8)}`; 
    },
    // 実際のハッシュ比較ロジックをシミュレート
    compare: async (plainPassword, hash) => { 
        // 実際のコードではここにbcrypt.compare(plainPassword, hash)などが入る
        return hash.includes(`HASHED_${plainPassword}`);
    }
};

// --- 初期設定 ---
const INITIAL_PASSWORD_PLAIN = 'password';

// -----------------------------------------------------------------
// 【仕様2/3: 強度検証と初期パスワード禁止】
// -----------------------------------------------------------------

/**
 * 新しいパスワードがセキュリティ要件と初期パスワードの禁止条件を満たしているか検証します。
 * * @param {string} newPassword - ユーザーが入力した新しいパスワード（平文）
 * @param {string} currentPasswordHash - DBに保存されている現在のパスワードのハッシュ値
 * @returns {Promise<{isValid: boolean, message: string}>} - 検証結果とエラーメッセージ
 */
export async function validateNewPassword(newPassword, currentPasswordHash) {
    // 1. 文字数チェック (最低8文字以上)
    if (newPassword.length < 8) {
        return { isValid: false, message: "パスワードは最低8文字以上で設定してください。" };
    }

    // 2. 文字種チェック (大文字、小文字、記号の3種類すべてを必須)
    const hasUpperCase = /[A-Z]/.test(newPassword);
    const hasLowerCase = /[a-z]/.test(newPassword);
    const hasSymbol = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(newPassword);

    if (!hasUpperCase || !hasLowerCase || !hasSymbol) {
        return { 
            isValid: false, 
            message: "パスワードには大文字、小文字、および記号をそれぞれ1文字以上含める必要があります。" 
        };
    }
    
    // 3. 【重要】初期パスワードとの一致チェック（ハッシュ値比較）
    // newPasswordをハッシュ化しなくても、currentPasswordHashが'password'のハッシュ値であるため、
    // compare関数を使って、newPasswordが現在のハッシュ値と一致しないかをチェックすれば良い
    const isSameAsCurrent = await passwordHashingLibrary.compare(newPassword, currentPasswordHash);

    if (isSameAsCurrent) {
        return { isValid: false, message: "初期パスワード ('password') は安全のため使用できません。別のパスワードを設定してください。" };
    }

    // 4. すべての条件をクリア
    return { isValid: true, message: "OK" };
}

// -----------------------------------------------------------------
// 【仕様3: 初回強制変更フロー】
// -----------------------------------------------------------------

/**
 * ユーザー登録処理（初期設定）
 * @param {string} email 
 * @returns {Promise<object>}
 */
export async function registerUser(email) {
    // 1. 初期パスワードのハッシュ値を生成
    const initialHash = await passwordHashingLibrary.hash(INITIAL_PASSWORD_PLAIN);
    
    // 2. データベースに保存（must_change_password: trueを設定）
    const newUserRecord = {
        email: email,
        password_hash: initialHash, 
        must_change_password: true // 初回強制変更フラグ
    };
    
    // 実際はここでDBにインサートする
    console.log(`[DB] 新規ユーザー登録: ${email}, フラグ: TRUE`); 
    return newUserRecord;
}

/**
 * ログイン後の処理（強制変更へのリダイレクト判定）
 * @param {object} user - DBから取得したユーザーレコード
 * @param {boolean} isAuthSuccessful - 認証が成功したか
 * @returns {string} - リダイレクト先のURL
 */
export function determineLoginRedirect(user, isAuthSuccessful) {
    if (!isAuthSuccessful) {
        return '/login'; // 認証失敗
    }

    if (user.must_change_password === true) {
        return '/change-password-force'; // 強制変更画面へ
    } else {
        return '/dashboard'; // 通常画面へ
    }
}

/**
 * パスワード変更処理
 * @param {number} userId 
 * @param {string} newPassword - 新しいパスワード（平文）
 * @param {string} currentHash - 現在のDBハッシュ値
 * @returns {Promise<{success: boolean, message: string}>}
 */
export async function changePassword(userId, newPassword, currentHash) {
    // 1. パスワード検証を実行
    const validation = await validateNewPassword(newPassword, currentHash);

    if (validation.isValid === false) {
        return { success: false, message: validation.message };
    }

    // 2. 新しいパスワードをハッシュ化
    const newHash = await passwordHashingLibrary.hash(newPassword);

    // 3. データベースを更新（パスワードハッシュとフラグの更新）
    // 実際はここでDBを更新する
    console.log(`[DB] ユーザー ${userId} のパスワードを更新し、フラグを FALSE に設定`);
    
    return { success: true, message: "パスワードが正常に変更されました。" };
}