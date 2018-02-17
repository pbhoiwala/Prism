package com.mikechoch.prism.attribute;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.mikechoch.prism.adapter.UserUploadedPostsRecyclerViewAdapter;
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

    private static Context context;
    private static FirebaseAuth auth;
    public static FirebaseUser firebaseUser;
    private static DatabaseReference userReference;
    private static DatabaseReference allPostReference;

    // Key: String postId
    // Value: long timestamp
    public static HashMap user_liked_posts;
    public static HashMap user_reposted_posts;

    public static ArrayList<PrismPost> user_uploaded_posts;
    private static HashMap user_uploaded_posts_map;

    public static PrismUser prismUser;


    public CurrentUser(Context context) {

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        userReference = Default.USERS_REFERENCE.child(firebaseUser.getUid());
        allPostReference = Default.ALL_POSTS_REFERENCE;

        CurrentUser.context = context;

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
        user_liked_posts = new HashMap<String, Long>();
        userReference.child(Key.DB_REF_USER_LIKES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user_liked_posts.putAll((Map) dataSnapshot.getValue());
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
        user_reposted_posts = new HashMap<String, Long>();
        userReference.child(Key.DB_REF_USER_REPOSTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user_reposted_posts.putAll((Map) dataSnapshot.getValue());
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

                                LinearLayout userUploadedPostsLinearLayout = ((Activity) context).findViewById(R.id.user_uploaded_posts_linear_layout);
                                userUploadedPostsLinearLayout.removeAllViews();
                                userUploadedPostsLinearLayout.setWeightSum((float) Default.USER_UPLOADED_POSTS_COLUMNS);

//                                ArrayList<ArrayList<PrismPost>> userUploadedPostsArrayLists = new ArrayList<>(Collections.nCopies(userUploadedColumns, new ArrayList<>()));
                                // TODO: figure out how to initialize an ArrayList of ArrayLists without using while loop inside of populating for-loop
                                ArrayList<ArrayList<PrismPost>> userUploadedPostsArrayLists = new ArrayList<>();
                                for (int i = 0; i < user_uploaded_posts.size(); i++) {
                                    while (userUploadedPostsArrayLists.size() != Default.USER_UPLOADED_POSTS_COLUMNS) {
                                        userUploadedPostsArrayLists.add(new ArrayList<>());
                                    }
                                    userUploadedPostsArrayLists.get((i % Default.USER_UPLOADED_POSTS_COLUMNS)).add(user_uploaded_posts.get(i));
                                }

                                for (int i = 0; i < Default.USER_UPLOADED_POSTS_COLUMNS; i++) {
                                    LinearLayout recyclerViewLinearLayout = new LinearLayout(context);
                                    LinearLayout.LayoutParams one_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,1f);
                                    recyclerViewLinearLayout.setLayoutParams(one_params);

                                    RecyclerView userUploadedPostsRecyclerView = (RecyclerView) LayoutInflater.from(context).inflate(R.layout.user_uploaded_posts_recycler_view_layout, null);
                                    LinearLayoutManager recyclerViewLinearLayoutManager = new LinearLayoutManager(context);
                                    userUploadedPostsRecyclerView.setLayoutManager(recyclerViewLinearLayoutManager);
                                    UserUploadedPostsRecyclerViewAdapter recyclerViewAdapter = new UserUploadedPostsRecyclerViewAdapter(context, userUploadedPostsArrayLists.get(i));
                                    userUploadedPostsRecyclerView.setAdapter(recyclerViewAdapter);

                                    recyclerViewLinearLayout.addView(userUploadedPostsRecyclerView);
                                    userUploadedPostsLinearLayout.addView(recyclerViewLinearLayout);
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
        ImageView userProfilePicImageView = ((Activity) context).findViewById(R.id.profile_frag_profile_picture_image_view);
        TextView userUsernameTextView = ((Activity) context).findViewById(R.id.profile_frag_username_text_view);
        TextView userFullNameTextView = ((Activity) context).findViewById(R.id.profile_frag_full_name_text_view);
        TextView postsCountTextView = ((Activity) context).findViewById(R.id.posts_count_text_view);
        TextView followersCountTextView = ((Activity) context).findViewById(R.id.followers_count_text_view);
        TextView followingCountTextView = ((Activity) context).findViewById(R.id.following_count_text_view);

        userUsernameTextView.setText(prismUser.getUsername());
        userFullNameTextView.setText(prismUser.getFullName());
        followersCountTextView.setText(String.valueOf(0));
        postsCountTextView.setText(String.valueOf(user_uploaded_posts.size()));
        followingCountTextView.setText(String.valueOf(0));

        Glide.with(context)
                .asBitmap()
                .thumbnail(0.05f)
                .load(prismUser.getProfilePicture().hiResUri)
                .apply(new RequestOptions().fitCenter())
                .into(new BitmapImageViewTarget(userProfilePicImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        if (!prismUser.getProfilePicture().isDefault) {
                            float scale = context.getResources().getDisplayMetrics().density;
                            int whiteOutlinePadding = (int) (2 * scale);
                            userProfilePicImageView.setPadding(whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding);
                            userProfilePicImageView.setBackground(context.getResources().getDrawable(R.drawable.circle_profile_frame));
                        }

                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        drawable.setCircular(true);
                        userProfilePicImageView.setImageDrawable(drawable);
                    }
                });
    }
}