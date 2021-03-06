package com.mikechoch.prism.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikechoch.prism.R;
import com.mikechoch.prism.attribute.LinkedPrismPosts;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.callback.fetch.OnFetchPrismPostsCallback;
import com.mikechoch.prism.constant.Default;
import com.mikechoch.prism.constant.Message;
import com.mikechoch.prism.fire.DatabaseRead;
import com.mikechoch.prism.helper.Helper;
import com.mikechoch.prism.user_interface.InterfaceAction;
import com.mikechoch.prism.user_interface.PrismPostStaggeredGridRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;


public class PrismTagActivity extends AppCompatActivity {

    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    private TextView toolbarTagNameTextView;
    private SwipeRefreshLayout tagSwipeRefreshLayout;
    private NestedScrollView tagNestedScrollView;
    private LinearLayout tagPostsLinearLayout;
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
                onBackPressed();
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

        toolbar = findViewById(R.id.prism_tag_toolbar);
        appBarLayout = findViewById(R.id.prism_tag_app_bar_layout);
        tagSwipeRefreshLayout = findViewById(R.id.prism_tag_swipe_refresh_layout);
        tagNestedScrollView = findViewById(R.id.prism_tag_nested_scroll_view);
        tagPostsLinearLayout = findViewById(R.id.tag_posts_linear_layout);
        toolbarTagNameTextView = findViewById(R.id.toolbar_tag_name_text_view);
        tagNameTextView = findViewById(R.id.prism_tag_name_text_view);
        tagPicImageView = findViewById(R.id.prism_tag_picture_image_view);
        postsCountTextView = findViewById(R.id.prism_tag_posts_count_text_view);
        postsLabelTextView = findViewById(R.id.prism_tag_posts_label_text_view);

        prismTagPostsArrayList = new ArrayList<>();

        setupInterfaceElements();
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
     * Count the size of the tag PrismPost ArrayList and set the posts count TextView
     * Handle the singular or plural "post" String
     */
    private void setupTagPageLayout() {
        String tagPostCount = String.valueOf(prismTagPostsArrayList.size());
        postsCountTextView.setText(tagPostCount);
        String tagCountLabel = Helper.getSingularOrPluralText("post", prismTagPostsArrayList.size());
        postsLabelTextView.setText(tagCountLabel);

        new PrismPostStaggeredGridRecyclerView(this, tagPostsLinearLayout, prismTagPostsArrayList);
        tagPostsLinearLayout.setVisibility(View.VISIBLE);

        tagSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Add the tag to the title TextView and also
     */
    private void setupTagTitleTextViews() {
        // Get prismUser associated with this profile page from Intent
        Intent intent = getIntent();
        tag = intent.getStringExtra(Default.CLICKED_TAG_EXTRA);

        String tagString = "#" + tag;
        toolbarTagNameTextView.setText(tagString);
        tagNameTextView.setText(tagString);
    }

    /**
     * Setup the swipe refresh layout so that it updates all PrismPosts for the specific
     * tag of the page
     */
    private void setupSwipeRefreshLayout() {
        tagSwipeRefreshLayout.setColorSchemeColors(InterfaceAction.swipeRefreshLayoutColors);
        tagSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullTagPrismPosts();
            }
        });
    }

    /**
     * Pull all PrismPosts from Firebase for the corresponding tag
     */
    private void pullTagPrismPosts() {
        tagPostsLinearLayout.setVisibility(View.GONE);
        tagSwipeRefreshLayout.setRefreshing(true);
        DatabaseRead.fetchPrismPostsForTag(tag, new OnFetchPrismPostsCallback() {
            @Override
            public void onSuccess(LinkedPrismPosts linkedPrismPosts) {
                prismTagPostsArrayList.clear();
                prismTagPostsArrayList.addAll(linkedPrismPosts.getPrismPosts());
                setupTagPageLayout();
            }

            @Override
            public void onPrismPostsNotFound() {
                Helper.toast(PrismTagActivity.this, Message.PRISM_POST_NOT_FOUND);
                tagSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Exception e) {
                Helper.toast(PrismTagActivity.this, Message.FETCH_POST_INFO_FAIL);
                tagSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    /**
     * Setup elements of current activity
     */
    private void setupInterfaceElements() {
        toolbarTagNameTextView.setTypeface(Default.sourceSansProBold);
        tagNameTextView.setTypeface(Default.sourceSansProBold);
        postsCountTextView.setTypeface(Default.sourceSansProBold);
        postsLabelTextView.setTypeface(Default.sourceSansProLight);

        setupToolbar();
        setupAppBarLayout();
        setupTagTitleTextViews();
        setupSwipeRefreshLayout();
        pullTagPrismPosts();
    }
}
