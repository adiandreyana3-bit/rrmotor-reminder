package com.rrmotor.reminder;

import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        tampilkanHalamanLogin();
    }

    private void tampilkanHalamanLogin() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(40, 60, 40, 40);

        TextView title = new TextView(this);
        title.setText("🏍️ RR MOTOR REMINDER");
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);

        TextView subtitle = new TextView(this);
        subtitle.setText("Login untuk melanjutkan");
        subtitle.setTextSize(17);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 40);

        emailInput = new EditText(this);
        emailInput.setHint("Email");
        emailInput.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        passwordInput = new EditText(this);
        passwordInput.setHint("Password");
        passwordInput.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        loginButton = new Button(this);
        loginButton.setText("LOGIN");
        loginButton.setOnClickListener(v -> login());

        layout.addView(title);
        layout.addView(subtitle);

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        inputParams.setMargins(0, 0, 0, 15);

        layout.addView(emailInput, inputParams);
        layout.addView(passwordInput, inputParams);
        layout.addView(loginButton);

        setContentView(layout);
    }

    private void login() {

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (email.isEmpty()) {
            emailInput.setError("Email wajib diisi");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password wajib diisi");
            passwordInput.requestFocus();
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("LOGIN...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    loginButton.setEnabled(true);
                    loginButton.setText("LOGIN");

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        Toast.makeText(
                                this,
                                "Login berhasil!",
                                Toast.LENGTH_SHORT
                        ).show();

                        tampilkanHalamanUtama();

                    } else {

                        String pesan = "Login gagal";

                        if (task.getException() != null) {
                            pesan = "Login gagal: "
                                    + task.getException().getMessage();
                        }

                        Toast.makeText(
                                this,
                                pesan,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void tampilkanHalamanUtama() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 40, 30, 30);

        TextView title = new TextView(this);
        title.setText("🏍️ RR MOTOR REMINDER");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 25);

        TextView info = new TextView(this);
        info.setText(
                "Login berhasil.\n\n" +
                "Sistem reminder RR MOTOR siap digunakan.\n\n" +
                "📱 Data konsumen\n" +
                "⏰ Jadwal reminder\n" +
                "🔧 Pengingat ganti oli\n" +
                "💬 Kirim WhatsApp"
        );
        info.setTextSize(18);

        Button logoutButton = new Button(this);
        logoutButton.setText("LOGOUT");

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            tampilkanHalamanLogin();
        });

        layout.addView(title);
        layout.addView(info);
        layout.addView(logoutButton);

        setContentView(layout);
    }
}
