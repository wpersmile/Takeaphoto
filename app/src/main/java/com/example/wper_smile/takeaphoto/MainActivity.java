package com.example.wper_smile.takeaphoto;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="myTag";

    String mCurrentPhotoPath;//图像文件路径

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO_AND_SAVE_TO_PUBLIC = 2;
    static final int REQUEST_TAKE_PHOTO_AND_SAVE_TO_PRIVATE = 3;

    ImageView mImageView=null;//缩略图


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //拍照并以缩略图显示
        Button takeBtn=(Button)findViewById(R.id.takeBtn);
        mImageView=(ImageView)findViewById(R.id.photoImage);
        //只有设备上有相机才能执行相机的有关操作
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            takeBtn.setEnabled(false);
        }

        takeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });


        //拍照并保存到公共图片目录
        Button buttonSaveToPublic=(Button)findViewById(R.id.savePublicBtn);
        //只有设备上有相机才能执行相机的有关操作
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            buttonSaveToPublic.setEnabled(false);
        }


        buttonSaveToPublic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFileInPublicDir();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO_AND_SAVE_TO_PUBLIC);
                    }
                }
            }
        });

        //拍照并保存到私有目录
        Button buttonSaveToPrivate=(Button)findViewById(R.id.savePriaverBtn);
        //只有设备上有相机才能执行相机的有关操作
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            buttonSaveToPrivate.setEnabled(false);
        }

        buttonSaveToPrivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFileInPrivateDir();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO_AND_SAVE_TO_PRIVATE);
                    }
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
        }else if (requestCode == REQUEST_TAKE_PHOTO_AND_SAVE_TO_PUBLIC && resultCode == RESULT_OK) {
            setPic();//显示缩略图
            addPicTogallery();
        }  else if (requestCode == REQUEST_TAKE_PHOTO_AND_SAVE_TO_PRIVATE && resultCode == RESULT_OK) {
            setPic();//显示缩略图
        }
    }

    //代码有点问题
    private void setPic() {
        // Get the dimensions of the View
        int targetW =20;// mImageView.getWidth();
        int targetH =20;// mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;//设置仅加载位图边界信息（相当于位图的信息，但没有加载位图）

        //返回为NULL，即不会返回bitmap,但可以返回bitmap的横像素和纵像素还有图片类型
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }


    private void addPicTogallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }




    private File createImageFileInPublicDir() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        //如果路径不存在，则创建之
        if(!storageDir.exists()) {
            Log.v(TAG,"目录不存在，创建之");
            if(!storageDir.mkdirs()) {
                Log.v(TAG,"目录创建失败");
                return null;
            }
        }

        //    File image = File.createTempFile(
        //            imageFileName,  /* prefix */
        //            ".jpg",         /* suffix */
        //            storageDir      /* directory */
        //    );

        File image = new File(storageDir, imageFileName+"photo.jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath =  image.getAbsolutePath();

        Log.v(TAG,"mCurrentPhotoPath:"+mCurrentPhotoPath);
        return image;
    }

    public File createImageFileInPrivateDir() throws IOException  {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        //如果路径不存在，则创建之
        if(!storageDir.exists()) {
            Log.v(TAG, "目录不存在，创建之");
            if(!storageDir.mkdirs()) {
                Log.v(TAG,"目录创建失败");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath =  image.getAbsolutePath();
        Log.v(TAG,"mCurrentPhotoPath:"+mCurrentPhotoPath);
        return image;
    }
}