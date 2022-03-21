package com.example.rider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    Button register, cancel;
    TextView selectProfile;
    ImageView profileImage;

    EditText inputFirstName, inputLastName, inputEmail, inputAddress, inputPhoneNumber, inputPassword;
    String emailPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)(\\.[A-Za-z]{2,})$";
    ProgressDialog progressDialog;

    RadioGroup radioGroup;
    RadioButton rb_volunteer, rb_student;

    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;
    String userID;

    Uri setImageURI;
    private static final int IMAGE_PICK_CODE =1000;
    private static final int PERRMISSION_CODE =1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputFirstName = findViewById(R.id.register_firstname_id);
        inputLastName = findViewById(R.id.register_lastname_id);
        inputEmail = findViewById(R.id.register_email_id);
        inputPassword = findViewById(R.id.register_password_id);
        inputAddress = findViewById(R.id.register_address_id);
        inputPhoneNumber =  findViewById(R.id.register_phone_id);


        register = findViewById(R.id.register_registerBtn_id);
        cancel = findViewById(R.id.register_cancelBtn_id);
        selectProfile =findViewById(R.id.register_selectProfile_id);
        profileImage =  findViewById(R.id.registerimageSelect_id);

        radioGroup = findViewById(R.id.radioGroup_Id);
        rb_student = findViewById(R.id.rb_student_id);
        rb_volunteer = findViewById(R.id.rb_volunteer_id);

        fAuth =  FirebaseAuth.getInstance();
        fUser = fAuth.getCurrentUser();
        fStore =FirebaseFirestore.getInstance();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performAuth();
                checkButton();
            }
        });

   selectProfile.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View view) {
           selectImage();
       }
   });


}

    private void checkButton() {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.rb_student_id:
                Intent Aintent = new Intent(getApplicationContext(), StudentLogin.class);
                startActivity(Aintent);
                break;

            case R.id.rb_volunteer_id:
                Intent Cintent = new Intent(getApplicationContext(), VolunteerLogin.class);
                startActivity(Cintent);
                break;
        }
    }

    private void performAuth() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String firstName = inputFirstName.getText().toString().trim();
        String lastName = inputLastName.getText().toString().trim();
        String address = inputAddress.getText().toString().trim();
        String phoneNumber = inputPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) {
            inputFirstName.setError("FirstName is required");
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            inputLastName.setError("LastName is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            inputEmail.setError("Email is required");
            return;
        }
        if (email.matches(emailPattern)){
            inputEmail.setError("Email is badly formatted");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Password is required");
            return;
        }

        if (TextUtils.isEmpty(address)) {
            inputAddress.setError("Address is required");
            return;
        }
        if (password.length() < 6) {
            inputPassword.setError("Password must be >= 8 characters");
            return;
        }
        if (phoneNumber.length() < 10) {
            inputPhoneNumber.setError("Password must be = 10 digits");
            return;
        }


//        else {
//            progressDialog.setMessage("Plz wait while registration");
//            progressDialog.setTitle("Registration");
//            progressDialog.setCanceledOnTouchOutside(false);
//            progressDialog.show();
//        }

        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(RegisterActivity.this, "User Created", Toast.LENGTH_SHORT).show();
                    userID = fAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = fStore.collection("users").document(userID);
//                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    Map<String, Object> user = new HashMap<>();
                    user.put("firstName", firstName);
                    user.put("lastName", lastName);
                    user.put("email", email);
                    user.put("phoneNumber", phoneNumber);
                    user.put("address", address);
                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: user Profile is created" + userID);
                        }
                    });
//                    startActivity(intent);
                }
                else {
                    Toast.makeText(RegisterActivity.this, "Error !" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void selectImage() {
        final  CharSequence [] options = {"Take Photo", "Choose from Gallery","cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (options[i].equals("Take Photo"))
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                }
                else if (options[i].equals("Choose from Gallery"))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                }
                else if (options[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);
                    profileImage.setImageBitmap(bitmap);
                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                profileImage.setImageBitmap(thumbnail);
            }
        }
    }

}