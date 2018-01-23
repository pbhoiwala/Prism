package com.mikechoch.prism;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mikechoch on 1/21/18.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> wallpaperKeys;
    private HashMap<String, Wallpaper> wallpaperHashMap;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private ItemListener mListener;

    public RecyclerViewAdapter(Context context, ArrayList<String> wallpaperKeys, HashMap<String, Wallpaper> wallpaperHashMap, ItemListener listener) {
        this.context = context;
        this.wallpaperKeys = wallpaperKeys;
        this.wallpaperHashMap = wallpaperHashMap;
        this.mListener = listener;

        this.sourceSansProLight = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Light.ttf");
        this.sourceSansProBold = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Black.ttf");
    }

    public void setListener(ItemListener listener) {
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(wallpaperHashMap.get(wallpaperKeys.get(position)));
    }

    @Override
    public int getItemCount() {
        return wallpaperHashMap.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Wallpaper wallpaper;
        private TextView wallpaperCaptionTextView;
        private TextView wallpaperUserTextView;
        private ImageView wallpaperImageView;
        private FrameLayout imageFrameLayout;
        private ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            imageFrameLayout = itemView.findViewById(R.id.image_frame_layout);
            progressBar = itemView.findViewById(R.id.image_progress_bar);
            progressBar.setVisibility(View.VISIBLE);

            wallpaperUserTextView = itemView.findViewById(R.id.recycler_view_user_text_view);
            wallpaperUserTextView.setTypeface(sourceSansProBold);

            wallpaperImageView = itemView.findViewById(R.id.recycler_view_image_image_view);
        }

        public void setData(final Wallpaper wallpaper) {
            this.wallpaper = wallpaper;
            wallpaperUserTextView.setText("username");
            wallpaperCaptionTextView.setText(wallpaper.getCaption());
            Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(new Runnable(){
                @Override
                public void run() {
                    Picasso.with(context).load(wallpaper.getImageUri()).into(wallpaperImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {

                        }
                    });
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(wallpaper);
            }
        }
    }

    public interface ItemListener {
        void onItemClick(Wallpaper item);
    }
}
