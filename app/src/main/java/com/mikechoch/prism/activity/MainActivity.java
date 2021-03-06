package com.mikechoch.prism.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.mikechoch.prism.R;
import com.mikechoch.prism.activity.util.NetworkStateReceiver;
import com.mikechoch.prism.adapter.MainViewPagerAdapter;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.callback.action.OnUploadPostCallback;
import com.mikechoch.prism.constant.Default;
import com.mikechoch.prism.constant.Message;
import com.mikechoch.prism.fire.CurrentUser;
import com.mikechoch.prism.fire.DatabaseAction;
import com.mikechoch.prism.fragment.MainFeedFragment;
import com.mikechoch.prism.fragment.NotificationFragment;
import com.mikechoch.prism.helper.BitmapHelper;
import com.mikechoch.prism.helper.Helper;
import com.mikechoch.prism.helper.IntentHelper;
import com.mikechoch.prism.type.MainViewPagerTab;
import com.mikechoch.prism.user_interface.InterfaceAction;


public class MainActivity extends FragmentActivity implements NetworkStateReceiver.NetworkStateReceiverListener {

    private AppBarLayout.LayoutParams params;

    private Toolbar toolbar;
    private TextView toolbarTextView;

    private CoordinatorLayout mainCoordinateLayout;
    private TabLayout prismTabLayout;
    private ViewPager prismViewPager;
    private ImageView imageUploadPreview;
    private TextView uploadingImageTextView;
    private RelativeLayout prismDecorationRelativeLayout;
    private RelativeLayout uploadingImageRelativeLayout;
    private FloatingActionButton uploadImageFab;
    private ProgressBar imageUploadProgressBar;
    private Snackbar networkSnackBar;
    private NetworkStateReceiver networkStateReceiver;

    private Uri uploadedImageUri;
    private String uploadedImageDescription;

    private Handler clearNotificationsHandler;
    private Runnable clearNotificationsRunnable;
    private boolean shouldClearNotifications = false;

    public static String FCM_API_KEY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        AndroidNetworking.initialize(this);

        FCM_API_KEY = getFirebaseKey();

        // This is a safety check to make sure that user is signed in properly
        if (!CurrentUser.isUserSignedIn()) {
            IntentHelper.intentToLoginActivity(MainActivity.this);
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        toolbarTextView = findViewById(R.id.prism_toolbar_title);
        params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

        mainCoordinateLayout = findViewById(R.id.main_coordinate_layout);
        prismTabLayout = findViewById(R.id.prism_tab_layout);
        prismViewPager = findViewById(R.id.prism_view_pager);
        imageUploadPreview = findViewById(R.id.image_upload_preview);
        uploadingImageTextView = findViewById(R.id.uploading_image_text_view);
        prismDecorationRelativeLayout = findViewById(R.id.prism_toolbar_decoration);
        uploadingImageRelativeLayout = findViewById(R.id.uploading_image_relative_layout);
        uploadImageFab = findViewById(R.id.upload_image_fab);
        imageUploadProgressBar = findViewById(R.id.image_upload_progress_bar);

        initializeNetworkListener();
        setupInterfaceElements();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * onDestroy is a method that gets invoked when OS tries to kill the activity
     * This is the last chance for app to finalize closing activities before app
     * gets shut down
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

    /**
     * When a permission is allowed, this function will run and you can
     * Check for this allow and do something
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Default.MY_PERMISSIONS_WRITE_MEDIA_REQUEST_CODE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Code here for allowing write permission
                }
                break;
            case Default.MY_PERMISSIONS_CAMERA_REQUEST_CODE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Code here for allowing write permission
                }
                break;
            default:
                break;
        }
    }

    /**
     * Check the incoming Intent for an upload image intent key
     * If the Boolean Extra is true then an upload will be initialized
     */
    private void checkForImageUploadIntent() {
        Intent uploadIntent = getIntent();
        if (uploadIntent.getBooleanExtra(Default.UPLOAD_IMAGE_INTENT_KEY, false)) {
            setupUploadPrismPostToFirebase(uploadIntent);
        }
    }

    /**
     * Method to begin uploading a PrismPost into Firebase
     * Setup upload image ProgressBar and preview ImageView
     * Than begin process of uploading to Firebase
     * @param uploadIntent - Intent to obtain the PrismPost info to upload with
     */
    private void setupUploadPrismPostToFirebase(Intent uploadIntent) {
        uploadingImageTextView.setText(Message.POST_UPLOAD_UPLOADING_IMAGE);
        imageUploadProgressBar.setProgress(0);
        imageUploadProgressBar.setIndeterminate(false);
        uploadingImageRelativeLayout.setVisibility(View.VISIBLE);
        prismDecorationRelativeLayout.setVisibility(View.GONE);

        params.setScrollFlags(0);
        toolbar.setLayoutParams(params);

        uploadedImageUri = Uri.parse(uploadIntent.getStringExtra(Default.IMAGE_URI_EXTRA));
        uploadedImageDescription = uploadIntent.getStringExtra(Default.IMAGE_DESCRIPTION_EXTRA);

        Glide.with(this)
                .asBitmap()
                .thumbnail(0.05f)
                .load(uploadedImageUri)
                .apply(new RequestOptions().centerCrop())
                .into(new BitmapImageViewTarget(imageUploadPreview) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable roundedProfilePicture = RoundedBitmapDrawableFactory.create(getResources(), resource);
                        roundedProfilePicture.setCircular(true);
                        imageUploadPreview.setImageDrawable(roundedProfilePicture);
                    }
                });
        beginUploadPrismPostToFirebase();
    }

    /**
     * Give PageChangeListener control to TabLayout
     * Create MainViewPagerAdapter and set it for the ViewPager
     */
    private void setupPrismViewPager() {
        prismViewPager.setOffscreenPageLimit(Default.MAIN_VIEW_PAGER_SIZE);
        prismViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(prismTabLayout));
        MainViewPagerAdapter prismViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());
        prismViewPager.setAdapter(prismViewPagerAdapter);
        prismTabLayout.setupWithViewPager(prismViewPager);
    }

    /**
     * Setup for the TabLayout
     * Give each tab an icon and set the listener for selecting, reselecting, and unselecting
     * Selected tabs will be a ColorAccent and unselected tabs White
     */
    private void setupPrismTabLayout() {
        // Create the selected and unselected tab icon colors
        int tabUnselectedColor = Color.WHITE;
        int tabSelectedColor = getResources().getColor(R.color.colorAccent);

        // Setup all TabLayout tab icons
        for (MainViewPagerTab tab : MainViewPagerTab.values()) {
            int tabId = tab.getId();
            TabLayout.Tab tabLayoutTab = prismTabLayout.getTabAt(tabId);

            // Make first tab selected color and all others unselected
            if (tabLayoutTab != null) {
                Drawable tabIcon = getResources().getDrawable(tab.getIcon());
                switch (tabId) {
                    case Default.MAIN_VIEW_PAGER_MAIN_FEED:
                        tabIcon.setColorFilter(tabSelectedColor, PorterDuff.Mode.SRC_IN);
                        break;
                    default:
                        tabIcon.setColorFilter(tabUnselectedColor, PorterDuff.Mode.SRC_IN);
                        break;
                }
                tabLayoutTab.setIcon(tabIcon.mutate());
            }
        }

        // Setup the tab selected, unselected, and reselected listener
        // TODO should we replace below deprecated `setOnTabSelected` with `addOnTabSelected` ?
        prismTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
//                params.setScrollFlags(isUploadingImage ?
//                        0 : AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
//                        AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
//                        AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
                // Sets the selected tab to the selected color and
                // If at the HOME tab the uploadImageFab will be shown
                // Otherwise, the uploadImageFab will be hidden
                tab.getIcon().setColorFilter(tabSelectedColor, PorterDuff.Mode.SRC_IN);
                prismViewPager.setCurrentItem(tab.getPosition(), true);
                if (tab.getPosition() <= Default.MAIN_VIEW_PAGER_MAIN_FEED && !uploadImageFab.isShown()) {
//                    toolbar.setLayoutParams(params);
                    uploadImageFab.startAnimation(createFabShowAnimation(false));
                } else if (tab.getPosition() > Default.MAIN_VIEW_PAGER_MAIN_FEED && uploadImageFab.isShown()) {
//                    params.setScrollFlags(0);
//                    toolbar.setLayoutParams(params);
                    uploadImageFab.startAnimation(createFabShowAnimation(true));
                }

                // Switch statement handing reselected tabs
                int tabPosition = tab.getPosition();
                switch (tabPosition) {
                    // HOME tab will...
                    case Default.MAIN_VIEW_PAGER_MAIN_FEED:
                        break;

                    // SEARCH tab will...
                    case Default.MAIN_VIEW_PAGER_SEARCH:
                        break;

                    // NOTIFICATIONS tab will...
                    case Default.MAIN_VIEW_PAGER_NOTIFICATIONS:
                        clearNotificationsHandler = new Handler();
                        clearNotificationsRunnable = new Runnable() {
                            @Override
                            public void run() {
                                shouldClearNotifications = true;
                            }
                        };
                        clearNotificationsHandler.postDelayed(clearNotificationsRunnable, 2000);
                        break;

                    // PROFILE tab will...
                    case Default.MAIN_VIEW_PAGER_PROFILE:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Set the tab unselected to the unselected color
                tab.getIcon().setColorFilter(tabUnselectedColor, PorterDuff.Mode.SRC_IN);

                // Switch statement handing reselected tabs
                int tabPosition = tab.getPosition();
                switch (tabPosition) {
                    // HOME tab will...
                    case Default.MAIN_VIEW_PAGER_MAIN_FEED:
                        break;

                    // SEARCH tab will...
                    case Default.MAIN_VIEW_PAGER_SEARCH:
                        break;

                    // NOTIFICATIONS tab will set all notifications isViewed to true
                    case Default.MAIN_VIEW_PAGER_NOTIFICATIONS:
                        if (shouldClearNotifications) {
                            NotificationFragment.clearAllNotifications();
                            DatabaseAction.updateViewedTimestampForAllNotifications();
                            RecyclerView notificationRecyclerView = MainActivity.this.findViewById(R.id.notification_recycler_view);
                            if (notificationRecyclerView != null) {
                                notificationRecyclerView.getAdapter().notifyDataSetChanged();
                            }
                            clearNotificationsHandler.removeCallbacks(clearNotificationsRunnable);
                        }
                        shouldClearNotifications = false;
                        break;

                    // PROFILE tab will...
                    case Default.MAIN_VIEW_PAGER_PROFILE:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Switch statement handing reselected tabs
                int tabPosition = tab.getPosition();
                switch (tabPosition) {
                    // HOME tab will bring the user back to the top of the mainContentRecyclerView
                    case Default.MAIN_VIEW_PAGER_MAIN_FEED:
                        RecyclerView mainContentRecyclerView = MainActivity.this
                                .findViewById(R.id.main_content_recycler_view);
                        if (mainContentRecyclerView != null) {
                            LinearLayoutManager layoutManager  = (LinearLayoutManager)
                                    mainContentRecyclerView.getLayoutManager();
                            if (layoutManager.findFirstVisibleItemPosition() < 10) {
                                mainContentRecyclerView.smoothScrollToPosition(0);
                            } else {
                                mainContentRecyclerView.scrollToPosition(0);
                            }
                        }
                        break;

                    // SEARCH tab will...
                    case Default.MAIN_VIEW_PAGER_SEARCH:
                        break;

                    // NOTIFICATIONS tab will...
                    case Default.MAIN_VIEW_PAGER_NOTIFICATIONS:
                        break;

                    // PROFILE tab will...
                    case Default.MAIN_VIEW_PAGER_PROFILE:
                        break;

                    default:
                        break;
                }
            }
        });
    }

    /**
     * Setup the UploadImageFab, so when it is clicked it will Intent to UploadImageActivity
     */
    private void setupUploadImageFab() {
        uploadImageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentHelper.intentToUploadImageSelectionActivity(MainActivity.this);
            }
        });
    }

    /**
     * Setup elements for current activity
     */
    private void setupInterfaceElements() {
        toolbarTextView.setTypeface(Default.sourceSansProBold);
        uploadingImageTextView.setTypeface(Default.sourceSansProLight);

        setupPrismViewPager();
        setupPrismTabLayout();
        setupUploadImageFab();

        TabLayout.Tab currentTab = prismTabLayout.getTabAt(prismViewPager.getCurrentItem());
        if (currentTab != null) {
            currentTab.select();
        }

        new InterfaceAction(this);

        checkForImageUploadIntent();
    }

    /**
     * Takes in a boolean shouldHide and will create a hiding and showing animation
     */
    private Animation createFabShowAnimation(boolean shouldHide) {
        float scaleFromXY = shouldHide ? 1f : 0f;
        float scaleToXY = shouldHide ? 0f : 1f;
        float pivotXY = 0.5f;
        Animation scaleAnimation  = new ScaleAnimation(scaleFromXY, scaleToXY, scaleFromXY, scaleToXY,
                Animation.RELATIVE_TO_SELF, pivotXY,
                Animation.RELATIVE_TO_SELF, pivotXY);
        scaleAnimation.setDuration(200);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                uploadImageFab.setVisibility(shouldHide ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        return scaleAnimation;
    }

    /**
     *
     */
    @SuppressLint("SimpleDateFormat")
    private void beginUploadPrismPostToFirebase() {

        DatabaseAction.uploadPost(uploadedImageUri, uploadedImageDescription, new OnUploadPostCallback() {
            @Override
            public void onSuccess(PrismPost prismPost) {
                updateLocalRecyclerViewWithNewPost(prismPost);
            }

            @Override
            public void onPermissionDenied() {
                Helper.toast(MainActivity.this, Message.POST_UPLOAD_PERMISSION_DENIED);
                CurrentUser.performSignOut();
                IntentHelper.resetApplication(MainActivity.this);
            }

            @Override
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onProgressUpdate(int progress) {
                boolean animate = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
                imageUploadProgressBar.setProgress(progress, animate);
            }

            @Override
            public void onImageUploadFail(Exception e) {
                uploadingImageTextView.setText(Message.POST_IMAGE_UPLOAD_FAIL);
                Helper.toast(MainActivity.this, Message.POST_IMAGE_UPLOAD_FAIL);
            }

            @Override
            public void onPostUploadFail(Exception e) {
                uploadingImageTextView.setText(Message.POST_UPLOAD_FAIL);
                Helper.toast(MainActivity.this, Message.POST_UPLOAD_FAIL);
            }

        });
    }

    /**
     * Takes new prismPost object that got uploaded to cloud and adds it to the recyclerViewAdapter
     * and wraps up other UI elements such as textviews and progress spinners
     * @param prismPost
     */
    private void updateLocalRecyclerViewWithNewPost(PrismPost prismPost) {
        uploadingImageTextView.setText(Message.POST_UPLOAD_FINISHING_UP);
        prismPost.setPrismUser(CurrentUser.getPrismUser());
        RecyclerView mainContentRecyclerView = MainActivity.this.findViewById(R.id.main_content_recycler_view);
        LinearLayoutManager layoutManager  = (LinearLayoutManager) mainContentRecyclerView.getLayoutManager();
        RelativeLayout noMainPostsRelativeLayout = MainActivity.this.findViewById(R.id.no_main_posts_relative_layout);
        MainFeedFragment.mainFeedPrismPostArrayList.add(0, prismPost);
        mainContentRecyclerView.getAdapter().notifyItemInserted(0);
        noMainPostsRelativeLayout.setVisibility(View.GONE);

        if (layoutManager.findFirstVisibleItemPosition() < 10) {
            mainContentRecyclerView.smoothScrollToPosition(0);
        } else {
            mainContentRecyclerView.scrollToPosition(0);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                uploadingImageTextView.setText(Message.POST_UPLOAD_DONE);
            }
        }, 1000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                uploadingImageRelativeLayout.setVisibility(View.GONE);
                prismDecorationRelativeLayout.setVisibility(View.VISIBLE);

                params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                        AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
                        AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
                toolbar.setLayoutParams(params);
            }
        }, 2000);
    }

    /**
     * Obtain the firebase cloud messaging server key from strings.xml
     * @return - String firebase cloud messaging server key for accessing Firebase
     */
    private String getFirebaseKey() {
        return getString(R.string.firebase_cloud_messaging_server_key);
    }

    /**
     * Initializes listeners for network (wifi or data) and location
     * Gets called first thing when app opens up
     */
    private void initializeNetworkListener() {
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onNetworkConnected() {
        if (networkSnackBar != null && networkSnackBar.isShownOrQueued()) {
            networkSnackBar.dismiss();
            networkSnackBar = null;
        }
    }

    @Override
    public void onNetworkDisconnected() {
        CoordinatorLayout coordinatorLayout = findViewById(R.id.main_coordinate_layout);
        networkSnackBar = Snackbar.make(coordinatorLayout, Message.NO_INTERNET,
                Snackbar.LENGTH_INDEFINITE);
        ((TextView) (networkSnackBar.getView())
                .findViewById(android.support.design.R.id.snackbar_text))
                .setTypeface(Default.sourceSansProBold);
        networkSnackBar.show();
        Helper.disableSnackBarSwipeDismiss(networkSnackBar.getView());
    }

    /**
     * This method should stay in MainActivity and only be called with Context from MainActivity
     * This will refresh the CurrentUser data in the name TextView and
     * user profile picture ImageView for ProfileFragment
     * @param context - Context from MainActivity
     */
    public static void updateProfileFragmentInterface(Context context) {
        ImageView userProfileImageView = ((Activity) context).findViewById(R.id.profile_fragment_user_profile_image_view);
        TextView userProfileTextView = ((Activity) context).findViewById(R.id.profile_fragment_user_full_name_text_view);

        if (userProfileTextView != null && userProfileImageView != null) {
            userProfileTextView.setText(CurrentUser.getPrismUser().getFullName());
            Glide.with(context)
                    .asBitmap()
                    .thumbnail(0.05f)
                    .load(CurrentUser.getPrismUser().getProfilePicture().getLowResProfilePicUri())
                    .apply(new RequestOptions().fitCenter())
                    .into(new BitmapImageViewTarget(userProfileImageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            int imageViewPadding = (int) (1 * Default.scale);
                            RoundedBitmapDrawable profilePictureDrawable =
                                    BitmapHelper.createCircularProfilePicture(
                                            context,
                                            userProfileImageView,
                                            CurrentUser.getPrismUser().getProfilePicture().isDefault(),
                                            resource,
                                            imageViewPadding);
                            userProfileImageView.setImageDrawable(profilePictureDrawable);
                        }
                    });
        }
    }
}
