package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private ImageView profilepic;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference root= FirebaseDatabase.getInstance().getReference().child("Image");
     TextView tv;
     Button btn;

    private Button inbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        profilepic=findViewById(R.id.image);
        inbtn=findViewById(R.id.insert);
        tv=findViewById(R.id.tv1);
        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference();
        int a=10;
    btn=findViewById(R.id.Predict);

        //Request request=new Request.Builder().url(" http://192.168.137.130:5000/predict").post(formbody).build();


        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery= new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery,2);

            }
        });

        inbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageUri!=null){
                    uploadpic(imageUri);
                }
                else{
                    Toast.makeText(MainActivity.this,"Else",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2 && resultCode ==RESULT_OK && data != null){
            imageUri=data.getData();
            profilepic.setImageURI(imageUri);

            
        }
    }

    private void uploadpic(Uri uri) {
        final Random myRandom = new Random();
        int a=myRandom.nextInt(100);
        String f=a+"."+getFileExtension(uri);
        StorageReference file=storageReference.child(a+"."+getFileExtension(uri));
        file.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        model model=new model(uri.toString());
                        String modelid=root.push().getKey();
                        root.child(modelid).setValue(model);
                        Toast.makeText(MainActivity.this,"Upload Succesful",Toast.LENGTH_LONG).show();
                    }
                });

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        OkHttpClient okHttpClient=new OkHttpClient();
                        RequestBody formbody=new FormBody.Builder().add("file",f ).build();
                        Request request=new Request.Builder().url(" http://192.168.137.130:5000/predict").post(formbody).build();
                        okHttpClient.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                Toast.makeText(MainActivity.this,"not connected",Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                tv.setText(response.body().string());

                            }
                        });

                    }
                });


            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Upload failed",Toast.LENGTH_LONG).show();
            }
        });
        tv.setText(f.toString());



    }

    private String getFileExtension(Uri muri) {

        ContentResolver cr=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(muri));
    }


}