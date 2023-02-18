package com.study.asy.exception;

public class BackOfficeCustomException extends RuntimeException {

    private static final long serialVersionUID = 483703226492523389L;

    private static final int REQUEST_INVALID_SHOW_ALERT_CLOSE = 451;
    private static final int REQUEST_INVALID_SHOW_ALERT_HISTORY_BACK = 452;
    private static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final int CONFLICT = 409;
    private static final int NOT_FOUND = 404;


    private final int statusCd;
    private final String message;

    public BackOfficeCustomException(int statusCd, String message) {
        super(message);
        this.statusCd = statusCd;
        this.message = message;
    }

    public BackOfficeCustomException(BACKOFFICE_ERROR customError) {
        super(customError.getMsg());
        this.statusCd = customError.getStatusCd();
        this.message = customError.getMsg();
    }

    public int getStatusCd() {
        return statusCd;
    }

    public String getStatusMessage() {
        return message;
    }

    /*
     * 커스터 에러 응답
     */
    public enum BACKOFFICE_ERROR {

        COMMON_NOT_FOUND(REQUEST_INVALID_SHOW_ALERT_CLOSE, "데이터가 삭제되었거나, 올바른 데이터가 아닙니다."),

        INVALID_PARAMETER(REQUEST_INVALID_SHOW_ALERT_HISTORY_BACK, "요청 파라미터가 잘못 되었습니다."),

        EVENT_ID_NOT_FOUND(REQUEST_INVALID_SHOW_ALERT_HISTORY_BACK, "이벤트 아이디가 없거나 잘못 되었습니다."),

        EVENT_REPLY_ID_NOT_FOUND(REQUEST_INVALID_SHOW_ALERT_HISTORY_BACK, "이벤트 댓글이 없거나 잘못 되었습니다."),

        EVENT_REPLY_IS_NOT_DELETE(REQUEST_INVALID_SHOW_ALERT_HISTORY_BACK, "댓글로 참여완료한 이벤트의 댓글은 삭제하실 수 없습니다."),

        COUPONS_ID_NOT_FOUND(REQUEST_INVALID_SHOW_ALERT_HISTORY_BACK, "쿠폰 아이디가 없거나 잘못 되었습니다."),

        ADMIN_REGISTER_NOT_FOUND(REQUEST_INVALID_SHOW_ALERT_HISTORY_BACK, "어드민 계정이 없거나 잘못 되었습니다."),

        POPUP_IDREGISTER_NOT_FOUND(REQUEST_INVALID_SHOW_ALERT_HISTORY_BACK, "시작팝업 idRegister 값이 없거나 잘못 되었습니다."),

        POPUP_ID_NOT_FOUND(REQUEST_INVALID_SHOW_ALERT_HISTORY_BACK, "시작팝업 아이디가 없거나 잘못 되었습니다."),

        HOTEDITER_DUPLICATE(REQUEST_INVALID_SHOW_ALERT_HISTORY_BACK, "19년 10월 확정된 운영 정책 : 같은 기간동안 동일한 2차 카테고리의 추천제품으로 노출가능한 제품은 1개 존재합니다"),

        SPLISH_AOS1_FILE_CHECK_ERROR(REQUEST_INVALID_SHOW_ALERT_HISTORY_BACK, "스플래쉬 AOS 타입 0 파일 정합성이 안맞습니다"),

        SPLISH_AOS2_FILE_CHECK_ERROR(REQUEST_INVALID_SHOW_ALERT_HISTORY_BACK, "스플래쉬 AOS 타입 1 파일 정합성이 안맞습니다"),

        SPLISH_AOS3_FILE_CHECK_ERROR(REQUEST_INVALID_SHOW_ALERT_HISTORY_BACK, "스플래쉬 AOS 타입 2 파일 정합성이 안맞습니다"),

        SPLISH_IOS_FILE_CHECK_ERROR(REQUEST_INVALID_SHOW_ALERT_HISTORY_BACK, "스플래쉬 IOS 타입 파일 정합성이 안맞습니다"),

        REGISTER_NOT_FOUND(NOT_FOUND, "없는 사용자입니다."),

        EVENT_REPLY_CONFLICT_ERROR(CONFLICT, "이미 댓글을 입력하셨습니다."),

        EVENT_REPLY_NOT_FOUND(NOT_FOUND, "없는 이벤트 댓글입니다."),

        REGISTER_NOT_AUTHORIZED_TO_MODIFY_EVENT_REPLY(UNAUTHORIZED, "이벤트 댓글을 수정할 권한이 없습니다."),

        REGISTER_CANNOT_DELETE_EVENT_REPLY(UNAUTHORIZED, "이벤트 댓글을 삭제할 수 없습니다."),

        PREREQUISITE_TYPE_NOT_AVAILABLE(NOT_FOUND, "없는 참여조건 항번입니다."),

        REGISTER_ALREADY_WROTE_CAST_REPLY(FORBIDDEN, "캐스트 댓글을 이미 작성하였습니다."),

        REGISTER_NOT_AUTHORIZED_TO_MODIFY_CAST_REPLY(UNAUTHORIZED, "캐스트 댓글을 수정할 권한이 없습니다."),

        REGISTER_NOT_AUTHORIZED_TO_DELETE_CAST_REPLY(UNAUTHORIZED, "캐스트 댓글을 삭제할 권한이 없습니다."),

        CAST_REPLY_NOT_FOUND(NOT_FOUND,"없는 캐스트 댓글입니다."),

        POUCH_LIKE_CONFLICT_ERROR(CONFLICT, "이미 좋아요가 있습니다."),

        POUCH_INTEREST_CONFLICT_ERROR(CONFLICT, "이미 설정 했습니다."),

        TOKEN_VALIDATION_FAIL(UNAUTHORIZED, "토큰이 유효하지 않습니다.");


        private int statusCd;
        private String msg;

        BACKOFFICE_ERROR(int statusCd, String msg) {
            this.statusCd = statusCd;
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "CUSTOM_EXCEPTION_ERROR [" + statusCd + "] " + msg;
        }

        public String getMsg() {
            return msg;
        }

        public int getStatusCd() {
            return statusCd;
        }
    }


}
