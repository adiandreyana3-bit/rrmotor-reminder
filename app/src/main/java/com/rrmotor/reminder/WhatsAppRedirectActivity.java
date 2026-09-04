package com.rrmotor.reminder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URLEncoder;

public class WhatsAppRedirectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String nomorWA =
                getIntent().getStringExtra("nomorWA");

        String nama =
                getIntent().getStringExtra("nama");

        long kmMaksimal =
                getIntent().getLongExtra(
                        "kmMaksimal",
                        0
                );

        long kmTerakhir =
                getIntent().getLongExtra(
                        "kmTerakhir",
                        0
                );

        long bulan =
                getIntent().getLongExtra(
                        "bulan",
                        1
                );

        if (nomorWA == null || nomorWA.isEmpty()) {
            finish();
            return;
        }

        nomorWA = nomorWA.replaceAll(
                "[^0-9+]",
                ""
        );

        if (nomorWA.startsWith("0")) {
            nomorWA =
                    "62" +
                            nomorWA.substring(1);
        }

        if (nomorWA.startsWith("+")) {
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

        pesan.append(
                barisBulan
        ).append("\n\n");

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
                    .append(
                            formatAngka(kmMaksimal)
                    )
                    .append(" KM\n");

            pesan.append(
                    "⚠️ Paling lambat penggantian oli: "
            )
                    .append(
                            formatAngka(kmTerakhir)
                    )
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

            startActivity(intent);

        } catch (Exception e) {
            // Tidak melakukan apa-apa jika WhatsApp tidak dapat dibuka
        }

        finish();
    }

    private String formatAngka(long angka) {

        return String.format(
                java.util.Locale.US,
                "%,d",
                angka
        ).replace(",", ".");
    }
}
