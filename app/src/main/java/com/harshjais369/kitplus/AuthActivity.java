package com.harshjais369.kitplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {

    EditText eTxt_userId, eTxt_password, eTxt_captcha;
    Button btn_login;

    String userId, password, captcha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        eTxt_userId = findViewById(R.id.eTxt_userId);
        eTxt_password = findViewById(R.id.eTxt_psk);
        eTxt_captcha = findViewById(R.id.eTxt_captcha);
        btn_login = findViewById(R.id.btn_login);

        btn_login.setOnClickListener(v -> {
            userId = eTxt_userId.getText().toString().trim();
            password = eTxt_password.getText().toString().trim();
            captcha = eTxt_captcha.getText().toString().trim();
            String url = "https://erp.kit.ac.in/json/UserServiceWS.asmx/AuthenticateUser";
            String method = "POST";
            String params = "{\"institute\": \"" + "b2e19fe8-1bb5-4a02-8953-9e0e42ae850f\","
                    + "\"openFor\": \"" + "Students" + "\","
                    + "\"vcaptcha\": \"" + captcha + "\","
                    + "\"userId\": \"" + userId + "\","
                    + "\"passwds\": \"" + password + "\""
                    + "}";
            Log.println(Log.INFO, "params", params);
            new Thread(() -> {
                String res = sendRequest(url, method, params);
                if (res == null || res.equals("")) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to fetch response from server",
                            Toast.LENGTH_LONG).show());
                    return;
                }
                String[] resArr = res.split(":::");
                if (!resArr[0].equals("200")) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error: Server returned code " + resArr[0],
                            Toast.LENGTH_LONG).show());
                    return;
                }
                try {
                    JSONObject resObj = new JSONObject(resArr[1]);
                    JSONObject resObj2 = resObj.getJSONObject("d");
                    int loginStatus = resObj2.getInt("LoginStatus");
                    String loginMessage = resObj2.getString("Message");
                    if (loginStatus == 15) {
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = getSharedPreferences("com.harshjais369.kitplus.currUser", MODE_PRIVATE).edit();
                            editor.putString("currUser", userId);
                            try {
                                editor.putString("profilePicUrl", getString(R.string.erp_domain) + resObj2.getString("ProfilePicUrl"));
                            } catch (JSONException e) {
                                editor.putString("profilePicUrl", null);
                                e.printStackTrace();
                            }
                            editor.apply();
                            startActivity(new Intent(AuthActivity.this, MainActivity.class));
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), loginMessage,
                                Toast.LENGTH_LONG).show());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Invalid server response",
                            Toast.LENGTH_LONG).show());
                }
            }).start();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already logged in
        if (validateUserAuth()) {
            Intent intent = new Intent(AuthActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            //  send get request to fetch captcha
            String url = "https://erp.kit.ac.in/captchaHandler.ashx?query";
            new Thread(() -> {
                try {
                    URL urlObj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
                    con.setDoInput(true);
                    con.connect();
                    InputStream input = con.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    runOnUiThread(() -> {
                        ImageView imageView = findViewById(R.id.imgVw_captcha);
                        imageView.setImageBitmap(bitmap);
                    });
//                    Save cookies in shared preferences
                    SharedPreferences.Editor editor = getSharedPreferences("com.harshjais369.kitplus.cookies", MODE_PRIVATE).edit();
                    for (int i = 0; i < con.getHeaderFields().size(); i++) {
                        String hKey = con.getHeaderFieldKey(i);
                        if (hKey == null) continue;
                        String hVal = con.getHeaderField(i);
                        if (hKey.equals("Set-Cookie")) {
                            String[] cookieArr = hVal.split(";")[0].split("=", 2);
                            String cKey = cookieArr[0], cVal = cookieArr[1];
                            editor.putString(cKey, cVal);
                        }
                    }
                    editor.apply();
                    con.disconnect();
                } catch (Exception e) {
                    Log.d("AuthActivity", "onStart: Error in sending GET request to fetch captcha", e);
                }
            }).start();
        }
    }

    @Nullable
    private String sendRequest(String url, String method, String params) {
        try {
            SharedPreferences cookiesPref = getSharedPreferences("com.harshjais369.kitplus.cookies", MODE_PRIVATE);
            Map<String, ?> cookies = cookiesPref.getAll();
            StringBuilder cookieStr = new StringBuilder();
            for (Map.Entry<String, ?> entry : cookies.entrySet()) {
                cookieStr.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
            }
            URL urlObj = new URL(url);
            byte[] paramsBytes = params.getBytes(StandardCharsets.UTF_8);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Cookie", cookieStr.toString().trim());
            System.out.println(cookieStr.toString().trim());
            con.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Content-Length", String.valueOf(paramsBytes.length));
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Host", "erp.kit.ac.in");
            con.setRequestProperty("Origin", "https://erp.kit.ac.in");
            con.setRequestProperty("Referer", "https://erp.kit.ac.in/index.aspx?openFor=Students&amp;institute=b2e19fe8-1bb5-4a02-8953-9e0e42ae850f");
            con.setRequestProperty("Sec-Fetch-Dest", "empty");
            con.setRequestProperty("Sec-Fetch-Mode", "cors");
            con.setRequestProperty("Sec-Fetch-Site", "same-origin");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            con.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            con.setRequestProperty("sec-ch-ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"");
            con.setRequestProperty("sec-ch-ua-mobile", "?0");
            con.setRequestProperty("sec-ch-ua-platform", "\"Windows\"");
            if (method.equals("POST")) {
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.write(paramsBytes);
                wr.flush();
                wr.close();
            }
//            Save cookies in shared preferences
            SharedPreferences.Editor editor = getSharedPreferences("com.harshjais369.kitplus.cookies", MODE_PRIVATE).edit();
            for (int i = 0; i < 50; i++) {
                String hKey = con.getHeaderFieldKey(i);
                if (hKey == null) continue;
                String hVal = con.getHeaderField(i);
                if (hKey.equals("Set-Cookie")) {
                    String[] cookieArr = hVal.split(";")[0].split("=", 2);
                    String cKey = cookieArr[0], cVal = cookieArr[1];
                    editor.putString(cKey, cVal);
                }
            }
            editor.apply();

            StringBuffer response = new StringBuffer();
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) response.append(inputLine);
                in.close();
                Log.println(Log.INFO, "response", response.toString());
            }
            con.disconnect();
            return responseCode + ":::" + response;
        } catch (Exception e) {
            Log.e("AuthActivity", "Error in sendRequest", e);
        }
        return null;
    }

    private boolean validateUserAuth() {
        SharedPreferences cookiesPref = getSharedPreferences("com.harshjais369.kitplus.cookies", MODE_PRIVATE);
        SharedPreferences currUserPref = getSharedPreferences("com.harshjais369.kitplus.currUser", MODE_PRIVATE);
        String currUser = currUserPref.getString("currUser", null);
        Map<String, ?> cookies = cookiesPref.getAll();
        return currUser != null && cookies.size() > 0;
    }
}