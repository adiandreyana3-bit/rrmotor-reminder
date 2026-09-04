package com.rrmotor.reminder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            tampilkanHalamanUtama();
        } else {
            tampilkanHalamanLogin();
        }
    }

    // =========================
    // LOGIN
    // =========================

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

    // =========================
    // HALAMAN UTAMA
    // =========================

    private void tampilkanHalamanUtama() {

        ScrollView scrollView = new ScrollView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 40, 30, 30);

        TextView title = new TextView(this);
        title.setText("🏍️ RR MOTOR REMINDER");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);

        Button tambahButton = new Button(this);
        tambahButton.setText("➕ TAMBAH REMINDER");
        tambahButton.setOnClickListener(v -> tampilkanFormTambah());

        Button lihatButton = new Button(this);
        lihatButton.setText("📋 ANTRIAN REMINDER");
        lihatButton.setOnClickListener(v -> tampilkanAntrian(layout));

        Button logoutButton = new Button(this);
        logoutButton.setText("LOGOUT");
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            tampilkanHalamanLogin();
        });

        layout.addView(title);
        layout.addView(tambahButton);
        layout.addView(lihatButton);
        layout.addView(logoutButton);

        scrollView.addView(layout);

        setContentView(scrollView);
    }

    // =========================
    // FORM TAMBAH REMINDER
    // =========================

    private void tampilkanFormTambah() {

        ScrollView scrollView = new ScrollView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);

        TextView title = new TextView(this);
        title.setText("➕ TAMBAH REMINDER");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 25);

        EditText nama = buatInput("Nama konsumen (opsional)");
        EditText wa = buatInput("Nomor WhatsApp");
        wa.setInputType(InputType.TYPE_CLASS_PHONE);

        EditText motor = buatInput("Jenis motor (opsional)");
        EditText nopol = buatInput("Nopol (opsional)");

        EditText km = buatInput("KM saat ganti oli (opsional)");
        km.setInputType(InputType.TYPE_CLASS_NUMBER);

        TextView pilihBulanText = new TextView(this);
        pilihBulanText.setText("Anda mau diingatkan berapa bulan?");
        pilihBulanText.setTextSize(17);
        pilihBulanText.setPadding(0, 20, 0, 10);

        Spinner bulanSpinner = new Spinner(this);

        String[] pilihanBulan = {
                "1 bulan",
                "2 bulan"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        pilihanBulan
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        bulanSpinner.setAdapter(adapter);

        Button simpanButton = new Button(this);
        simpanButton.setText("💾 SIMPAN REMINDER");

        Button kembaliButton = new Button(this);
        kembaliButton.setText("← KEMBALI");

        layout.addView(title);
        layout.addView(nama);
        layout.addView(wa);
        layout.addView(motor);
        layout.addView(nopol);
        layout.addView(km);
        layout.addView(pilihBulanText);
        layout.addView(bulanSpinner);
        layout.addView(simpanButton);
        layout.addView(kembaliButton);

        simpanButton.setOnClickListener(v -> {

            String namaText = nama.getText().toString().trim();
            String waText = wa.getText().toString().trim();
            String motorText = motor.getText().toString().trim();
            String nopolText = nopol.getText().toString().trim();
            String kmText = km.getText().toString().trim();

            if (waText.isEmpty()) {
                wa.setError("Nomor WhatsApp wajib diisi");
                wa.requestFocus();
                return;
            }

            int bulan = bulanSpinner.getSelectedItemPosition() + 1;

            int kmSekarang = 0;

            if (!kmText.isEmpty()) {
                try {
                    kmSekarang = Integer.parseInt(kmText);
                } catch (Exception e) {
                    km.setError("KM tidak valid");
                    km.requestFocus();
                    return;
                }
            }

            int kmMaksimal = 0;
            int kmTerakhir = 0;

            if (kmSekarang > 0) {
                kmMaksimal = kmSekarang + 1500;
                kmTerakhir = kmMaksimal + 500;
            }

            simpanReminder(
                    namaText,
                    waText,
                    motorText,
                    nopolText,
                    kmSekarang,
                    bulan,
                    kmMaksimal,
                    kmTerakhir
            );
        });

        kembaliButton.setOnClickListener(v ->
                tampilkanHalamanUtama()
        );

        scrollView.addView(layout);

        setContentView(scrollView);
    }

    private EditText buatInput(String hint) {

        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(16);
        input.setPadding(15, 15, 15, 15);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, 10);

        input.setLayoutParams(params);

        return input;
    }

    // =========================
    // SIMPAN FIRESTORE
    // =========================

    private void simpanReminder(
            String nama,
            String wa,
            String motor,
            String nopol,
            int km,
            int bulan,
            int kmMaksimal,
            int kmTerakhir
    ) {

        String tanggalGantiOli =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                ).format(new Date());

        Map<String, Object> data =
                new HashMap<>();

        data.put("nama", nama);
        data.put("wa", wa);
        data.put("motor", motor);
        data.put("nopol", nopol);
        data.put("km", km);
        data.put("bulan", bulan);
        data.put("tanggalGantiOli", tanggalGantiOli);
        data.put("kmMaksimal", kmMaksimal);
        data.put("kmTerakhir", kmTerakhir);
        data.put("status", "BELUM JATUH TEMPO");
        data.put("terkirim", false);
        data.put(
                "createdAt",
                com.google.firebase.firestore.FieldValue.serverTimestamp()
        );

        // Jadwal reminder berdasarkan bulan.
        long waktuReminder =
                System.currentTimeMillis()
                        + (bulan * 30L * 24L * 60L * 60L * 1000L);

        data.put(
                "tanggalReminderMillis",
                waktuReminder
        );

        db.collection("reminders")
                .add(data)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(
                            this,
                            "Reminder berhasil disimpan ke Firebase",
                            Toast.LENGTH_LONG
                    ).show();

                    tampilkanHalamanUtama();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Gagal menyimpan: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================
    // ANTRIAN REMINDER
    // =========================

    private void tampilkanAntrian(
            LinearLayout parentLayout
    ) {

        ScrollView scrollView = new ScrollView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);

        TextView title = new TextView(this);
        title.setText("📋 ANTRIAN REMINDER");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 25);

        layout.addView(title);

        TextView loading = new TextView(this);
        loading.setText("Memuat data...");
        loading.setTextSize(17);

        layout.addView(loading);

        Button kembali = new Button(this);
        kembali.setText("← KEMBALI");
        kembali.setOnClickListener(v ->
                tampilkanHalamanUtama()
        );

        layout.addView(kembali);

        scrollView.addView(layout);

        setContentView(scrollView);

        db.collection("reminders")
                .orderBy(
                        "tanggalReminderMillis",
                        Query.Direction.ASCENDING
                )
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    loading.setVisibility(View.GONE);

                    for (
                            DocumentSnapshot document :
                            queryDocumentSnapshots
                    ) {

                        tampilkanReminder(
                                layout,
                                document
                        );
                    }

                    if (queryDocumentSnapshots.isEmpty()) {

                        TextView kosong =
                                new TextView(this);

                        kosong.setText(
                                "Belum ada reminder."
                        );

                        kosong.setTextSize(18);

                        layout.addView(
                                kosong,
                                layout.indexOfChild(kembali)
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    loading.setText(
                            "Gagal mengambil data:\n"
                                    + e.getMessage()
                    );
                });
    }

    private void tampilkanReminder(
            LinearLayout layout,
            DocumentSnapshot document
    ) {

        String nama = stringValue(
                document,
                "nama"
        );

        String wa = stringValue(
                document,
                "wa"
        );

        String motor = stringValue(
                document,
                "motor"
        );

        String nopol = stringValue(
                document,
                "nopol"
        );

        long km =
                longValue(document, "km");

        long kmMaksimal =
                longValue(document, "kmMaksimal");

        long kmTerakhir =
                longValue(document, "kmTerakhir");

        long bulan =
                longValue(document, "bulan");

        String status =
                stringValue(document, "status");

        boolean terkirim =
                Boolean.TRUE.equals(
                        document.getBoolean("terkirim")
                );

        TextView item =
                new TextView(this);

        StringBuilder teks =
                new StringBuilder();

        teks.append("━━━━━━━━━━━━━━━━━━\n");

        if (!nama.isEmpty()) {
            teks.append("👤 ")
                    .append(nama)
                    .append("\n");
        }

        teks.append("📱 ")
                .append(wa)
                .append("\n");

        if (!motor.isEmpty()) {
            teks.append("🏍️ ")
                    .append(motor)
                    .append("\n");
        }

        if (!nopol.isEmpty()) {
            teks.append("🔖 ")
                    .append(nopol)
                    .append("\n");
        }

        teks.append("⏰ Reminder: ")
                .append(bulan)
                .append(" bulan\n");

        if (km > 0) {

            teks.append("🔧 KM: ")
                    .append(km)
                    .append("\n");

            teks.append("KM maksimal: ")
                    .append(kmMaksimal)
                    .append("\n");

            teks.append("⚠️ Paling lambat: ")
                    .append(kmTerakhir)
                    .append("\n");
        }

        teks.append("📌 Status: ")
                .append(status)
                .append("\n");

        teks.append(
                terkirim
                        ? "✅ Sudah diingatkan\n"
                        : "⏳ Belum dikirim\n"
        );

        teks.append("━━━━━━━━━━━━━━━━━━");

        item.setText(teks.toString());
        item.setTextSize(16);
        item.setPadding(0, 15, 0, 15);

        layout.addView(
                item,
                layout.indexOfChild(
                        layout.findViewWithTag(null)
                )
        );

        Button waButton =
                new Button(this);

        waButton.setText("💬 KIRIM WA");

        waButton.setOnClickListener(v ->
                bukaWhatsApp(
                        document.getId(),
                        wa,
                        nama,
                        motor,
                        kmMaksimal,
                        kmTerakhir,
                        bulan
                )
        );

        layout.addView(
                waButton,
                layout.indexOfChild(
                        layout.findViewWithTag(null)
                )
        );
    }

    private String stringValue(
            DocumentSnapshot document,
            String field
    ) {

        String value =
                document.getString(field);

        return value == null ? "" : value;
    }

    private long longValue(
            DocumentSnapshot document,
            String field
    ) {

        Long value =
                document.getLong(field);

        return value == null ? 0 : value;
    }

    // =========================
    // WHATSAPP
    // =========================

    private void bukaWhatsApp(
            String documentId,
            String nomor,
            String nama,
            String motor,
            long kmMaksimal,
            long kmTerakhir,
            long bulan
    ) {

        String nomorWA =
                nomor.replaceAll(
                        "[^0-9+]",
                        ""
                );

        if (nomorWA.startsWith("0")) {
            nomorWA =
                    "62"
                            + nomorWA.substring(1);
        }

        if (nomorWA.startsWith("+")) {
            nomorWA =
                    nomorWA.substring(1);
        }

        String namaPanggilan =
                nama.isEmpty()
                        ? "Bapak/Ibu"
                        : nama;

        String barisBulan;

        if (bulan == 1) {
            barisBulan =
                    "SUDAH SATU BULAN SAAT TERAKHIR GANTI OLI";
        } else {
            barisBulan =
                    "SUDAH 2 BULAN SAAT TERAKHIR GANTI OLI";
        }

        StringBuilder pesan =
                new StringBuilder();

        pesan.append("Halo Bapak/Ibu 👋\n\n");

        pesan.append(barisBulan)
                .append("\n\n");

        pesan.append(
                "Kami dari RR MOTOR ingin mengingatkan bahwa kendaraan Anda sudah memasuki jadwal pengecekan/penggantian oli. 🔧\n\n"
        );

        pesan.append(
                "✨ Pengecekan kondisi oli GRATIS!\n\n"
        );

        if (kmMaksimal > 0) {

            pesan.append(
                    "🔧 KM maksimal penggantian oli: "
            )
                    .append(formatAngka(kmMaksimal))
                    .append(" KM\n");

            pesan.append(
                    "⚠️ Paling lambat penggantian oli: "
            )
                    .append(formatAngka(kmTerakhir))
                    .append(" KM\n\n");
        }

        pesan.append(
                "Silakan datang ke RR MOTOR untuk pengecekan kondisi oli dan kendaraan Anda.\n\n"
        );

        pesan.append(
                "Jangan tunggu sampai terlambat agar mesin tetap nyaman dan terawat. 😊\n\n"
        );

        pesan.append(
                "Kami tunggu kedatangannya di RR MOTOR.\n\n"
        );

        pesan.append(
                "Terima kasih 🙏\n\n"
        );

        pesan.append("RR MOTOR");

        try {

            String url =
                    "https://wa.me/"
                            + nomorWA
                            + "?text="
                            + URLEncoder.encode(
                            pesan.toString(),
                            "UTF-8"
                    );

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

            startActivity(intent);

            // Tandai sudah dikirim setelah WhatsApp dibuka.
            db.collection("reminders")
                    .document(documentId)
                    .update(
                            "terkirim",
                            true,
                            "status",
                            "SUDAH DIINGATKAN"
                    );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "WhatsApp tidak dapat dibuka",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private String formatAngka(long angka) {

        return String.format(
                Locale.US,
                "%,d",
                angka
        ).replace(",", ".");
    }
}
