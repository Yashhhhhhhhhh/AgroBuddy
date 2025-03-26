package com.example.Farmer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText otpEditText;
    private Button sendOtpButton;
    private Button submitButton;
    private Button resendOtpButton;
    private TextView timerTextView;

    private FirebaseAuth firebaseAuth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 60000; // 1 minute
    private boolean timerRunning;
    private String newEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Initialize UI elements
        otpEditText = findViewById(R.id.otpEditText);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        submitButton = findViewById(R.id.submitButton);
        resendOtpButton = findViewById(R.id.resendOtpButton);
        timerTextView = findViewById(R.id.timerTextView);

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.bg_color));
        }

        // Add AuthStateListener to handle the case when the user's email is not immediately available
        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    String email = user.getEmail();
                    if (email != null) {
                        newEmail = email;
                        // User is authenticated, and email is available
                        getPhoneNumberFromFirestore(email);
                        firebaseAuth.removeAuthStateListener(this); // Remove the listener once email is retrieved
                    }
                }
            }
        });

        // Set click listener for sendOtpButton
        sendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendOtp();
            }
        });

        // Set click listener for submitButton
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitOtp();
            }
        });

        // Set click listener for resendOtpButton
        resendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendOtp();
            }
        });

        startTimer(); // Start the countdown timer
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                // Enable the resendOtpButton when the timer finishes
                resendOtpButton.setEnabled(true);
                timerTextView.setText("");
            }
        }.start();

        timerRunning = true;
    }

    private void updateTimer() {
        int seconds = (int) (timeLeftInMillis / 1000);
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
        timerTextView.setText(timeLeftFormatted);
    }

    private void getPhoneNumberFromFirestore(String email) {
        FirebaseFirestore.getInstance().collection("Users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean userFound = false;
                            for (DocumentSnapshot document : task.getResult()) {
                                userFound = true;
                                // Email matches, get the phone number
                                String rawPhoneNumber = document.getString("mobileNo");

                                if (rawPhoneNumber != null && !rawPhoneNumber.isEmpty()) {
                                    // Cleanse the phone number and include the country code "+91"
                                    String phoneNumber = "+91" + rawPhoneNumber.replaceAll("\\D", "");

                                    // Send OTP to the retrieved phone number
                                    sendOtpToPhoneNumber(phoneNumber);
                                } else {
                                    // Handle the case where the phone number is not available
                                    Toast.makeText(OtpVerificationActivity.this, "Phone number not available", Toast.LENGTH_SHORT).show();
                                }
                            }

                            if (!userFound) {
                                // Handle the case where the user document is not found for the given email
                                Toast.makeText(OtpVerificationActivity.this, "User document not found for email: " + email, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle errors
                            Toast.makeText(OtpVerificationActivity.this, "Error retrieving user details: " + task.getException(), Toast.LENGTH_SHORT).show();
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    private void sendOtpToPhoneNumber(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(OtpVerificationActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        OtpVerificationActivity.this.verificationId = verificationId;
                        resendToken = forceResendingToken;
                        Toast.makeText(OtpVerificationActivity.this, "OTP sent to " + phoneNumber, Toast.LENGTH_SHORT).show();

                        // Enable the submitButton after OTP is sent
                        submitButton.setEnabled(true);
                    }
                }, resendToken);
    }

    private void sendOtp() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            // User is already signed in, get the email
            String email = currentUser.getEmail();
            Toast.makeText(this, "" + email, Toast.LENGTH_SHORT).show();
            if (email != null) {
                // Email is available, proceed with phone number retrieval
                getPhoneNumberFromFirestore(email);
            } else {
                // If email is null, add an AuthStateListener to wait for the user information
                firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        FirebaseUser updatedUser = firebaseAuth.getCurrentUser();
                        if (updatedUser != null) {
                            // Authentication completed, get the email and remove the listener
                            String updatedEmail = updatedUser.getEmail();
                            if (updatedEmail != null) {
                                firebaseAuth.removeAuthStateListener(this);
                                getPhoneNumberFromFirestore(updatedEmail);
                            }
                        }
                    }
                });
            }
        } else {
            // User is not authenticated, handle accordingly
            Toast.makeText(OtpVerificationActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void resendOtp() {
        // Disable the resendOtpButton to prevent multiple clicks during the countdown
        resendOtpButton.setEnabled(false);

        // Resend the OTP
        if (firebaseAuth.getCurrentUser() != null && resendToken != null) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    firebaseAuth.getCurrentUser().getEmail(),
                    60,
                    TimeUnit.SECONDS,
                    this,
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                            signInWithPhoneAuthCredential(phoneAuthCredential);
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            Toast.makeText(OtpVerificationActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            OtpVerificationActivity.this.verificationId = verificationId;
                            resendToken = forceResendingToken;
                            Toast.makeText(OtpVerificationActivity.this, "OTP resent", Toast.LENGTH_SHORT).show();

                            // Enable the submitButton after OTP is resent
                            submitButton.setEnabled(true);
                        }
                    }, resendToken);
        } else {
            Toast.makeText(OtpVerificationActivity.this, "User not authenticated or resend token is null", Toast.LENGTH_SHORT).show();
        }

        // Restart the timer
        resetTimer();
        startTimer();
    }

    private void resetTimer() {
        countDownTimer.cancel();
        timeLeftInMillis = 60000; // Reset the timer to 1 minute
        updateTimer();
    }

    private void submitOtp() {
        String enteredOtp = otpEditText.getText().toString().trim();

        if (verificationId != null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, enteredOtp);
            signInWithPhoneAuthCredential(credential);
        } else {
            Toast.makeText(OtpVerificationActivity.this, "Verification ID is null. Please resend OTP.", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(OtpVerificationActivity.this, "OTP verification successful", Toast.LENGTH_SHORT).show();
                            retrieveUserTypeAndRedirect();
                        } else {
                            Toast.makeText(OtpVerificationActivity.this, "OTP verification failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void retrieveUserTypeAndRedirect() {
        // User is already signed in, get the email
        String email = newEmail;
        if (email != null) {
            // Email is available, proceed with user type retrieval
            retrieveUsernameFromUid(email);
        } else {
            // If email is null, add an AuthStateListener to wait for the user information
            firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser updatedUser = firebaseAuth.getCurrentUser();
                    if (updatedUser != null) {
                        // Authentication completed, get the email and remove the listener
                        String updatedEmail = updatedUser.getEmail();
                        if (updatedEmail != null) {
                            firebaseAuth.removeAuthStateListener(this);
                            retrieveUsernameFromUid(updatedEmail);
                        } else {
                            // Handle the case where the email is still null
                            Toast.makeText(OtpVerificationActivity.this, "Email is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    private void retrieveUsernameFromUid(String email) {
        FirebaseFirestore.getInstance().collection("Users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                                String userType = document.getString("userType");
                                String username = document.getString("username");

                                if (username != null) {
                                    // Save username to SharedPreferences
                                    SharedPreferences preferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("user_Id", username);
                                    editor.apply();

                                    // Save user email to SharedPreferences (added this part)
                                    editor.putString("userEmail", email);
                                    editor.apply();

                                    // Save OTP verification status to SharedPreferences
                                    editor.putBoolean("isOtpVerified", true);
                                    editor.apply();

                                    editor.putString("userType", userType);
                                    editor.apply();
                                    // Proceed with user type checking
                                    if (userType != null) {
                                        if (userType.equals("Farmer")) {
                                            startActivity(new Intent(OtpVerificationActivity.this, Activity_Post.class));
                                        } else if (userType.equals("Trader")) {
                                            startActivity(new Intent(OtpVerificationActivity.this, TraderHome.class));
                                        } else if (userType.equals("Consumer")) {
                                            startActivity(new Intent(OtpVerificationActivity.this, ConsumerHome.class));
                                        } else {
                                            Toast.makeText(OtpVerificationActivity.this, "Unknown user type: " + userType, Toast.LENGTH_SHORT).show();
                                        }
                                        finish();
                                    } else {
                                        Toast.makeText(OtpVerificationActivity.this, "User type not found in document", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(OtpVerificationActivity.this, "Username not found in document", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(OtpVerificationActivity.this, "User document not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(OtpVerificationActivity.this, "Error retrieving username: " + task.getException(), Toast.LENGTH_SHORT).show();
                            task.getException().printStackTrace();
                        }
                    }
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}