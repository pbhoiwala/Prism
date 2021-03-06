package com.mikechoch.prism.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.constant.Default;
import com.mikechoch.prism.fragment.LikedPostsFragment;
import com.mikechoch.prism.fragment.UploadedRepostedPostsFragment;

import java.util.ArrayList;

public class ProfileViewPagerAdapter extends FragmentStatePagerAdapter {

    private int NUM_ITEMS = Default.USER_POSTS_VIEW_PAGER_SIZE;
    private ArrayList<Fragment> fragments;
    private ArrayList<PrismPost> userUploadedRepostedPosts;
    private ArrayList<PrismPost> userLikedPosts;


    public ProfileViewPagerAdapter(FragmentManager fragmentManager, ArrayList<PrismPost> uploadedAndReposted, ArrayList<PrismPost> liked) {
        super(fragmentManager);
        fragments = new ArrayList<>();
        this.userUploadedRepostedPosts = uploadedAndReposted;
        this.userLikedPosts = liked;
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                UploadedRepostedPostsFragment uploadedRepostedPostsFragment = UploadedRepostedPostsFragment.newInstance();
                uploadedRepostedPostsFragment.setUserUploadedAndRepostedPosts(userUploadedRepostedPosts);
                fragments.add(uploadedRepostedPostsFragment);
                return uploadedRepostedPostsFragment;
            case 1:
                LikedPostsFragment likedPostsFragment = LikedPostsFragment.newInstance();
                likedPostsFragment.setUserLikedPosts(userLikedPosts);
                fragments.add(likedPostsFragment);
                return likedPostsFragment;
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    /**
     *
     */
    public void refreshViewPagerTabs() {
        for (Fragment fragment : fragments) {
            if (fragment instanceof UploadedRepostedPostsFragment) {
                ((UploadedRepostedPostsFragment) fragment).getPrismPostStaggeredGridRecyclerView().refreshStaggeredRecyclerViews();
            } else if (fragment instanceof LikedPostsFragment) {
                ((LikedPostsFragment) fragment).getPrismPostStaggeredGridRecyclerView().refreshStaggeredRecyclerViews();
            }
        }
    }
}
