package com.rrmotor.reminder;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACT = 1001;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;

    private EditText waInputKontak;

    private boolean sedangMenyimpan = false;

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
        layout.addView(emailInput);
        layout.addView(passwordInput);
        layout.addView(loginButton);

        setContentView(layout);
    }

    private void login() {

        String email =
                emailInput.getText().toString().trim();

        String password =
                passwordInput.getText().toString();

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

        mAuth.signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(
                        this,
                        task -> {

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

                                String pesan =
                                        "Login gagal";

                                if (task.getException() != null) {
                                    pesan =
                                            "Login gagal: "
                                                    + task.getException()
                                                    .getMessage();
                                }

                                Toast.makeText(
                                        this,
                                        pesan,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void tampilkanHalamanUtama() {

        sedangMenyimpan = false;

        ScrollView scrollView =
                new ScrollView(this);

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                30,
                40,
                30,
                30
        );

        TextView title =
                new TextView(this);

        title.setText(
                "🏍️ RR MOTOR REMINDER"
        );

        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(
                0,
                0,
                0,
                20
        );

        Button tambahButton =
                new Button(this);

        tambahButton.setText(
                "➕ TAMBAH REMINDER"
        );

        tambahButton.setOnClickListener(
                v -> tampilkanFormTambah()
        );

        Button lihatButton =
                new Button(this);

        lihatButton.setText(
                "📋 ANTRIAN REMINDER"
        );

        lihatButton.setOnClickListener(
                v -> tampilkanAntrian()
        );

        Button logoutButton =
                new Button(this);

        logoutButton.setText(
                "LOGOUT"
        );

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

    private void tampilkanFormTambah() {

        sedangMenyimpan = false;

        ScrollView scrollView =
                new ScrollView(this);

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                30,
                30,
                30,
                30
        );

        TextView title =
                new TextView(this);

        title.setText(
                "➕ TAMBAH REMINDER"
        );

        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);

        title.setPadding(
                0,
                0,
                0,
                25
        );

        layout.addView(title);

        EditText nama =
                buatInput(
                        "Nama konsumen (opsional)"
                );

        // ==========================
        // NOMOR WHATSAPP + KONTAK
        // ==========================

        LinearLayout waLayout =
                new LinearLayout(this);

        waLayout.setOrientation(
                LinearLayout.HORIZONTAL
        );

        waInputKontak =
                buatInput(
                        "Nomor WhatsApp"
                );

        waInputKontak.setInputType(
                InputType.TYPE_CLASS_PHONE
        );

        LinearLayout.LayoutParams waParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        waParams.setMargins(
                0,
                0,
                10,
                10
        );

        waInputKontak.setLayoutParams(
                waParams
        );

        Button kontakButton =
                new Button(this);

        kontakButton.setText(
                "📱 KONTAK"
        );

        LinearLayout.LayoutParams kontakParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        kontakParams.setMargins(
                0,
                0,
                0,
                10
        );

        kontakButton.setLayoutParams(
                kontakParams
        );

        kontakButton.setOnClickListener(
                v -> bukaKontak()
        );

        waLayout.addView(
                waInputKontak
        );

        waLayout.addView(
                kontakButton
        );

        layout.addView(waLayout);

        EditText motor =
                buatInput(
                        "Jenis motor (opsional)"
                );

        EditText nopol =
                buatInput(
                        "Nopol (opsional)"
                );

        EditText km =
                buatInput(
                        "KM saat ganti oli (opsional)"
                );

        km.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        layout.addView(motor);
        layout.addView(nopol);
        layout.addView(km);

        // ==========================
        // TANGGAL INPUT MANUAL
        // ==========================

        TextView tanggalLabel =
                new TextView(this);

        tanggalLabel.setText(
                "📅 Tanggal Input"
        );

        tanggalLabel.setTextSize(17);
        tanggalLabel.setPadding(
                0,
                10,
                0,
                5
        );

        layout.addView(
                tanggalLabel
        );

        EditText tanggalInput =
                buatInput(
                        "DD/MM/YYYY"
                );

        tanggalInput.setInputType(
                InputType.TYPE_CLASS_DATETIME |
                        InputType.TYPE_DATETIME_VARIATION_DATE
        );

        tanggalInput.setSingleLine(true);

        layout.addView(
                tanggalInput
        );

        // ==========================
        // PILIH REMINDER
        // ==========================

        TextView reminderLabel =
                new TextView(this);

        reminderLabel.setText(
                "⏰ Pilih waktu pengingat (wajib)"
        );

        reminderLabel.setTextSize(17);

        reminderLabel.setPadding(
                0,
                15,
                0,
                5
        );

        layout.addView(
                reminderLabel
        );

        RadioGroup reminderGroup =
                new RadioGroup(this);

        reminderGroup.setOrientation(
                RadioGroup.VERTICAL
        );

        RadioButton satuBulan =
                new RadioButton(this);

        satuBulan.setText(
                "1 bulan"
        );

        satuBulan.setTextSize(17);

        RadioButton duaBulan =
                new RadioButton(this);

        duaBulan.setText(
                "2 bulan"
        );

        duaBulan.setTextSize(17);

        reminderGroup.addView(
                satuBulan
        );

        reminderGroup.addView(
                duaBulan
        );

        layout.addView(
                reminderGroup
        );

        Button simpanButton =
                new Button(this);

        simpanButton.setText(
                "💾 SIMPAN REMINDER"
        );

        Button kembaliButton =
                new Button(this);

        kembaliButton.setText(
                "← KEMBALI"
        );

        layout.addView(
                simpanButton
        );

        layout.addView(
                kembaliButton
        );

        simpanButton.setOnClickListener(v -> {

            if (sedangMenyimpan) {
                return;
            }

            String namaText =
                    nama.getText()
                            .toString()
                            .trim();

            String waText =
                    waInputKontak
                            .getText()
                            .toString()
                            .trim();

            String motorText =
                    motor.getText()
                            .toString()
                            .trim();

            String nopolText =
                    nopol.getText()
                            .toString()
                            .trim();

            String kmText =
                    km.getText()
                            .toString()
                            .trim();

            String tanggalText =
                    tanggalInput
                            .getText()
                            .toString()
                            .trim();

            // WA wajib
            if (waText.isEmpty()) {

                waInputKontak.setError(
                        "Nomor WhatsApp wajib diisi"
                );

                waInputKontak.requestFocus();
                return;
            }

            // Tanggal wajib
            if (tanggalText.isEmpty()) {

                tanggalInput.setError(
                        "Tanggal input wajib diisi"
                );

                tanggalInput.requestFocus();
                return;
            }

            // Cek format tanggal
            Date tanggalInputDate =
                    parseTanggal(
                            tanggalText
                    );

            if (tanggalInputDate == null) {

                tanggalInput.setError(
                        "Format tanggal harus DD/MM/YYYY"
                );

                tanggalInput.requestFocus();
                return;
            }

            // Reminder wajib dipilih
            int pilihan =
                    reminderGroup
                            .getCheckedRadioButtonId();

            if (pilihan == -1) {

                Toast.makeText(
                        this,
                        "⚠️ Pilih 1 bulan atau 2 bulan",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            int bulan;

            if (pilihan == satuBulan.getId()) {
                bulan = 1;
            } else {
                bulan = 2;
            }

            int kmSekarang = 0;

            if (!kmText.isEmpty()) {

                try {

                    kmSekarang =
                            Integer.parseInt(
                                    kmText
                            );

                } catch (Exception e) {

                    km.setError(
                            "KM tidak valid"
                    );

                    km.requestFocus();
                    return;
                }
            }

            int kmMaksimal = 0;
            int kmTerakhir = 0;

            if (kmSekarang > 0) {

                kmMaksimal =
                        kmSekarang + 1500;

                kmTerakhir =
                        kmSekarang + 2000;
            }

            sedangMenyimpan = true;

            simpanButton.setEnabled(
                    false
            );

            simpanButton.setText(
                    "⏳ MENYIMPAN..."
            );

            simpanReminder(
                    namaText,
                    waText,
                    motorText,
                    nopolText,
                    kmSekarang,
                    bulan,
                    tanggalText,
                    tanggalInputDate,
                    kmMaksimal,
                    kmTerakhir,
                    simpanButton
            );
        });

        kembaliButton.setOnClickListener(v -> {

            if (sedangMenyimpan) {

                Toast.makeText(
                        this,
                        "Mohon tunggu, sedang menyimpan...",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            tampilkanHalamanUtama();
        });

        scrollView.addView(
                layout
        );

        setContentView(
                scrollView
        );
    }

    private EditText buatInput(
            String hint
    ) {

        EditText input =
                new EditText(this);

        input.setHint(hint);
        input.setTextSize(16);
        input.setPadding(
                15,
                15,
                15,
                15
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                0,
                0,
                10
        );

        input.setLayoutParams(
                params
        );

        return input;
    }

    // ==========================
    // KONTAK HP
    // ==========================

    private void bukaKontak() {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_PICK,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                    );

            startActivityForResult(
                    intent,
                    REQUEST_CONTACT
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Kontak HP tidak dapat dibuka",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (
                requestCode == REQUEST_CONTACT
                        && resultCode == RESULT_OK
                        && data != null
        ) {

            Uri contactUri =
                    data.getData();

            if (contactUri == null) {
                return;
            }

            Cursor cursor = null;

            try {

                cursor =
                        getContentResolver()
                                .query(
                                        contactUri,
                                        new String[]{
                                                ContactsContract.CommonDataKinds.Phone.NUMBER
                                        },
                                        null,
                                        null,
                                        null
                                );

                if (
                        cursor != null
                                && cursor.moveToFirst()
                ) {

                    int index =
                            cursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                            );

                    if (index >= 0) {

                        String nomor =
                                cursor.getString(
                                        index
                                );

                        if (
                                waInputKontak != null
                        ) {

                            waInputKontak.setText(
                                    nomor
                            );
                        }
                    }
                }

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        "Nomor kontak tidak dapat diambil",
                        Toast.LENGTH_LONG
                ).show();

            } finally {

                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    // ==========================
    // SIMPAN FIREBASE
    // ==========================

    private void simpanReminder(
            String nama,
            String wa,
            String motor,
            String nopol,
            int km,
            int bulan,
            String tanggalInput,
            Date tanggalInputDate,
            int kmMaksimal,
            int kmTerakhir,
            Button simpanButton
    ) {

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "nama",
                nama
        );

        data.put(
                "wa",
                wa
        );

        data.put(
                "motor",
                motor
        );

        data.put(
                "nopol",
                nopol
        );

        data.put(
                "km",
                km
        );

        data.put(
                "bulan",
                bulan
        );

        data.put(
                "tanggalInput",
                tanggalInput
        );

        data.put(
                "tanggalGantiOli",
                tanggalInput
        );

        data.put(
                "kmMaksimal",
                kmMaksimal
        );

        data.put(
                "kmTerakhir",
                kmTerakhir
        );

        data.put(
                "status",
                "BELUM JATUH TEMPO"
        );

        data.put(
                "terkirim",
                false
        );

        data.put(
                "createdAt",
                com.google.firebase.firestore.FieldValue
                        .serverTimestamp()
        );

        Calendar kalender =
                Calendar.getInstance();

        kalender.setTime(
                tanggalInputDate
        );

        kalender.add(
                Calendar.MONTH,
                bulan
        );

        long waktuReminder =
                kalender.getTimeInMillis();

        data.put(
                "tanggalReminderMillis",
                waktuReminder
        );

        db.collection(
                        "reminders"
                )
                .add(data)
                .addOnSuccessListener(
                        documentReference -> {

                            sedangMenyimpan =
                                    false;

                            jadwalkanNotifikasi(
                                    documentReference.getId(),
                                    nama,
                                    wa,
                                    motor,
                                    nopol,
                                    kmMaksimal,
                                    kmTerakhir,
                                    bulan,
                                    waktuReminder
                            );

                            Toast.makeText(
                                    this,
                                    "✅ Reminder berhasil disimpan",
                                    Toast.LENGTH_LONG
                            ).show();

                            tampilkanHalamanUtama();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            sedangMenyimpan =
                                    false;

                            simpanButton
                                    .setEnabled(true);

                            simpanButton
                                    .setText(
                                            "💾 SIMPAN REMINDER"
                                    );

                            Toast.makeText(
                                    this,
                                    "❌ Gagal menyimpan:\n"
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    private Date parseTanggal(
            String tanggal
    ) {

        try {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                    );

            format.setLenient(false);

            return format.parse(
                    tanggal
            );

        } catch (Exception e) {

            return null;
        }
    }

    // ==========================
    // NOTIFIKASI
    // ==========================

    private void jadwalkanNotifikasi(
            String documentId,
            String nama,
            String wa,
            String motor,
            String nopol,
            long kmMaksimal,
            long kmTerakhir,
            long bulan,
            long waktuReminder
    ) {

        AlarmManager alarmManager =
                (AlarmManager)
                        getSystemService(
                                Context.ALARM_SERVICE
                        );

        Intent intent =
                new Intent(
                        this,
                        ReminderReceiver.class
                );

        intent.putExtra(
                "documentId",
                documentId
        );

        intent.putExtra(
                "nomorWA",
                wa
        );

        intent.putExtra(
                "nama",
                nama
        );

        intent.putExtra(
                "motor",
                motor
        );

        intent.putExtra(
                "nopol",
                nopol
        );

        intent.putExtra(
                "kmMaksimal",
                kmMaksimal
        );

        intent.putExtra(
                "kmTerakhir",
                kmTerakhir
        );

        intent.putExtra(
                "bulan",
                bulan
        );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        documentId.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        if (
                android.os.Build.VERSION.SDK_INT >=
                        android.os.Build.VERSION_CODES.S
        ) {

            if (
                    !alarmManager
                            .canScheduleExactAlarms()
            ) {

                Toast.makeText(
                        this,
                        "Izin alarm tepat waktu belum aktif.",
                        Toast.LENGTH_LONG
                ).show();

                try {

                    Intent settingIntent =
                            new Intent(
                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                            );

                    startActivity(
                            settingIntent
                    );

                } catch (Exception ignored) {
                }

                return;
            }
        }

        if (
                android.os.Build.VERSION.SDK_INT >=
                        android.os.Build.VERSION_CODES.M
        ) {

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    waktuReminder,
                    pendingIntent
            );

        } else {

            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    waktuReminder,
                    pendingIntent
            );
        }
    }

    // ==========================
    // ANTRIAN
    // ==========================

    private void tampilkanAntrian() {

        ScrollView scrollView =
                new ScrollView(this);

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                30,
                30,
                30,
                30
        );

        TextView title =
                new TextView(this);

        title.setText(
                "📋 ANTRIAN REMINDER"
        );

        title.setTextSize(24);
        title.setGravity(
                Gravity.CENTER
        );

        title.setPadding(
                0,
                0,
                0,
                25
        );

        layout.addView(title);

        TextView loading =
                new TextView(this);

        loading.setText(
                "Memuat data..."
        );

        loading.setTextSize(17);

        layout.addView(
                loading
        );

        Button kembali =
                new Button(this);

        kembali.setText(
                "← KEMBALI"
        );

        kembali.setOnClickListener(
                v -> tampilkanHalamanUtama()
        );

        scrollView.addView(
                layout
        );

        setContentView(
                scrollView
        );

        db.collection(
                        "reminders"
                )
                .orderBy(
                        "tanggalReminderMillis",
                        Query.Direction.ASCENDING
                )
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            loading.setVisibility(
                                    View.GONE
                            );

                            if (
                                    queryDocumentSnapshots
                                            .isEmpty()
                            ) {

                                TextView kosong =
                                        new TextView(this);

                                kosong.setText(
                                        "Belum ada reminder."
                                );

                                kosong.setTextSize(
                                        18
                                );

                                layout.addView(
                                        kosong
                                );

                            } else {

                                for (
                                        DocumentSnapshot document :
                                        queryDocumentSnapshots
                                ) {

                                    tampilkanReminder(
                                            layout,
                                            document
                                    );
                                }
                            }

                            layout.addView(
                                    kembali
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            loading.setText(
                                    "Gagal mengambil data:\n"
                                            + e.getMessage()
                            );

                            layout.addView(
                                    kembali
                            );
                        }
                );
    }

    private void tampilkanReminder(
            LinearLayout layout,
            DocumentSnapshot document
    ) {

        String nama =
                stringValue(
                        document,
                        "nama"
                );

        String wa =
                stringValue(
                        document,
                        "wa"
                );

        String motor =
                stringValue(
                        document,
                        "motor"
                );

        String nopol =
                stringValue(
                        document,
                        "nopol"
                );

        String tanggalInput =
                stringValue(
                        document,
                        "tanggalInput"
                );

        long km =
                longValue(
                        document,
                        "km"
                );

        long kmMaksimal =
                longValue(
                        document,
                        "kmMaksimal"
                );

        long kmTerakhir =
                longValue(
                        document,
                        "kmTerakhir"
                );

        long bulan =
                longValue(
                        document,
                        "bulan"
                );

        String status =
                stringValue(
                        document,
                        "status"
                );

        boolean terkirim =
                Boolean.TRUE.equals(
                        document.getBoolean(
                                "terkirim"
                        )
                );

        TextView item =
                new TextView(this);

        StringBuilder teks =
                new StringBuilder();

        teks.append(
                "━━━━━━━━━━━━━━━━━━\n"
        );

        if (!nama.isEmpty()) {

            teks.append(
                    "👤 "
            )
                    .append(nama)
                    .append("\n");
        }

        teks.append(
                "📱 "
        )
                .append(wa)
                .append("\n");

        if (!motor.isEmpty()) {

            teks.append(
                    "🏍️ "
            )
                    .append(motor)
                    .append("\n");
        }

        if (!nopol.isEmpty()) {

            teks.append(
                    "🔖 "
            )
                    .append(nopol)
                    .append("\n");
        }

        if (!tanggalInput.isEmpty()) {

            teks.append(
                    "📅 Tanggal Input: "
            )
                    .append(tanggalInput)
                    .append("\n");
        }

        teks.append(
                "⏰ Reminder: "
        )
                .append(bulan)
                .append(" bulan\n");

        if (km > 0) {

            teks.append(
                    "🔧 KM: "
            )
                    .append(
                            formatAngka(km)
                    )
                    .append("\n");

            teks.append(
                    "KM maksimal: "
            )
                    .append(
                            formatAngka(
                                    kmMaksimal
                            )
                    )
                    .append("\n");

            teks.append(
                    "⚠️ Paling lambat: "
            )
                    .append(
                            formatAngka(
                                    kmTerakhir
                            )
                    )
                    .append("\n");
        }

        teks.append(
                "📌 Status: "
        )
                .append(status)
                .append("\n");

        teks.append(
                terkirim
                        ? "✅ Sudah diingatkan\n"
                        : "⏳ Belum dikirim\n"
        );

        teks.append(
                "━━━━━━━━━━━━━━━━━━"
        );

        item.setText(
                teks.toString()
        );

        item.setTextSize(
                16
        );

        item.setPadding(
                0,
                15,
                0,
                15
        );

        layout.addView(
                item
        );

        Button waButton =
                new Button(this);

        waButton.setText(
                "💬 KIRIM WA"
        );

        waButton.setOnClickListener(
                v ->
                        bukaWhatsApp(
                                document.getId(),
                                wa,
                                nama,
                                motor,
                                nopol,
                                kmMaksimal,
                                kmTerakhir,
                                bulan
                        )
        );

        layout.addView(
                waButton
        );
    }

    private String stringValue(
            DocumentSnapshot document,
            String field
    ) {

        String value =
                document.getString(
                        field
                );

        return value == null
                ? ""
                : value;
    }

    private long longValue(
            DocumentSnapshot document,
            String field
    ) {

        Long value =
                document.getLong(
                        field
                );

        return value == null
                ? 0
                : value;
    }

    // ==========================
    // WHATSAPP
    // ==========================

    private void bukaWhatsApp(
            String documentId,
            String nomor,
            String nama,
            String motor,
            String nopol,
            long kmMaksimal,
            long kmTerakhir,
            long bulan
    ) {

        String nomorWA =
                nomor.replaceAll(
                        "[^0-9+]",
                        ""
                );

        if (
                nomorWA.startsWith("0")
        ) {

            nomorWA =
                    "62"
                            + nomorWA.substring(1);
        }

        if (
                nomorWA.startsWith("+")
        ) {

            nomorWA =
                    nomorWA.substring(1);
        }

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

        pesan.append(
                "Halo Bapak/Ibu 👋\n\n"
        );

        // Nama hanya muncul jika diisi
        if (!nama.isEmpty()) {

            pesan.append(
                    "👤 "
            )
                    .append(nama)
                    .append("\n\n");
        }

        pesan.append(
                barisBulan
        )
                .append("\n\n");

        pesan.append(
                "Kami dari RR MOTOR ingin mengingatkan bahwa kendaraan Anda sudah memasuki jadwal pengecekan/penggantian oli. 🔧\n\n"
        );

        pesan.append(
                "✨ Pengecekan kondisi oli GRATIS!\n\n"
        );

        // Motor hanya muncul jika diisi
        if (!motor.isEmpty()) {

            pesan.append(
                    "🏍️ Jenis motor: "
            )
                    .append(motor)
                    .append("\n");
        }

        // Nopol hanya muncul jika diisi
        if (!nopol.isEmpty()) {

            pesan.append(
                    "🔖 Nopol: "
            )
                    .append(nopol)
                    .append("\n");
        }

        // KM hanya muncul jika diisi
        if (kmMaksimal > 0) {

            pesan.append(
                    "\n🔧 KM maksimal penggantian oli: "
            )
                    .append(
                            formatAngka(
                                    kmMaksimal
                            )
                    )
                    .append(" KM\n");

            pesan.append(
                    "⚠️ Paling lambat penggantian oli: "
            )
                    .append(
                            formatAngka(
                                    kmTerakhir
                            )
                    )
                    .append(" KM\n");
        }

        pesan.append(
                "\nSilakan datang ke RR MOTOR untuk pengecekan kondisi oli dan kendaraan Anda.\n\n"
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

        pesan.append(
                "RR MOTOR"
        );

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

            startActivity(
                    intent
            );

            db.collection(
                            "reminders"
                    )
                    .document(
                            documentId
                    )
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

    private String formatAngka(
            long angka
    ) {

        return String.format(
                Locale.US,
                "%,d",
                angka
        ).replace(
                ",",
                "."
        );
    }
}
