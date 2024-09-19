package com.crmushi.mitapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    AlertDialog dialog;
    LinearLayout login_layout,reg_layout;
    Button login,registerbtn;
    ProgressDialog progressDialog;
    Handler handler;
    EditText email_login,password_login,fullname_reg,email_reg,password_reg,confp_reg;
    TextView forgotpassword,donthaveacc,alreadyhaveacc;
    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
            "[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
            "A-Z]{2,7}$";
    Pattern pat = Pattern.compile(emailRegex);
    private ImageView eyeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText passwordEditText = findViewById(R.id.login_password);

        eyeIcon = findViewById(R.id.eye_icon);
        eyeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle password visibility
                if (passwordEditText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    eyeIcon.setImageResource(R.drawable.ic_eye);
                } else {
                    passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    eyeIcon.setImageResource(R.drawable.ic_eye_off);
                }
                // Move cursor to the end of the input
                passwordEditText.setSelection(passwordEditText.getText().length());
            }
        });

        firebaseAuth=FirebaseAuth.getInstance();
        handler=new Handler(Looper.getMainLooper());
        forgotpassword=findViewById(R.id.login_forgotpassword);
        donthaveacc=findViewById(R.id.login_donthaveaccl);
        alreadyhaveacc=findViewById(R.id.reg_alreadyHaveAccount);
        login_layout=findViewById(R.id.login_layout);
        reg_layout=findViewById(R.id.reg_layout);
        login=findViewById(R.id.login_btn);
        registerbtn=findViewById(R.id.register_btn);
        email_login=findViewById(R.id.login_email);
        password_login=findViewById(R.id.login_password);
        fullname_reg=findViewById(R.id.reg_fullname);
        email_reg=findViewById(R.id.reg_email);
        password_reg=findViewById(R.id.reg_password);
        confp_reg=findViewById(R.id.reg_confirmPassword);
        handler.post(() -> {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);

        });
        donthaveacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_layout.setVisibility(View.GONE);
                reg_layout.setVisibility(View.VISIBLE);
            }
        });
        alreadyhaveacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_layout.setVisibility(View.VISIBLE);
                reg_layout.setVisibility(View.GONE);
            }
        });

        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordReset();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=email_login.getText().toString().trim().toLowerCase();
                String password=password_login.getText().toString().trim();
                if (email.isEmpty()){
                    email_login.setError("Required");
                    email_login.requestFocus();
                } else if (password.isEmpty()) {
                    password_login.setError("Required");
                    password_login.requestFocus();
                }else{
                    firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(MainActivity.this, "success!", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(MainActivity.this, Dashboard.class);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=fullname_reg.getText().toString().trim();
                String email=email_reg.getText().toString().toLowerCase().trim();
                String password=password_reg.getText().toString().trim();
                String confpass=confp_reg.getText().toString().trim();
                if (name.isEmpty()){
                    fullname_reg.setError("Required");
                    fullname_reg.requestFocus();
                } else if (email.isEmpty()) {
                    email_reg.setError("Required");
                    email_reg.requestFocus();
                } else if (!pat.matcher(email).matches()) {
                    email_reg.setError("Invalid email");
                    email_reg.requestFocus();
                } else if (password.isEmpty()) {
                    password_reg.setError("Required");
                   password_reg.requestFocus();
                } else if (confpass.isEmpty()) {
                    confp_reg.setError("Required");
                    confp_reg.requestFocus();
                } else if (!password.equals(confpass)) {
                    confp_reg.setError("Password does not match!");
                }else{
                    HashMap<Object,String> hashMap=new HashMap<>();
                    hashMap.put("Fullname",name);
                    hashMap.put("Email",email);
                    hashMap.put("Password",password);
                    firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            DatabaseReference userSign= FirebaseDatabase.getInstance().getReference()
                                    .child("Students").child(firebaseAuth.getUid().toString());
                            userSign.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    reg_layout.setVisibility(View.GONE);
                                    login_layout.setVisibility(View.VISIBLE);
                                    Toast.makeText(MainActivity.this, "successfully registered", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Failed due to "+e+"", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null){
            Intent intent=new Intent(MainActivity.this, Dashboard.class);
            startActivity(intent);
            finish();
        }
    }

    private void passwordReset(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View popupView = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_resetpassword, null);

        EditText email_et = popupView.findViewById(R.id.rp_userEmail);
        Button proceedtoReset = popupView.findViewById(R.id.rp_resetButton);

        proceedtoReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                String my_email=email_et.getText().toString().trim();
                if (my_email.isEmpty()){
                    progressDialog.dismiss();
                    email_et.setError("Email required");
                }else if (!pat.matcher(my_email).matches()) {
                    progressDialog.dismiss();
                    email_et.setError("Invalid email!");
                }else{
                    FirebaseAuth.getInstance().sendPasswordResetEmail(my_email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Reset email sent successful to "+my_email, Toast.LENGTH_SHORT).show();
                            }else{
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Error reseting, retry later", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        builder.setView(popupView);
        dialog = builder.create();
        dialog.show();
    }
}