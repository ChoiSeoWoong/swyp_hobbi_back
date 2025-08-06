package swyp.hobbi.swyphobbiback.email.service;

public class EmailContentBuilder {
    public static String getVerificationTitle() {
        return "[Hobbi] 이메일 인증 코드";
    }

    public static String buildVerificationContent(String code) {
        return """
                <div style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;">
                    <div style="max-width: 500px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                        <h1 style="color: #333333; text-align: center;">Hobbi 인증 코드</h1>
                        <p style="font-size: 16px; color: #333333;">아래 인증 코드를 입력해주세요:</p>
                        <div style="font-size: 32px; font-weight: bold; text-align: center; color: #16EBC2; margin: 20px 0;">%s</div>
                        <p style="font-size: 14px; color: #666666;">인증 코드는 3분간 유효합니다.</p>
                        <hr style="margin: 30px 0; border: none; border-top: 1px solid #eeeeee;">
                        <p style="font-size: 12px; color: #aaaaaa;">본 메일은 발신 전용입니다.</p>
                    </div>
                </div>
                """.formatted(code);
    }
}
