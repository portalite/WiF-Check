
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
import android.widget.ScrollView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends Activity {

    LinearLayout layout;
    TextView status, network, ip, signal, devices;

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
        ScrollView scroll = new ScrollView(this);

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

        TextView deviceTitle = makeText(
            "Ağa Bağlı Cihazlar", 21, Color.WHITE
        );
        deviceTitle.setTypeface(null, Typeface.BOLD);
        layout.addView(deviceTitle);

        devices = makeText(
            "Cihazlar kontrol ediliyor...", 15, Color.WHITE
        );
        addCard(devices);

        Button refresh = new Button(this);
        refresh.setText("Bilgileri Yenile");
        refresh.setTextColor(Color.WHITE);
        refresh.setBackgroundColor(Color.rgb(30, 110, 220));
        refresh.setOnClickListener(v -> refreshInfo());

        layout.addView(refresh);

        scroll.addView(layout);
        setContentView(scroll);
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

                readDevices();

            } else {
                status.setText("● Wi-Fi bağlantısı bulunamadı");
                status.setTextColor(Color.RED);
                network.setText("Ağ adı: -");
                ip.setText("IP adresi: -");
                signal.setText("Sinyal gücü: -");
                devices.setText("Cihazları görmek için Wi-Fi'ye bağlan.");
            }

        } catch (Exception e) {
            status.setText("Bilgiler alınamadı.");
            devices.setText("Cihaz listesi okunamadı.");
        }
    }

    private void readDevices() {
        ArrayList<String> deviceList = new ArrayList<>();
        HashSet<String> seen = new HashSet<>();

        try {
            BufferedReader reader = new BufferedReader(
                new FileReader("/proc/net/arp")
            );

            String line;
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");

                if (parts.length >= 4) {
                    String deviceIp = parts[0];
                    String mac = parts[3];

                    if (mac.matches("(?i)([0-9a-f]{2}:){5}[0-9a-f]{2}")
                            && !mac.equalsIgnoreCase("00:00:00:00:00:00")
                            && seen.add(deviceIp)) {

                        deviceList.add(
                            "IP: " + deviceIp + "\nMAC: " + mac
                        );
                    }
                }
            }

            reader.close();

            if (deviceList.isEmpty()) {
                devices.setText(
                    "Şu anda görüntülenebilen cihaz bulunamadı.\n\n" +
                    "Android, bazı ağ cihazlarını gizleyebilir."
                );
            } else {
                StringBuilder result = new StringBuilder();

                result.append("Görüntülenebilen cihaz sayısı: ")
                      .append(deviceList.size())
                      .append("\n\n");

                for (int i = 0; i < deviceList.size(); i++) {
                    result.append("Cihaz ")
                          .append(i + 1)
                          .append("\n")
                          .append(deviceList.get(i))
                          .append("\n\n");
                }

                result.append(
                    "Not: Bu liste tüm bağlı cihazları göstermeyebilir."
                );

                devices.setText(result.toString());
            }

        } catch (Exception e) {
            devices.setText(
                "Cihaz listesi bu Android sürümünde okunamıyor olabilir."
            );
        }
    }
}
