package com.mikechoch.prism.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mikechoch.prism.R;
import com.mikechoch.prism.constant.Default;
import com.mikechoch.prism.helper.BitmapHelper;
import com.mikechoch.prism.helper.FileChooser;
import com.mikechoch.prism.helper.Helper;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by mikechoch on 1/21/18.
 */

public class ImageUploadActivity extends AppCompatActivity {

    /*
     * Global variables
     */

    private Toolbar toolbar;
    private TextView toolbarTextView;
    private TextInputLayout imageDescriptionTextInputLayout;
    private EditText imageDescriptionEditText;
    private ImageView uploadedImageImageView;
    private Button uploadButton;

    private Uri imageUri;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.expense_detail_menu, menu);
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
        setContentView(R.layout.image_upload_activity_layout);
        
        // Initialize all toolbar elements
        toolbar = findViewById(R.id.toolbar);
        toolbarTextView = findViewById(R.id.toolbar_text_view);

        // Initialize all UI elements
        imageDescriptionTextInputLayout = findViewById(R.id.image_description_title_text_input_layout);
        imageDescriptionEditText = findViewById(R.id.image_description_edit_text);
        uploadedImageImageView = findViewById(R.id.uploaded_image_image_view);
        uploadButton = findViewById(R.id.upload_button);

        setupUIElements();

        // Ask user to select an image to upload from phone gallery
        selectImageFromGallery();
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
     * Setup the uploadedImageImageView,
     */
    private void setupUploadedImageImageView() {
        uploadedImageImageView.getLayoutParams().width = (int) (Default.screenWidth * 0.9);
        uploadedImageImageView.setMaxHeight((int) (Default.screenHeight * 0.6));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            uploadedImageImageView.setForeground(getResources().getDrawable(R.drawable.image_upload_selector));
        }
        uploadedImageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFromGallery();
            }
        });
    }

    /**
     * Setup onClickListener for uploadButton
     */
    private void setupUploadButton() {
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(Helper.parseDescriptionForTags(imageDescriptionEditText.getText().toString()));
                intentBackToMainActivitySuccess();
            }
        });
    }

    /**
     * This passes the uploaded image data (image and description) back to MainActivity
     * Then ImageUploadActivity is finished with success
     */
    private void intentBackToMainActivitySuccess() {
        Intent data = new Intent();
        data.putExtra(Default.IMAGE_URI_EXTRA, imageUri.toString());
        data.putExtra(Default.IMAGE_DESCRIPTION_EXTRA, imageDescriptionEditText.getText().toString().trim());
        setResult(RESULT_OK, data);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Setup all UI elements
     */
    private void setupUIElements() {
        setupToolbar();

        // Setup Typefaces for all text based UI elements
        toolbarTextView.setTypeface(Default.sourceSansProLight);
        imageDescriptionTextInputLayout.setTypeface(Default.sourceSansProLight);
        uploadButton.setTypeface(Default.sourceSansProLight);
        imageDescriptionEditText.setTypeface(Default.sourceSansProLight);

        setupUploadedImageImageView();
        setupUploadButton();
    }

    /**
     * Create an Intent to ask firebaseUser to select a image they would like to upload
     */
    private void selectImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Select an image"), Default.GALLERY_INTENT_REQUEST);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * When an Activity is going to send back a result onActivityResult will tale responsibility
     * GALLERY_INTENT_REQUEST: intents back a Gallery imageUri to be rotated and ready for upload
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case Default.GALLERY_INTENT_REQUEST:
                if (resultCode == RESULT_OK) {
                    imageUri = data.getData();
                    Bitmap bitmap = null;
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String imagePath = FileChooser.getPath(this, imageUri);
                    bitmap = BitmapHelper.rotateBitmap(imagePath, bitmap);
                    imageUri = getImageUri(bitmap);
                    Glide.with(this)
                            .asBitmap()
                            .load(bitmap)
                            .into(uploadedImageImageView);
                } else {
                    if (uploadedImageImageView.getDrawable() == null) {
                        finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * Pass in a local Bitmap from Gallery and get the URI of the Bitmap
     * Recreated the insertImage method and placed in our own BitmapHelper
     * Original insertImage method compressed images 50% at all times, but we want control of that
     */
    private Uri getImageUri(Bitmap inBitmap) {
        String path = BitmapHelper.insertImage(this.getContentResolver(), inBitmap, null, null);
        return Uri.parse(path);
    }

}
