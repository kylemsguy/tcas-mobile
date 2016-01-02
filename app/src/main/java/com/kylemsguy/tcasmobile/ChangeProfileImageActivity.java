package com.kylemsguy.tcasmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.ProfileManager;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ChangeProfileImageActivity extends AppCompatActivity {

    private static final int RQS_OPEN_IMAGE = 1;
    private static final int RQS_SAVE_IMAGE = 2;

    ImageView profileImgView;
    TextView statusView;
    Button browseButton;
    Button revertButton;
    Button submitButton;

    ProfileManager pm;
    Bitmap profileImage;
    Bitmap newImage;

    Uri newImgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile_image);

        pm = ((TCaSApp) getApplicationContext()).getProfileManager();
        profileImgView = (ImageView) findViewById(R.id.current_profile_image);
        statusView = (TextView) findViewById(R.id.status);
        browseButton = (Button) findViewById(R.id.browse_images);
        revertButton = (Button) findViewById(R.id.revert_image);
        submitButton = (Button) findViewById(R.id.submit_image);

        // clear the imageview
        profileImgView.setImageResource(0);

        new GetProfileImageTask().execute(pm);
    }

    private void updateImageView() {
        if (newImage != null && profileImgView != null) {
            profileImgView.setImageBitmap(newImage);
        }
    }

    public void browseImage(View v) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        startActivityForResult(intent, RQS_OPEN_IMAGE);
    }

    public void submitImage(View v) {
        updateStatus(true);
        new SubmitProfileImageTask().execute(pm, newImage);
    }

    public void revertImage(View v) {
        newImage = profileImage;
        updateStatus(true);
        updateImageView();
    }

    public void saveImage(View v) {
        // save newImage to disk
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_CREATE_DOCUMENT);
        } else {
            intent.setAction(Intent.ACTION_SEND);
        }

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/png");

        startActivityForResult(intent, RQS_SAVE_IMAGE);
    }

    public void updateStatus(boolean saved) {
        if (saved) {
            // set saved
            statusView.setText(R.string.status_saved);
            statusView.setTextColor(getResources().getColor(R.color.green));
        } else {
            // set unsaved
            statusView.setText(R.string.status_unsaved);
            statusView.setTextColor(getResources().getColor(R.color.red));
        }
    }

    private void writeImageContent(Uri uri, Bitmap image) {
        try {
            /*String[] uriChk = uri.toString().split("\\.");
            if(!uriChk[uriChk.length - 1].equals("png")) {
                String newUri = uri.toString();
                newUri = newUri + ".png";
                uri = Uri.parse(newUri);
            }*/
            ParcelFileDescriptor pfd = this.getContentResolver()
                    .openFileDescriptor(uri, "w");

            FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());

            image.compress(Bitmap.CompressFormat.PNG, 100, fos);

            fos.flush();
            fos.close();
            pfd.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            Uri dataUri = data.getData();

            if (requestCode == RQS_OPEN_IMAGE) {
                newImgUri = dataUri;

                try {
                    BitmapFactory.Options op = new BitmapFactory.Options();
                    op.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    newImage = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(newImgUri), null, op);
                    if (newImage.getWidth() != 32 || newImage.getHeight() != 32) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                .setTitle("Invalid Image Dimensions")
                                .setMessage("This image is not 32x32. I will try to resize it for you, but it may look weird.")
                                .setPositiveButton("Ok", null);
                        builder.show();

                        newImage = Bitmap.createScaledBitmap(newImage, 32, 32, false);
                    }
                    updateImageView();
                    updateStatus(false);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (requestCode == RQS_SAVE_IMAGE) {
                writeImageContent(dataUri, newImage);
            }
        }

    }


    private class GetProfileImageTask extends AsyncTask<ProfileManager, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(ProfileManager... params) {
            ProfileManager pm = params[0];

            try {
                return pm.getProfileImage();
            } catch (Exception e) {
                System.err.println("ChangeProfileImageActivity: Error getting Profile Image from server");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            profileImage = bitmap;
            newImage = bitmap;
            updateImageView();
        }
    }

    private class SubmitProfileImageTask extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            // params[0] is the ProfileManager
            // params[1] is the new profile image

            ProfileManager pm = (ProfileManager) params[0];
            Bitmap profileImage = (Bitmap) params[1];

            try {
                return pm.submitProfileImage(profileImage);
            } catch (Exception e) {
                System.err.println("ChangeProfileImageActivity: Failed to upload ProfileImage:");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            System.out.println(s);
            profileImage = newImage;
            updateStatus(true);
        }
    }
}
