package com.example.rider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VolunteerLogin extends AppCompatActivity {
    TextView no_account, forget_pwd;
    EditText login_Email, login_Password;
    Button login;
    String emailPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)(\\.[A-Za-z]{2,})$";
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    ProgressDialog dialog;
    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(VolunteerLogin.this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("Do you want to LogOut?")
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.volunteer_login);
        no_account = findViewById(R.id.login_registerTextView_id);

        login_Email = findViewById(R.id.login_email_id);
        login_Password = findViewById(R.id.login_password_id);

        fAuth =  FirebaseAuth.getInstance();
        fUser = fAuth.getCurrentUser();

        login = findViewById(R.id.login_loginBtn_id);

        no_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VolunteerLogin.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             performLogin();
                dialog = new ProgressDialog(VolunteerLogin.this);
                dialog.setMessage("Logging in");
                dialog.show();
            }
        });
    }

    private void performLogin() {
        String email = login_Email.getText().toString().trim();
        String password = login_Password.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            login_Email.setError("Email is required");
            return;
        }
        if (email.matches(emailPattern)){
            login_Email.setError("Email is badly formatted");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            login_Password.setError("Password is required");
            return;
        }

        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(VolunteerLogin.this, "Logged in", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VolunteerLogin.this, VolunteerSideNavBar.class);
                    startActivity(intent);
                    dialog.dismiss();
                }
                else {
                    Toast.makeText(VolunteerLogin.this, "Error !" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}