package com.mikechoch.prism.constant;

public class Key {

    // DO NOT CHANGE ANY OF THESE //
    // THESE ARE LINKED TO MANY OTHER THINGS //

    /** STORAGE **/
    public static final String STORAGE_POST_IMAGES_REF = "POST_IMAGES";
    public static final String STORAGE_USER_PROFILE_IMAGE_REF = "USER_PROFILES";
    public static final String STORAGE_DEFALT_PROFILE_PICS_REF = "DEFAULT_PROFILES";


    /** USERS **/
    public static final String DB_REF_USER_PROFILES = "USERS";
    public static final String DB_REF_USER_UPLOADS = "USER_UPLOADS";
    public static final String DB_REF_USER_LIKES = "USER_LIKES";
    public static final String DB_REF_USER_REPOSTS = "USER_REPOSTS";
    public static final String DB_REF_USER_FOLLOWERS = "USER_FOLLOWERS";
    public static final String DB_REF_USER_FOLLOWINGS = "USER_FOLLOWINGS";
    public static final String DB_REF_USER_NOTIFICATIONS = "USER_NOTIFICATIONS";
    public static final String DB_REF_USER_PREFERENCES = "USER_PREFERENCES";
    public static final String DB_REF_USER_NEWS_FEED = "USER_NEWS_FEED";

    public static final String USER_PROFILE_FULL_NAME = "fullname";
    public static final String USER_PROFILE_USERNAME = "username";
    public static final String USER_PROFILE_PIC = "profilepic";
    public static final String USER_TOKEN = "usertoken";

    public static final String NOTIFICATION_MOST_RECENT_USER = "mostRecentUid";
    public static final String NOTIFICATION_ACTION_TIMESTAMP = "actionTimestamp";
    public static final String NOTIFICATION_VIEWED_TIMESTAMP = "viewedTimestamp";

    public static final String PREFERENCE_ALLOW_LIKE_NOTIFICATION = "allowLikeNotification";
    public static final String PREFERENCE_ALLOW_REPOST_NOTIFICATION = "allowRepostNotification";
    public static final String PREFERENCE_ALLOW_FOLLOW_NOTIFICATION = "allowFollowNotification";


    /** ALL POSTS **/
    public static final String DB_REF_ALL_POSTS = "ALL_POSTS";
    public static final String DB_REF_POST_LIKED_USERS = "LIKED_USERS";
    public static final String DB_REF_POST_REPOSTED_USERS = "REPOSTED_USERS";
    public static final String POST_UID = "uid";
    public static final String POST_IMAGE_URI = "image";
    public static final String POST_DESC = "caption";
    public static final String POST_TIMESTAMP = "timestamp";

    /** CONTENT REVIEW **/
    public static final String DB_REF_CONTENT_REVIEW = "CONTENT_REVIEW";
    public static final String REPORTED_BY = "reportedBy";


    /** APP STATUS **/
    public static final String DB_REF_APP_STATUS = "APP_STATUS";
    public static final String STATUS_IS_ACTIVE = "isActive";
    public static final String STATUS_MESSAGE = "message";
    public static final String MIN_APP_VERSION = "minAppVersion";

    /** ACCOUNTS **/
    public static final String DB_REF_ACCOUNTS = "ACCOUNTS";

    /** TAGS **/
    public static final String DB_REF_TAGS = "TAGS";

}
