
package com.example.rider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    TextView no_account, forget_pwd;
    EditText login_Email, login_Password;
    Button login;
    String emailPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)(\\.[A-Za-z]{2,})$";
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    RadioGroup radioGroup;
    RadioButton rb_volunteer, rb_student;

    //    ProgressBar progressBar;
    // Back Button pressed Exit Dialog
    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("Do you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = fAuth.getCurrentUser();
        if(currentUser != null){
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        getSupportActionBar().setTitle("LoginForm");

        login_Email = findViewById(R.id.login_email_id);
        login_Password = findViewById(R.id.login_password_id);

        no_account = findViewById(R.id.login_registerTextView_id);
        forget_pwd = findViewById(R.id.login_forgetpassword_id);

        login = findViewById(R.id.login_loginBtn_id);

        radioGroup = findViewById(R.id.radioGroup_Id);
        rb_student = findViewById(R.id.rb_student_id);
        rb_volunteer = findViewById(R.id.rb_volunteer_id);

//        progressBar = findViewById(R.id.login_progressBar_id);
        fAuth = FirebaseAuth.getInstance();


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = login_Email.getText().toString().trim();
                String password = login_Password.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    login_Email.setError("Email is required");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    login_Password.setError("Password is required");
                    return;
                }


                //authenticate the user
                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            login.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String email = login_Email.getText().toString().trim();
                                    String password = login_Password.getText().toString().trim();
                                    if (TextUtils.isEmpty(email)) {
                                        login_Email.setError("Email is required");
                                        return;
                                    }
                                    if (TextUtils.isEmpty(password)) {
                                        login_Password.setError("Password is required");
                                        return;
                                    }
                                    if (password.length() < 6) {
                                        login_Password.setError("Password Incorrect");
                                    }
                                    if (login.isPressed()) {

                                        switch (radioGroup.getCheckedRadioButtonId()) {
                                            case R.id.rb_student_id:
                                                Intent Aintent = new Intent(getApplicationContext(), StudentSideNavBar.class);
                                                startActivity(Aintent);
                                                break;

                                            case R.id.rb_volunteer_id:
                                                Intent Cintent = new Intent(getApplicationContext(), VolunteerSideNavBar.class);
                                                startActivity(Cintent);
                                                break;
                                        }
                                    }
                                    else{
                                        Toast.makeText(LoginActivity.this, "CHeck one of Button", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
//                            FirebaseUser user = fAuth.getCurrentUser();
////                            Intent Aintent = new Intent(getApplicationContext(), AdminSplashScreenActivity.class);
////                            startActivity(Aintent);
//                            Toast.makeText(LoginActivity.this, "Loggedin successful", Toast.LENGTH_SHORT).show();
////                            updateUI(user);

                        } else {
                            Toast.makeText(LoginActivity.this, "Error !" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                         progressBar.setVisibility(View.GONE);
//                            updateUI(null);

                        }
                    }
                });

            }

        });

        // onclick event to redirect to register layout
        no_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


        forget_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText resetMail = new EditText(view.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter your Mail to receive Reset Link");
                passwordResetDialog.setView(resetMail);

                //yes or no
                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // extract the email ans send reset link
                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this, "Reset Link has been sent in your mail", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Link is not sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                    }
                });
                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//close the dialog
                    }
                });
                passwordResetDialog.create().show();

            }
        });


    }




}





