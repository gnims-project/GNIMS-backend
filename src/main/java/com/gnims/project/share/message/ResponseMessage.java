package com.gnims.project.share.message;

public class ResponseMessage {

    //user
    public static final String SIGNUP_SUCCESS_MESSAGE = "회원가입 완료";
    public static final String PROFILE_UPDATE_SUCCESS_MESSAGE = "프로필 변경 성공";
    public static final String LOGIN_SUCCESS_MESSAGE = "로그인 성공";
    public static final String USER_SEARCH_SUCCESS_MESSAGE = "유저 검색 성공";
    public static final String SECRET_UPDATE_SUCCESS_MESSAGE = "비밀번호 변경 성공";
    public static final String CHECK_NICKNAME_MESSAGE = "사용 가능한 닉네임 입니다.";
    public static final String CHECK_EMAIL_MESSAGE = "사용 가능한 이메일 입니다.";

    //friendshipController
    public static final String READ_FOLLOWINGS_MESSAGE = "팔로잉 조회 완료";
    public static final String READ_FOLLOWERS_MESSAGE = "팔로워 조회 완료";
    public static final String COUNT_FOLLOWINGS_MESSAGE = "팔로잉 수 조회 완료";
    public static final String COUNT_FOLLOWERS_MESSAGE = "팔로워 수 조회 완료";

    //ScheduleController
    public static final String CREATE_SCHEDULE_MESSAGE = "스케줄 생성 완료";
    public static final String READ_ALL_SCHEDULE_MESSAGE = "스케줄 전체 조회 완료";
    public static final String READ_ONE_SCHEDULE_MESSAGE = "스케줄 상세 조회 완료";
    public static final String READ_PENDING_SCHEDULE_MESSAGE = "수락 대기중인 스케줄 조회 완료";
    public static final String READ_PAST_SCHEDULE_MESSAGE = "과거 스케줄 조회 완료";
    public static final String ACCEPT_SCHEDULE_MESSAGE = "스케줄 수락 완료";
    public static final String REJECT_SCHEDULE_MESSAGE = "스케줄 거절 완료";
    public static final String DELETE_SCHEDULE_MESSAGE = "스케줄 삭제 완료";
    public static final String UPDATE_SCHEDULE_MESSAGE = "스케줄 수정 완료";

    //email
    public static final String SUCCESS_AUTH_EMAIL_MESSAGE = "이메일 인증 성공";

    public static final String READ_ALL_NOTIFICATION_MESSAGE = "알림 전체 조회 완료";
    public static final String READ_ONE_NOTIFICATION_MESSAGE = "알림 상세 조회 완료";
}
