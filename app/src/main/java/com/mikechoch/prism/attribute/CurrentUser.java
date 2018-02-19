package com.mikechoch.prism.attribute;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikechoch.prism.R;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.constants.Message;
import com.mikechoch.prism.helper.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by parth on 1/25/18.
 */

public class CurrentUser {

    /*
     * Globals
     */
    private static FirebaseAuth auth;
    public static FirebaseUser firebaseUser;
    private static DatabaseReference userReference;
    private static DatabaseReference allPostReference;

    private static Context context;
    private float scale;

    // Key: String postId
    // Value: long timestamp
    public static ArrayList<PrismPost> user_liked_posts;
    public static HashMap user_liked_posts_map;
    public static ArrayList<PrismPost> user_reposted_posts;
    public static HashMap user_reposted_posts_map;

    public static ArrayList<PrismPost> user_uploaded_posts;
    private static HashMap user_uploaded_posts_map;

    public static PrismUser prismUser;


    public CurrentUser(Context context) {
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        userReference = Default.USERS_REFERENCE.child(firebaseUser.getUid());
        allPostReference = Default.ALL_POSTS_REFERENCE;

        CurrentUser.context = context;
        scale = context.getResources().getDisplayMetrics().density;

        refreshUserLinkedPosts();
        getUserProfileDetails();
    }

    /**
     * Refreshes list of liked, reposted and uploaded posts by current firebaseUser
     */
    public static void refreshUserLinkedPosts() {
        refreshUserLikedPosts();
        refreshUserRepostedPosts();
        refreshUserUploadedPosts();
    }

    /**
     * Pulls current firebaseUser's list of liked posts and puts them in a HashMap
     */
    public static void refreshUserLikedPosts() {
        user_liked_posts = new ArrayList<>();
        user_liked_posts_map = new HashMap<String, Long>();
        userReference.child(Key.DB_REF_USER_LIKES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user_liked_posts_map.putAll((Map) dataSnapshot.getValue());
                    allPostReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (Object key : user_liked_posts_map.keySet()) {
                                    String postId = (String) key;
                                    DataSnapshot postSnapshot = dataSnapshot.child(postId);
                                    if (postSnapshot.exists()) {
                                        PrismPost prismPost = Helper.constructPrismPostObject(postSnapshot);
                                        prismPost.setPrismUser(CurrentUser.prismUser);
                                        user_liked_posts.add(prismPost);
                                    }
                                }
                            } else {
                                Log.wtf(Default.TAG_DB, Message.NO_DATA);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(Default.TAG_DB, Message.FETCH_POST_INFO_FAIL, databaseError.toException());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Default.TAG_DB, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    /**
     * Pulls current firebaseUser's list of reposted posts and puts them in a HashMap
     */
    public static void refreshUserRepostedPosts() {
        user_reposted_posts = new ArrayList<>();
        user_reposted_posts_map = new HashMap<String, Long>();
        userReference.child(Key.DB_REF_USER_REPOSTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user_reposted_posts_map.putAll((Map) dataSnapshot.getValue());
                    allPostReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (Object key : user_reposted_posts_map.keySet()) {
                                    String postId = (String) key;
                                    DataSnapshot postSnapshot = dataSnapshot.child(postId);
                                    if (postSnapshot.exists()) {
                                        PrismPost prismPost = Helper.constructPrismPostObject(postSnapshot);
                                        prismPost.setPrismUser(CurrentUser.prismUser);
                                        user_reposted_posts.add(prismPost);
                                    }
                                }
                            } else {
                                Log.wtf(Default.TAG_DB, Message.NO_DATA);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(Default.TAG_DB, Message.FETCH_POST_INFO_FAIL, databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Default.TAG_DB, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    /**
     * TODO: convert items to PrismPost or something for User Profile Page
     */
    public static void refreshUserUploadedPosts() {
        user_uploaded_posts = new ArrayList<>();
        user_uploaded_posts_map = new HashMap<String, Long>();
        userReference.child(Key.DB_REF_USER_UPLOADS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user_uploaded_posts_map.putAll((Map) dataSnapshot.getValue());
                    allPostReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (Object key : user_uploaded_posts_map.keySet()) {
                                    String postId = (String) key;
                                    DataSnapshot postSnapshot = dataSnapshot.child(postId);
                                    if (postSnapshot.exists()) {
                                        PrismPost prismPost = Helper.constructPrismPostObject(postSnapshot);
                                        prismPost.setPrismUser(CurrentUser.prismUser);
                                        user_uploaded_posts.add(prismPost);
                                    }
                                }
                            } else {
                                Log.wtf(Default.TAG_DB, Message.NO_DATA);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(Default.TAG_DB, Message.FETCH_POST_INFO_FAIL, databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Default.TAG_DB, databaseError.getMessage(), databaseError.toException());
            }
        });
    }


    /**
     * Gets firebaseUser's profile details such as full name, username,
     * and link to profile pic uri
     */
    public void getUserProfileDetails() {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    prismUser = new PrismUser();
                    prismUser.setUid(firebaseUser.getUid());
                    prismUser.setFullName((String) dataSnapshot.child(Key.USER_PROFILE_FULL_NAME).getValue());
                    prismUser.setUsername((String) dataSnapshot.child(Key.USER_PROFILE_USERNAME).getValue());
                    prismUser.setProfilePicture(new ProfilePicture((String)
                            dataSnapshot.child(Key.USER_PROFILE_PIC).getValue()));

                    updateUserProfilePageUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Default.TAG_DB, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    /**
     * Updates UI elements (such as profilePic, username, fullName) on User Profile Page.
     * I don't like that this function is here but it needs to be this way :(
     */
    private void updateUserProfilePageUI() {
        ImageView userProfileImageView = ((Activity) context).findViewById(R.id.profile_fragment_user_profile_image_view);
        TextView userProfileTextView = ((Activity) context).findViewById(R.id.profile_fragment_user_full_name_text_view);

        userProfileTextView.setText(prismUser.getFullName());
        Glide.with(context)
                .asBitmap()
                .thumbnail(0.05f)
                .load(prismUser.getProfilePicture().lowResUri)
                .apply(new RequestOptions().fitCenter())
                .into(new BitmapImageViewTarget(userProfileImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        if (!prismUser.getProfilePicture().isDefault) {
                            int whiteOutlinePadding = (int) (1 * scale);
                            userProfileImageView.setPadding(whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding);
                            userProfileImageView.setBackground(context.getResources().getDrawable(R.drawable.circle_profile_frame));
                        } else {
                            userProfileImageView.setPadding(0, 0, 0, 0);
                            userProfileImageView.setBackground(null);
                        }

                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        drawable.setCircular(true);
                        userProfileImageView.setImageDrawable(drawable);
                    }
                });
    }
}
