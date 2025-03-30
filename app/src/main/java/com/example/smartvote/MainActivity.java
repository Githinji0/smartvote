package com.example.smartvote;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button btnSignUp;
    private EditText etEmail, etPassword, etConfirmPassword, etPhoneNumber;
    private TextView tvLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        btnSignUp = findViewById(R.id.btnSignUp);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        tvLogin = findViewById(R.id.tvLogin);

        // Register Button Click
        btnSignUp.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim();

            // Validate inputs
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(MainActivity.this, "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(MainActivity.this, "Passwords do not match",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading state
            btnSignUp.setEnabled(false);

            // Create user with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Registration successful
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Store additional user information in Firestore
                            if (user != null) {
                                Map<String, Object> userProfile = new HashMap<>();
                                userProfile.put("email", email);
                                userProfile.put("phoneNumber", phoneNumber);
                                userProfile.put("registrationDate", System.currentTimeMillis());

                                db.collection("users").document(user.getUid())
                                        .set(userProfile)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(MainActivity.this, "Registration successful!",
                                                    Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(MainActivity.this, LandingScreen.class);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            btnSignUp.setEnabled(true);
                                            Toast.makeText(MainActivity.this, "Error storing user data: " ,
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            // Registration failed
                            btnSignUp.setEnabled(true);
                            Toast.makeText(MainActivity.this, "Registration failed: " +
                                    task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Login link click
        tvLogin.setOnClickListener(view -> {
            // Navigate to login screen
            Intent intent = new Intent(MainActivity.this, LandingScreen.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}