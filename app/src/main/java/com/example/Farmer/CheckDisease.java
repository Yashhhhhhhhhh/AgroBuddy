package com.example.Farmer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.Farmer.ml.CropModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CheckDisease extends AppCompatActivity {
    Button selectBtn,predictBtn,captureBtn;
    TextView result;
    ImageView imageView;
    Bitmap bitmap;
    ImageProcessor imageProcessor;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_disease);

        //permissions
        getPermission();

        String[] labels=new String[38];
        int cnt=0;
        try {
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(getAssets().open("labels.txt")));
            String line = bufferedReader.readLine();
            while(line!=null){
                labels[cnt]=line;
                cnt++;
                line=bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }

        imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(256, 256, ResizeOp.ResizeMethod.BILINEAR))
                        .build();


        selectBtn=findViewById(R.id.select);
        predictBtn=findViewById(R.id.predict);
        captureBtn=findViewById(R.id.capture);
        result=findViewById(R.id.result);
        imageView=findViewById(R.id.imageView);


        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 10);
            }
        });


        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 12);
            }
        });


        predictBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmap == null) {
                    Toast.makeText(getApplicationContext(), "Bitmap is null", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {

                    CropModel model = CropModel.newInstance(getApplicationContext());

                    // Print original bitmap size
                    Log.d("MainActivity", "Original bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                    // Preprocess and resize the input image
//                    bitmap = bitmap.createScaledBitmap(bitmap, 256, 256, true);
//
//                    // Convert the bitmap to a float array with values in the range [0, 1]
//                    float[] floatValues = new float[256 * 256 * 3];
//                    int[] intValues = new int[256 * 256];
//                    bitmap.getPixels(intValues, 0, 256, 0, 0, 256, 256);
//                    for (int i = 0; i < intValues.length; ++i) {
//                        final int val = intValues[i];
//                        floatValues[i * 3 + 0] = ((val >> 16) & 0xFF) / 255.0f;
//                        floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;
//                        floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f;
//                    }
//
//
//                    // Convert the float array to a TensorImage
//                    TensorImage inputImageBuffer = new TensorImage(DataType.FLOAT32);
//                    inputImageBuffer.load(floatValues, new int[]{1, 256, 256, 3});
//
//                    // Print TensorImage size and shape
//                    Log.d("MainActivity", "TensorImage size: " + inputImageBuffer.getBuffer().array().length);
//                    int width = inputImageBuffer.getWidth();
//                    int height = inputImageBuffer.getHeight();
//                    Log.d("MainActivity", "TensorImage shape: " + width + "x" + height);
//
//                    // Convert the TensorImage to a TensorBuffer
//                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
//                    inputFeature0.loadBuffer(inputImageBuffer.getBuffer());
//
//                    // Print TensorBuffer size and shape
//                    Log.d("MainActivity", "TensorBuffer size: " + inputFeature0.getFlatSize());
//                    Log.d("MainActivity", "TensorBuffer shape: " + inputFeature0.getShape().toString());

                    TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                    tensorImage.load(bitmap);
                    tensorImage = imageProcessor.process(tensorImage);

                    Log.d("MainActivity", "TensorImage size: " + tensorImage.getBuffer().array().length);
                    int width = tensorImage.getWidth();
                    int height = tensorImage.getHeight();
                    Log.d("MainActivity", "TensorImage shape: " + width + "x" + height);


                    // Convert the TensorImage to a TensorBuffer
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
                    inputFeature0.loadBuffer(tensorImage.getBuffer());
                    Log.d("MainActivity", "TensorBuffer size: " + inputFeature0.getFlatSize());
                    Log.d("MainActivity", "TensorBuffer shape: " + inputFeature0.getShape().toString());


                    // Runs model inference and gets result.
                    CropModel.Outputs outputs = model.process(inputFeature0);
                    Log.d("Input", "Input Done");
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                    Intent intent = new Intent();
                    intent.putExtra("KEY_TEXT", labels[getMax(outputFeature0.getFloatArray())]+"");
                    setResult(RESULT_OK, intent);
                    finish();
                      result.setText(labels[getMax(outputFeature0.getFloatArray())]+"");
//                    result.setText(getMax(outputFeature0.getFloatArray())+"");
                } catch (IOException e) {
                    // Handle the exception
                }
            }
        });

    }




    int getMax(float[] arr){
        int max=0;
        for (int i=0;i< arr.length;i++){
            if (arr[i]>arr[max]) max=i;
        }
        return max;
    }


    void getPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},11);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==11){
            if (grantResults.length>0){
                if (grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    this.getPermission();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==10){
            if(data!=null){
                Uri uri=data.getData();
                try {
                    bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else if (requestCode==12) {
            bitmap=(Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
