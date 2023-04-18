package org.ielts.playground.common.constant;

@SuppressWarnings("java:S2386")
public final class PathConstants {
    private PathConstants() {
    }

    public static final String ALL_PATTERN = "/**";
    public static final String API_BASE_URL = "/api";
    public static final String PRIVATE_BASE_URL = "/private";
    public static final String PUBLIC_BASE_URL = "/public";
    public static final String API_ADMIN_BASE_URL = API_BASE_URL + "/admin";
    public static final String JWT_AUTH_URL_PATTERN = API_BASE_URL + ALL_PATTERN;
    public static final String PRIVATE_AUTH_URL_PATTERN = PRIVATE_BASE_URL + ALL_PATTERN;
    public static final String ADMIN_AUTH_URL_PATTERN = API_ADMIN_BASE_URL + ALL_PATTERN;

    public static final String API_AUTHENTICATION_URL = API_BASE_URL + "/authenticate";
    public static final String API_REGISTRATION_URL = API_BASE_URL + "/register";

    public static final String API_POSTS_BASE_URL = API_BASE_URL + "/posts";
    public static final String API_POSTS_CREATION_URL = API_POSTS_BASE_URL + "";
    public static final String API_POSTS_SEARCH_URL = API_POSTS_BASE_URL + "";
    public static final String API_POSTS_VIEW_URL = API_POSTS_BASE_URL + "/{postId}";
    public static final String API_POSTS_DELETION_URL = API_POSTS_BASE_URL + "/{postId}";
    public static final String API_POSTS_UPLOAD_URL = API_POSTS_BASE_URL + "/upload";
    public static final String API_POSTS_GENERATE_URL = API_POSTS_BASE_URL + "/generate";

    public static final String API_GET_TEST_READING_SKILL = API_BASE_URL + "/random/reading";

    public static final String API_GET_TEST_LISTENING_SKILL = API_BASE_URL + "/random/listening";

    public static final String API_GET_TEST_WRITING_SKILL = API_BASE_URL + "/random/writing";

    public static final String API_CHECK_ANSWER = API_BASE_URL + "/check/answer";

    public static final String API_USERS_BASE_URL = API_BASE_URL + "/users";
    public static final String API_USERS_INFO_URL = API_USERS_BASE_URL + "";

    public static final String PRIVATE_DEV_URL = PRIVATE_BASE_URL + "/dev";
    public static final String API_DEV_URL = API_BASE_URL + "/dev";
    public static final String API_ADMIN_DEV_URL = API_ADMIN_BASE_URL + "/dev";
    public static final String PUBLIC_DEV_URL = PUBLIC_BASE_URL + "/dev";

    public static final String API_ADMIN_USERS_INFO_URL = API_ADMIN_BASE_URL + "/users/{username}";

    public static final String API_TEST_CREATION_URL = API_BASE_URL + "/test";
    public static final String API_TEST_AUDIO_URL = API_BASE_URL + "/test/{id}/audio";
    public static final String API_EXAM_SUBMISSION_URL = API_BASE_URL + "/exam/{id}/submit";
}
