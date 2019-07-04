package ovarb.vision.test.com;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {

    EditText mResultEt;
    ImageView mPreviewIv;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    String cameraPermission[];
    String storagePermission[];

    Uri image_uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResultEt = findViewById(R.id.resultEt);
        mPreviewIv = findViewById(R.id.imageIv);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}; 

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.addImage){
            showImageImprtDialog();
        }

        if(id == R.id.settings){
            Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageImprtDialog() {
        String[] items = {"Camera", "Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    //open camera
                    //check for camera permission
                    if(!checkCameraPermission()){
                        //camera use is not allowed, request it
                        requestCameraPermission();
                    }else{
                         //if permission is allowed, take pic
                        pickCamera();
                    }


                }

                if(i == 1){
                    //check for storage permission
                    if(!checkStroagePermission()){
                        //storage use is not allowed, request it
                        requestStoragePermission();
                    }else{
                        pickGallery();
                    }
                }
            }
        });

        dialog.create().show();
    }

    private void pickGallery() {
        //INTENT TO PICK IMAGE FORM GALLETY
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);


    }

    private void pickCamera() {
        //intent to take image from camera, it will also be saved to storage to get high quility image
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New pIC");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image to Text");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);

    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStroagePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length > 0){
                    boolean cameerAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(cameerAccepted && writeAccepted){
                        pickCamera();
                    }else{
                        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                    }
                }

                break;


            case STORAGE_REQUEST_CODE:
                if(grantResults.length > 0){
                    boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(writeAccepted){
                        pickGallery();
                    }else{
                        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
        }
    }

    //handle  image result


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
       //got image
        if(requestCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){

                //GOT IMAGE FROM GALLARY NOW CROP IT
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this); //ENABLE IMAGE GUILDLINES


            }

            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //GOT FROM CAMERA NOE CROP IT
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this); //ENABLE IMAGE GUILDLINES

            }
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(requestCode == RESULT_OK){
                Uri resultUri = result.getUri();

                //set image to image view
                mPreviewIv.setImageURI(resultUri);

                //NOE GET THE BIT MAP FOR TEXT REC

                BitmapDrawable bitmapDrawable = (BitmapDrawable)mPreviewIv.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                if(!recognizer.isOperational()){
                    Toast.makeText(this, "Not functional", Toast.LENGTH_SHORT).show();
                }else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build(); ///build frame with bitmap
                    SparseArray<TextBlock> items = recognizer.detect(frame); //place that frame into the sparse array;
                    StringBuilder sb = new StringBuilder();

                    for(int i = 0; i < items.size(); i++){
                        TextBlock myitem = items.valueAt(i);
                        sb.append(myitem.getValue());
                        sb.append("\n");


                        //set the edit text to results
                        mResultEt.setText(sb.toString());

                    }
                }


            }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception e = result.getError();
                Toast.makeText(this, " " + e, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
