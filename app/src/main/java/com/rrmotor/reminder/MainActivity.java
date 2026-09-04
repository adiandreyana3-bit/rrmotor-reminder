package com.rrmotor.reminder;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);

        TextView title = new TextView(this);
        title.setText("🏍️ RR MOTOR REMINDER");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 30);

        TextView info = new TextView(this);
        info.setText(
                "Sistem Pengingat Ganti Oli\n\n" +
                "Data konsumen akan masuk ke antrian reminder.\n\n" +
                "⏰ Pengingat: 1 bulan / 2 bulan\n" +
                "🔧 KM maksimal: +1.500 KM\n" +
                "⚠️ Paling lambat: +500 KM\n\n" +
                "Firebase dan QR Code akan ditambahkan pada tahap berikutnya."
        );
        info.setTextSize(18);

        layout.addView(title);
        layout.addView(info);

        setContentView(layout);
    }
}
