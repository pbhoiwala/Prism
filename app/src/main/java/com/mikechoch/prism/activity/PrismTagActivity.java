package com.mikechoch.prism.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.mikechoch.prism.R;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.attribute.PrismUser;
import com.mikechoch.prism.constant.Default;
import com.mikechoch.prism.constant.Message;
import com.mikechoch.prism.fire.CurrentUser;
import com.mikechoch.prism.helper.Helper;
import com.mikechoch.prism.user_interface.InterfaceAction;
import com.mikechoch.prism.user_interface.PrismPostStaggeredGridRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mikechoch on 2/16/18.
 */

public class PrismTagActivity extends AppCompatActivity {

    /*
     * Globals
     */
    private StorageReference storageReference;
    private DatabaseReference currentUserReference;
    private DatabaseReference usersReference;
    private DatabaseReference allPostsReference;
    private DatabaseReference tagsReference;

    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    private TextView toolbarTagNameTextView;
    private SwipeRefreshLayout tagSwipeRefreshLayout;
    private NestedScrollView tagNestedScrollView;
    private TextView tagNameTextView;
    private ImageView tagPicImageView;
    private TextView postsCountTextView;
    private TextView postsLabelTextView;

    private String tag;
    private ArrayList<PrismPost> prismTagPostsArrayList;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu., menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prism_tag_activity_layout);

        // Initialize all Firebase references
        storageReference = Default.STORAGE_REFERENCE;
        currentUserReference = Default.USERS_REFERENCE.child(CurrentUser.prismUser.getUid());
        usersReference = Default.USERS_REFERENCE;
        allPostsReference = Default.ALL_POSTS_REFERENCE;
        tagsReference = Default.TAGS_REFERENCE;

        // Initialize all toolbar elements
        toolbar = findViewById(R.id.prism_tag_toolbar);
        appBarLayout = findViewById(R.id.prism_tag_app_bar_layout);

        // Initialize all UI elements
        tagSwipeRefreshLayout = findViewById(R.id.prism_tag_swipe_refresh_layout);
        tagNestedScrollView = findViewById(R.id.prism_tag_nested_scroll_view);
        toolbarTagNameTextView = findViewById(R.id.toolbar_tag_name_text_view);
        tagNameTextView = findViewById(R.id.prism_tag_name_text_view);
        tagPicImageView = findViewById(R.id.prism_tag_picture_image_view);
        postsCountTextView = findViewById(R.id.prism_tag_posts_count_text_view);
        postsLabelTextView = findViewById(R.id.prism_tag_posts_label_text_view);

        prismTagPostsArrayList = new ArrayList<>();

        // Get prismUser associated with this profile page from Intent
        Intent intent = getIntent();
        tag = intent.getStringExtra(Default.CLICKED_TAG_EXTRA);


        tagsReference.child(tag).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HashMap<String, Long> listOfPrismPosts = new HashMap<>();
                    listOfPrismPosts.putAll((Map) dataSnapshot.getValue());
                    fetchPrismPostDetails(listOfPrismPosts);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setupUIElements();
    }

    private void fetchPrismPostDetails(HashMap<String, Long> listOfPrismPostIds) {
        allPostsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (String postId : listOfPrismPostIds.keySet()) {
                        if (dataSnapshot.hasChild(postId)) {
                            DataSnapshot postSnapshot = dataSnapshot.child(postId);
                            PrismPost prismPost = Helper.constructPrismPostObject(postSnapshot);
                            prismTagPostsArrayList.add(prismPost);
                        } else {
                            Log.e(Default.TAG_DB, Message.POST_NOT_EXIST);
                            Log.e(Default.TAG_DB, postId);
                        }
                    }
                    String tagPostCount = String.valueOf(prismTagPostsArrayList.size());
                    postsCountTextView.setText(tagPostCount);
                    String tagCountLabel = Helper.getSingularOrPluralText("post", prismTagPostsArrayList.size());
                    postsLabelTextView.setText(tagCountLabel);
                    fetchPrismUserDetailsForPrismPosts();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void fetchPrismUserDetailsForPrismPosts() {
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (PrismPost prismPost : prismTagPostsArrayList) {
                        String userId = prismPost.getUid();
                        if (dataSnapshot.hasChild(userId)) {
                            DataSnapshot userSnapshot = dataSnapshot.child(userId);
                            PrismUser prismUser = Helper.constructPrismUserObject(userSnapshot);
                            prismPost.setPrismUser(prismUser);
                        } else {
                            Log.e(Default.TAG_DB, Message.USER_NOT_EXIST);
                            Log.e(Default.TAG_DB, userId);
                        }
                    }

                    Collections.sort(prismTagPostsArrayList, new Comparator<PrismPost>() {
                        @Override
                        public int compare(PrismPost p1, PrismPost p2) {
                            return (int) (p1.getTimestamp() - p2.getTimestamp());
                        }
                    });
                }
                setupTagPage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Setup the toolbar and back button to return to MainActivity
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Setup the AppBarLayout knowing when it is fully collapsed/ expanded
     * The percentage of the collapsed will be used to
     * set the alpha of the toolbar and collapsingToolbar elements
     */
    private void setupAppBarLayout() {
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            private int scrollRange = -1;

            @Override
            public void onOffsetChanged(final AppBarLayout appBarLayout, int verticalOffset) {
                //Initialize the size of the scroll
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                float toolbarElementsAlpha = Math.abs(verticalOffset/ ((float) scrollRange));
                toolbarTagNameTextView.setAlpha(toolbarElementsAlpha);

                // Check if the view is collapsed
                if (scrollRange + verticalOffset == 0) {

                } else {

                }
            }
        });
    }

    /**
     *
     */
    private void setupTagPage() {
        tagSwipeRefreshLayout.setColorSchemeResources(InterfaceAction.swipeRefreshLayoutColors);
        tagSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tagSwipeRefreshLayout.setRefreshing(false);
            }
        });

        LinearLayout tagPostsLinearLayout = this.findViewById(R.id.tag_posts_linear_layout);
        new PrismPostStaggeredGridRecyclerView(this, tagPostsLinearLayout, prismTagPostsArrayList);
        tagPostsLinearLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Setup all UI elements
     */
    private void setupUIElements() {
        setupToolbar();

        // Setup Typefaces for all text based UI elements
        toolbarTagNameTextView.setTypeface(Default.sourceSansProBold);
        tagNameTextView.setTypeface(Default.sourceSansProBold);
        postsCountTextView.setTypeface(Default.sourceSansProBold);
        postsLabelTextView.setTypeface(Default.sourceSansProLight);

        setupAppBarLayout();

        String tagString = "#" + tag;
        toolbarTagNameTextView.setText(tagString);
        tagNameTextView.setText(tagString);
    }

}
