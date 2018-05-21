package com.mikechoch.prism.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikechoch.prism.R;
import com.mikechoch.prism.activity.SearchActivity;
import com.mikechoch.prism.adapter.SearchDiscoverRecyclerViewAdapter;
import com.mikechoch.prism.constant.Default;


/**
 * Created by mikechoch on 1/22/18.
 */

public class SearchFragment extends Fragment {

    public static LinearLayout searchLinearLayout;
    private CardView searchCardView;
    private TextView searchBarHintTextView;

    public static SearchDiscoverRecyclerViewAdapter[] searchDiscoverRecyclerViewAdapters = new SearchDiscoverRecyclerViewAdapter[5];


    public static final SearchFragment newInstance() {
        SearchFragment searchFragment = new SearchFragment();
        return searchFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment_layout, container, false);

        searchLinearLayout = view.findViewById(R.id.search_fragment_linear_Layout);
        searchCardView = view.findViewById(R.id.search_bar_card_view);
        searchBarHintTextView = view.findViewById(R.id.search_bar_hint_text_view);
        searchBarHintTextView.setTypeface(Default.sourceSansProLight);

        searchCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                startActivity(searchIntent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        for (int i = 0; i < 5; i++) {
            TextView recyclerViewTitleTextView = new TextView(getActivity());
            LinearLayout.LayoutParams titleTextViewLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            titleTextViewLayoutParams.setMargins((int) (4 * Default.scale), (int) (4 * Default.scale), 0, 0);
            recyclerViewTitleTextView.setLayoutParams(titleTextViewLayoutParams);
            recyclerViewTitleTextView.setText("Most Liked");
            recyclerViewTitleTextView.setTextSize(15f);
            recyclerViewTitleTextView.setTextColor(Color.WHITE);
            recyclerViewTitleTextView.setTypeface(Default.sourceSansProLight);
            searchLinearLayout.addView(recyclerViewTitleTextView);

            RecyclerView prismPostDiscoveryRecyclerView = new RecyclerView(getActivity());
            LinearLayout.LayoutParams recyclerViewLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            prismPostDiscoveryRecyclerView.setLayoutParams(recyclerViewLayoutParams);
            LinearLayoutManager discoveryLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            DefaultItemAnimator discoveryDefaultItemAnimator = new DefaultItemAnimator();
            DividerItemDecoration discoveryDividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL);
            prismPostDiscoveryRecyclerView.setLayoutManager(discoveryLinearLayoutManager);
            prismPostDiscoveryRecyclerView.setItemAnimator(discoveryDefaultItemAnimator);
            prismPostDiscoveryRecyclerView.addItemDecoration(discoveryDividerItemDecoration);
            prismPostDiscoveryRecyclerView.setNestedScrollingEnabled(false);

            SearchDiscoverRecyclerViewAdapter searchDiscoverRecyclerViewAdapter = new SearchDiscoverRecyclerViewAdapter(getActivity(), MainContentFragment.prismPostArrayList);
            prismPostDiscoveryRecyclerView.setAdapter(searchDiscoverRecyclerViewAdapter);
            searchDiscoverRecyclerViewAdapters[i] = searchDiscoverRecyclerViewAdapter;
            searchLinearLayout.addView(prismPostDiscoveryRecyclerView);
        }


        return view;
    }
}