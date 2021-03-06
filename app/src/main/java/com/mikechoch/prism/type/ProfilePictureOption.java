package com.mikechoch.prism.type;

import com.mikechoch.prism.R;
import com.mikechoch.prism.constant.Default;

public enum ProfilePictureOption {

    GALLERY("Choose from gallery", R.drawable.ic_image_white_36dp),
    SELFIE("Take a selfie", R.drawable.ic_camera_front_variant_white_36dp),
    VIEW("View profile picture", R.drawable.ic_account_circle_white_36dp);

    final String title;
    final int icon;

    ProfilePictureOption(String title, int icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public int getIcon() {
        return icon;
    }

}
