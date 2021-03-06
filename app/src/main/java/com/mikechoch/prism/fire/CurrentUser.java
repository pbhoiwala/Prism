package com.mikechoch.prism.fire;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikechoch.prism.attribute.Notification;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.attribute.PrismUser;
import com.mikechoch.prism.attribute.UserPreference;
import com.mikechoch.prism.callback.fetch.OnFetchUserProfileCallback;
import com.mikechoch.prism.helper.IntentHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class CurrentUser {

    private static PrismUser prismUser;
    public static UserPreference preference;

    static ArrayList<PrismPost> news_feed;


    /**
     * Key: String postId
     * Value: long timestamp
    **/
    static HashMap<String, Long> liked_posts_map;
    static HashMap<String, Long> reposted_posts_map;
    static HashMap<String, Long> uploaded_posts_map;

    /** ArrayList of PrismPost objects for above structures **/
    private static ArrayList<PrismPost> liked_posts;
    private static ArrayList<PrismPost> reposted_posts;
    private static ArrayList<PrismPost> uploaded_posts;
    private static ArrayList<PrismPost> uploaded_and_reposted_posts;

    /** ArrayList of NotificationType objects for above structures **/
    static ArrayList<Notification> notifications;

    /**
     * Key: String uid
     * Value: Long timestamp
     */
    static HashMap<String, Long> followers;
    static HashMap<String, Long> followings;


    private CurrentUser() {
        prismUser = null;
        preference = null;

        liked_posts = new ArrayList<>();
        reposted_posts = new ArrayList<>();
        uploaded_posts = new ArrayList<>();
        uploaded_and_reposted_posts = new ArrayList<>();

        liked_posts_map = new HashMap<>();
        reposted_posts_map = new HashMap<>();
        uploaded_posts_map = new HashMap<>();

        followers = new HashMap<>();
        followings = new HashMap<>();

    }

    public static void prepareApp(Context context) {
        new CurrentUser();
        DatabaseRead.constructCurrentUserProfile(new OnFetchUserProfileCallback() {
            @Override
            public void onSuccess() {
                IntentHelper.intentToMainActivity(context);
            }

            @Override
            public void onFailure(Exception e) {
                // TODO
            }
        });
    }

    public static void refreshUser(OnFetchUserProfileCallback callback) {
        new CurrentUser();
        DatabaseRead.constructCurrentUserProfile(callback);
    }


    /**
     * Returns True if CurrentUser is following given PrismUser
     * @param prismUser
     * @return
     */
    public static boolean isFollowingPrismUser(PrismUser prismUser) {
        return followings.containsKey(prismUser.getUid());
    }

    /**
     * Adds given prismUser to CurrentUser's followings HashMap
     * @param prismUser
     * @param timestamp
     */
    static void followUser(PrismUser prismUser, Long timestamp) {
        followings.put(prismUser.getUid(), timestamp);
    }


    /**
     * Removes given PrismUser from CurrentUser's followings HashMap
     * @param prismUser
     */
    static void unfollowUser(PrismUser prismUser) {
        if (followings.containsKey(prismUser.getUid())) {
            followings.remove(prismUser.getUid());
        }
    }

    /**
     * Returns True if given PrismUser is a follower of CurrentUser
     * @param prismUser
     * @return
     */
    public static boolean isPrismUserFollower(PrismUser prismUser) {
        return followers.containsKey(prismUser.getUid());
    }


    /**
     * Returns True if CurrentUser has liked given prismPost
     * @param prismPost
     * @return
     */
    public static boolean hasLiked(PrismPost prismPost) {
        return liked_posts != null && liked_posts_map.containsKey(prismPost.getPostId());
    }

    /**
     * Adds prismPost to CurrentUser's liked_posts list and hashMap
     * @param prismPost
     */
    static void likePost(PrismPost prismPost) {
        liked_posts.add(prismPost);
        liked_posts_map.put(prismPost.getPostId(), prismPost.getTimestamp());
    }

    /**
     * Adds list of liked prismPosts to CurrentUser's liked_posts list
     * @param likedPosts
     */
    static void addLikedPosts(ArrayList<PrismPost> likedPosts) {
        liked_posts.addAll(likedPosts);
    }

    /**
     * Removes prismPost from CurrentUser's liked_posts list and hashMap
     * @param prismPost
     */
    static void unlikePost(PrismPost prismPost) {
        liked_posts.remove(prismPost);
        liked_posts_map.remove(prismPost.getPostId());
    }

    /**
     * Returns True if CurrentUser has reposted given prismPost
     * @param prismPost
     * @return
     */
    public static boolean hasReposted(PrismPost prismPost) {
        return hasReposted(prismPost.getPostId());
    }

    public static boolean hasReposted(String prismPostId) {
        return reposted_posts_map != null && reposted_posts_map.containsKey(prismPostId);
    }

    /**
     * Adds prismPost to CurrentUser's reposted_posts list and hashMap
     * @param prismPost
     */
    static void repostPost(PrismPost prismPost) {
        reposted_posts.add(prismPost);
        reposted_posts_map.put(prismPost.getPostId(), prismPost.getTimestamp());
    }

    /**
     * Adds the list of reposted prismPosts to CurrentUser's reposted_posts list
     * @param repostedPosts
     */
    static void addRepostedPosts(ArrayList<PrismPost> repostedPosts) {
        reposted_posts.addAll(repostedPosts);
        for (PrismPost prismPost : repostedPosts) {
            prismPost.setIsReposted(true);
        }
    }

    /**
     * Removes prismPost from CurrentUser's repost_posts list and hashMap
     * @param prismPost
     */
    static void unrepostPost(PrismPost prismPost) {
        reposted_posts.remove(prismPost);
        reposted_posts_map.remove(prismPost.getPostId());
    }

    /**
     * Adds prismPost to CurrentUser's uploaded_posts list and hashMap
     * @param prismPost
     */
    static void uploadPost(PrismPost prismPost) {
        uploaded_posts.add(prismPost);
        uploaded_posts_map.put(prismPost.getPostId(), prismPost.getTimestamp());
    }

    /**
     * Adds the list of uploaded prismPosts to CurrentUser's uploaded_posts list and hashMap
     * @param uploadedPosts
     */
    static void addUploadPosts(ArrayList<PrismPost> uploadedPosts) {
        uploaded_posts.addAll(uploadedPosts);
    }

    /**
     * Removes prismPost from CurrentUser's uploaded_posts list and hashMap
     * @param prismPost
     */
    static void deletePost(PrismPost prismPost) {
        uploaded_posts.remove(prismPost);
        uploaded_posts_map.remove(prismPost.getPostId());
    }

    static void addNotifications(Collection<Notification> userNotifications) {
        notifications = new ArrayList<>(userNotifications);
    }

    static void addNotification(Notification notification) {
        notifications.add(0, notification);
    }


    static void removeNotification(Notification notification) {
        notifications.remove(notification);
    }


    /**
     * Returns list of CurrentUser.uploaded_posts
     */
    public static ArrayList<PrismPost> getUserUploads() {
        return uploaded_posts;
    }

    /**
     * Returns list of CurrentUser.liked_posts
     */
    public static ArrayList<PrismPost> getUserLikes() {
        return liked_posts;
    }

    /**
     * Returns list of CurrentUser.reposted_posts
     */
    public static ArrayList<PrismPost> getUserReposts() {
        return reposted_posts;
    }

    public static ArrayList<PrismPost> getUserUploadsAndReposts() {
        return uploaded_and_reposted_posts;
    }

    /**
     * Prepares combined list of CurrentUser's uploaded
     * and reposted prismPosts
     */
    static void combineUploadsAndReposts() {
        uploaded_and_reposted_posts.addAll(uploaded_posts);
        uploaded_and_reposted_posts.addAll(reposted_posts);

        Collections.sort(uploaded_and_reposted_posts, new Comparator<PrismPost>() {
            @Override
            public int compare(PrismPost p1, PrismPost p2) {
                return Long.compare(p1.getTimestamp(), p2.getTimestamp());
            }
        });
    }

    /**
     * Returns list of uid of Current user's followers
     */
    static ArrayList<String> getFollowers() {
        return new ArrayList<>(followers.keySet());
    }

    /**
     * Returns list of uid of Current user's followings
     */
    static ArrayList<String> getFollowings() {
        return new ArrayList<>(followings.keySet());
    }

    /**
     *
     * @return
     */
    public static ArrayList<Notification> getNotifications() {
        return notifications;
    }

    public static PrismUser getPrismUser() {
        return prismUser;
    }

    public static void setPrismUser(PrismUser prismUser) {
        CurrentUser.prismUser = prismUser;
    }

    public static String getUid() {
        return getPrismUser().getUid();
    }

    /**
     *
     * @return
     */
    public static FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     *
     * @return
     */
    public static boolean isUserSignedIn() {
        FirebaseUser user = getFirebaseUser();
        if (user == null) {
            return false;
        }
        if (!user.isEmailVerified()) {
            performSignOut();
            return false;
        }
        return true;
    }


    /**
     *
     */
    public static void performSignOut() {

        IncomingNotificationController.clearNotifications();

        CurrentUser.prismUser = null;
        CurrentUser.preference = null;

        FirebaseAuth.getInstance().signOut();

//       CurrentUser.news_feed = null;  // TODO should do this?

    }

}
