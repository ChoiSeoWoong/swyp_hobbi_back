package swyp.hobbi.swyphobbiback.common.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 4xx
    INVALID_INPUT_VALUE(400, "INVALID_INPUT_VALUE", "요청 값이 유효하지 않습니다."),
    EMAIL_NOT_VERIFIED(400, "EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다."),
    INVALID_TOKEN(400, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    USER_ALREADY_DELETED(400, "USER_ALREADY_DELETED", "이미 탈퇴한 사용자입니다."),
    INVALID_OAUTH_PROVIDER(400, "INVALID_OAUTH_PROVIDER", "지원하지 않는 소셜 로그인입니다."),
    SOCIAL_INFO_NOT_FOUND(400, "SOCIAL_INFO_NOT_FOUND", "연동할 소셜 계정 정보가 없습니다."),
    INVALID_EMAIL_CODE(400, "INVALID_EMAIL_CODE", "잘못된 인증 코드이거나 만료되었습니다."),
    EXPIRED_TOKEN(401, "EXPIRED_TOKEN", "토큰이 만료되었습니다."),
    PASSWORD_NOT_MATCH(401, "PASSWORD_NOT_MATCH", "비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED(401, "UNAUTHORIZED","인증이 필요합니다."),
    FORBIDDEN(403, "FORBIDDEN","접근 권한이 없습니다."),
    USER_NOT_FOUND(404, "USER_NOT_FOUND","사용자를 찾을 수 없습니다."),
    POST_NOT_FOUND(404, "POST_NOT_FOUND","게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(404, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(404, "IMAGE_NOT_FOUND", "이미지를 찾을 수 없습니다."),

    EMAIL_ALREADY_EXISTS(409, "EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(409, "NICKNAME_ALREADY_EXISTS", "이미 존재하는 닉네임입니다."),
    SOCIAL_ALREADY_LINKED(409, "SOCIAL_ALREADY_LINKED", "이미 연동된 소셜 계정입니다."),
    TOO_MANY_REQUESTS(429, "TOO_MANY_REQUESTS", "인증 코드는 30초에 한 번 요청 가능합니다."),

    // 5xx
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR","서버 에러가 발생했습니다."),
    FAILED_TO_UPLOAD_FILE(500, "FAILED_TO_UPLOAD_FILE", "파일 업로드에 실패하였습니다."),
    EXCEED_FILE_SIZE_LIMIT(500, "EXCEED_FILE_SIZE_LIMIT", "파일 용량이 초과되었습니다."),
    REDIS_WRITE_FAILED(500, "REDIS_WRITE_FAILED", "Redis 정보 저장에 실패하였습니다."),
    EMAIL_SEND_FAILED(500,"EMAIL_SEND_FAILED", "이메일 전송에 실패하였습니다."),
    REJECTED_BY_THREAD_POOL(500, "REJECTED_BY_THREAD_POOL", "스레드 작업이 거부되었습니다.");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
