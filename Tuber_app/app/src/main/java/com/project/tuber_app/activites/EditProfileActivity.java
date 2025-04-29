package com.project.tuber_app.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project.tuber_app.R;
import com.project.tuber_app.api.ApiClient;
import com.project.tuber_app.api.LoginApi;
import com.project.tuber_app.api.UserApi;
import com.project.tuber_app.databases.Database;
import com.project.tuber_app.databases.UserEntity;
import com.project.tuber_app.entities.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPhone;
    private TextView etCin, etGender;
    private Button btnSave ;
    private FloatingActionButton btnEditPic;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri imageUri;
    private Database database;
    private Toolbar toolbar;
    private ImageView ivProfilePicture;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        btnEditPic = findViewById(R.id.fabEditPicture);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSaveProfile);
        toolbar = findViewById(R.id.toolbar);
        etCin = findViewById(R.id.tvCIN);
        etGender = findViewById(R.id.tvGender);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);

        Executors.newSingleThreadExecutor().execute(() -> {
            UserEntity userEntity = database.userDao().getUser();
            etCin.setText(userEntity.cin);
            etGender.setText(userEntity.gender.toString());
            runOnUiThread(() -> {
                Glide.with(this)
                        .load(userEntity.userImage)
                        .placeholder(R.drawable.ic_person)
                        .into(ivProfilePicture);
            });

        });


        database = Database.getInstance(getApplicationContext());

        //register image picker launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        Glide.with(this)
                                .load(imageUri)
                                .placeholder(R.drawable.ic_person)
                                .into(ivProfilePicture);
                        Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

       

        //buttons logic
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
        btnEditPic.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"));

        });

        btnSave.setOnClickListener(v -> {
            String updatedFirstName = etFirstName.getText().toString().trim();
            String updatedLastName = etLastName.getText().toString().trim();
            String updatedPhone = etPhone.getText().toString().trim();

            Executors.newSingleThreadExecutor().execute(() -> {
                UserEntity userEntity = database.userDao().getUser();
                boolean changed = false;

                if (!updatedFirstName.isEmpty()) {
                    userEntity.firstName = updatedFirstName;
                    changed = true;
                }

                if (!updatedLastName.isEmpty()) {
                    userEntity.lastName = updatedLastName;
                    changed = true;
                }

                if (!updatedPhone.isEmpty()) {
                    userEntity.phoneNumber = updatedPhone;
                    changed = true;
                }

                if (changed) {
                    database.userDao().update(userEntity);

                    runOnUiThread(() -> {
                        updateUserInfoOnServer(new User(userEntity));
                        Toast.makeText(this, "Profile updated locally!", Toast.LENGTH_SHORT).show();
                    });
                }
            });

            if (imageUri != null) {
                uploadImageToServer(imageUri);
            }

            finish();
        });

    }

    private void updateUserInfoOnServer(User user){
    try {
        UserApi userApi = ApiClient.getUserApi(getApplicationContext());
        Call<ResponseBody> call = userApi.updateProfile(user);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Profile updated Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    } catch (Exception e) {
        e.printStackTrace();
        Toast.makeText(this, "Failed update image", Toast.LENGTH_SHORT).show();
        Log.e("updaet", "error:" + e.getMessage());
    }

    }




    private void uploadImageToServer(Uri imageUri) {
        try {
            // Step 1: Decode the image from the input stream
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap resizedBitmap = BitmapFactory.decodeStream(inputStream);



            // Step 3: Compress the resized bitmap to JPEG
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream); // 75% quality for compression
            byte[] compressedBytes = byteArrayOutputStream.toByteArray();

            // Optional: Check final size
            int sizeInKB = compressedBytes.length / 1024;
            Log.wtf("UploadImage", "Compressed image size: " + sizeInKB + "KB");


            // Step 3: Upload compressed image
            RequestBody requestFile = RequestBody.create(compressedBytes, MediaType.parse("image/jpeg"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", "profile.jpg", requestFile);

            UserApi userApi = ApiClient.getUserApi(getApplicationContext());
            Call<ResponseBody> call = userApi.uploadProfileImage(body);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            // Read the response body as a plain string
                            String responseBody = response.body().string();

                            // Handle successful image upload (save locally, etc.)
                            Executors.newSingleThreadExecutor().execute(() -> {
                                UserEntity userEntity = database.userDao().getUser();
                                userEntity.userImage = compressedBytes;
                                database.userDao().update(userEntity);

                                runOnUiThread(() -> {
                                    Toast.makeText(EditProfileActivity.this, "Image saved locally!", Toast.LENGTH_SHORT).show();
                                });
                            });

                            // Show the server's response (plain text)
                            Toast.makeText(EditProfileActivity.this, responseBody, Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Log.e("Upload", "Failed to read response body", e);
                        }
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.wtf("ee", "error: " + t.getMessage());
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            Log.wtf("ee" ,"failed to upload image :" + e.getMessage());
        }
    }

    private Bitmap resizeImage(Bitmap original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();

        // Calculate the aspect ratio
        float aspectRatio = (float) width / height;
        int newWidth = maxWidth;
        int newHeight = maxHeight;

        if (width > height) {
            newHeight = (int) (newWidth / aspectRatio);
        } else {
            newWidth = (int) (newHeight * aspectRatio);
        }

        // Resize the image maintaining the aspect ratio
        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }



}
