
package com.kaan.wificheck;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;

public class MainActivity extends Activity {

    LinearLayout layout;
    TextView status, network, ip, signal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createScreen();
        refreshInfo();
    }

    private TextView makeText(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setPadding(0, 12, 0, 12);
        return t;
    }

    private void createScreen() {
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(28, 35, 28, 25);
        layout.setBackgroundColor(Color.rgb(10, 17, 32));

        TextView title = makeText("WiFiCheck", 30, Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        layout.addView(title);

        TextView subtitle = makeText(
            "Wi-Fi Ağ Kontrol Paneli", 15,
            Color.rgb(150, 175, 210)
        );
        layout.addView(subtitle);

        status = makeText("Bağlantı kontrol ediliyor...", 19, Color.GREEN);
        network = makeText("Ağ adı: Bilinmiyor", 17, Color.WHITE);
        ip = makeText("IP adresi: Bilinmiyor", 17, Color.WHITE);
        signal = makeText("Sinyal: Bilinmiyor", 17, Color.WHITE);

        addCard(status);
        addCard(network);
        addCard(ip);
        addCard(signal);

        Button refresh = new Button(this);
        refresh.setText("Bilgileri Yenile");
        refresh.setTextColor(Color.WHITE);
        refresh.setBackgroundColor(Color.rgb(30, 110, 220));
        refresh.setOnClickListener(v -> refreshInfo());

        layout.addView(refresh);
        setContentView(layout);
    }

    private void addCard(TextView view) {
        view.setPadding(20, 20, 20, 20);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(22, 34, 55));
        bg.setCornerRadius(22);
        view.setBackground(bg);

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );

        params.setMargins(0, 12, 0, 0);
        layout.addView(view, params);
    }

    private void refreshInfo() {
        try {
            WifiManager wifi = (WifiManager)
                getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo info = cm.getActiveNetworkInfo();

            if (info != null && info.isConnected()
                    && info.getType() == ConnectivityManager.TYPE_WIFI) {

                status.setText("● Wi-Fi bağlantısı aktif");
                status.setTextColor(Color.GREEN);

                WifiInfo wifiInfo = wifi.getConnectionInfo();

                String ssid = wifiInfo.getSSID();
                if (ssid != null && !ssid.equals("<unknown ssid>")) {
                    network.setText("Ağ adı: " + ssid.replace("\"", ""));
                } else {
                    network.setText("Ağ adı: Android izni gerekli olabilir");
                }

                int ipValue = wifiInfo.getIpAddress();
                String ipAddress = String.format(
                    "%d.%d.%d.%d",
                    ipValue & 255,
                    (ipValue >> 8) & 255,
                    (ipValue >> 16) & 255,
                    (ipValue >> 24) & 255
                );

                ip.setText("IP adresi: " + ipAddress);

                int level = wifiInfo.getRssi();
                signal.setText("Sinyal gücü: " + level + " dBm");

            } else {
                status.setText("● Wi-Fi bağlantısı bulunamadı");
                status.setTextColor(Color.RED);
                network.setText("Ağ adı: -");
                ip.setText("IP adresi: -");
                signal.setText("Sinyal gücü: -");
            }

        } catch (Exception e) {
            status.setText("Bilgiler alınamadı.");
        }
    }
}
