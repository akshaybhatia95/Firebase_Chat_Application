package com.example.d.unitedtochat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSettings extends AppCompatActivity {
   // g manual permissions for API level 23 and above i use this code.

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{permission},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static final int PICK_IMAGE = 1;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    String mName, mStatus, mImage, thumbImage;
    Uri imageUri;
    Bitmap myBitMap;
    String uId;
    private TextView displayName;
    private TextView statusName;
    private CircleImageView mCircleImageView;
    private Button changeStatusButton;
    private Button changePicturebutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        displayName = (TextView) findViewById(R.id.settingsDisplayName);
        statusName = (TextView) findViewById(R.id.settingsStatus);
        mCircleImageView = (CircleImageView) findViewById(R.id.circleImageView);
        changeStatusButton = (Button) findViewById(R.id.changeStatus);
        changePicturebutton = (Button) findViewById(R.id.changeImage);
        mStorage = FirebaseStorage.getInstance().getReference();


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        uId = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);

        mDatabase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProgressDialog pd = new ProgressDialog(AccountSettings.this);
                pd.setTitle("Loading");
                pd.show();
                if (dataSnapshot.child("name").getValue() != null) {
                    mName = dataSnapshot.child("name").getValue().toString();
                }
                if (dataSnapshot.child("status").getValue() != null) {
                    mStatus = dataSnapshot.child("status").getValue().toString();
                }
                if (dataSnapshot.child("image").getValue() != null) {
                    mImage = dataSnapshot.child("image").getValue().toString();
                    Log.d("IMAGE", mImage);

                }
                if (dataSnapshot.child("thumb_image").getValue() != null) {
                    thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                }
                Toast.makeText(AccountSettings.this, "Here", Toast.LENGTH_SHORT).show();
                displayName.setText(mName);
                statusName.setText(mStatus);

                //    Toast.makeText(AccountSettings.this,"mImage "+mImage,Toast.LENGTH_SHORT).show();
                if (mImage == "default") {
                    return;
                }

                Picasso.get().load(mImage).placeholder(R.drawable.viewlogo).into(mCircleImageView);
                pd.dismiss();
                pd.cancel();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                finish();
            }
        });

        changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusIntent = new Intent(AccountSettings.this, StatusActivity.class);
                statusIntent.putExtra("status", statusName.getText().toString());
                startActivity(statusIntent);

            }
        });
        changePicturebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), PICK_IMAGE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {

            imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                final ProgressDialog progressDialog = new ProgressDialog(AccountSettings.this);
                progressDialog.setTitle("Uploading Image");
                progressDialog.show();
                final StorageReference filePath = mStorage.child("profileImages").child(uId);

                if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
                    File thumbFile = new File(getPath(AccountSettings.this, imageUri));

                    try {
                        myBitMap = new Compressor(this)
                                .setMaxHeight(200)
                                .setMaxWidth(200)
                                .setQuality(75)
                                .compressToBitmap(thumbFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                myBitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumbBytes = baos.toByteArray();
                final StorageReference thumbStorage = filePath.child("profile_image_thumb.jpg");


                // Uri resultUri = result.getUri();
                //Toast.makeText(AccountSettings.this,resultUri.toString(),Toast.LENGTH_SHORT).show();
                // Toast.makeText(AccountSettings.this,"DOWNLOAD LINK"+imageUri.toString(),Toast.LENGTH_LONG).show();
                filePath.child("profile_image.jpg").putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(AccountSettings.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        progressDialog.cancel();

                        filePath.child("profile_image.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String requiredUri = uri.toString();
                                // Toast.makeText(AccountSettings.this,requiredUri,Toast.LENGTH_SHORT).show();
                                mDatabase.child("image").setValue(requiredUri);
                                Log.d("IMAGE2", requiredUri);
                            }
                        });
                        //ThumbNail
                        UploadTask uploadTask = thumbStorage.putBytes(thumbBytes);
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(AccountSettings.this, "Thumbnail Uploaded", Toast.LENGTH_SHORT).show();
                                    thumbStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String thumbUri = uri.toString();
                                            Log.d("IMAGE_THUMB", thumbUri);
                                            mDatabase.child("thumb_image").setValue(thumbUri);
                                        }
                                    });
                                }
                            }
                        });
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }


        }
    }
}
