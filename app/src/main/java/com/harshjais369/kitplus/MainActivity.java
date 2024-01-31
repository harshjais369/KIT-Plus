package com.harshjais369.kitplus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.harshjais369.kitplus.databinding.ActivityMainBinding;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private static final String TAG = "MainActivity";
    StuInfoModel stuInfoModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

//        TODO: work -----------------------------------------------------------------------


        binding.appBarMain.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
        binding.navView.getHeaderView(0).findViewById(R.id.nav_imgView_logout).setOnClickListener(v -> logout(MainActivity.this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        setStudentPic(binding);
        setStudentInfoModel(true, binding);
        setSubjectScheduleModel(binding, true);
    }

    private void setSubjectScheduleModel(@NonNull ActivityMainBinding binding, boolean isViewUpdate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", getResources().getConfiguration().locale);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String todayStr = sdf.format(new Date());
        String url = getString(R.string.erp_domain) + "/json/TimeTableWS.asmx/StudentTimeTableWithAttendance";
        String method = "POST";
        String params = "{\"student\":\"" + 232650 + "\",\"attendanceDate\":\"" + todayStr + "\",\"withAttendanceStatus\":1}";
        new Thread(() -> {
            String res = sendRequest(url, method, params);
            try {
                if (res == null)
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to fetch response from server",
                            Toast.LENGTH_LONG).show());
                String[] resArr = res.split(":::");
                if (!resArr[0].equals("200")) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error: Server returned code " + resArr[0],
                            Toast.LENGTH_LONG).show());
                } else {
                    try {
                        JSONObject resObj = new JSONObject(resArr[1]).getJSONObject("d");
                        String timeTableHtmlStr = resObj.getString("timeTable_ViewString_P");
                        Document doc = Jsoup.parse(timeTableHtmlStr);
                        Element tbody = doc.select("tbody").get(0);
                        Elements rows = tbody.select("tr");
                        String[][] timeTable_arr = new String[rows.size()][];
                        for (int i = 0; i < rows.size(); i++) {
                            Element row = rows.get(i);
                            Elements cols = row.select("td");
                            if (i==0)
                                cols = row.select("th");
                            String[] cols_arr = new String[cols.size()];
                            for (int j = 0; j < cols.size(); j++) {
                                Element col = cols.get(j);
                                cols_arr[j] = col.text();
//                                TODO: get subject, faculty, roomNo and attendance
                                Element attndce = col.select("span").get(0);
                            }
                            timeTable_arr[i] = cols_arr;
                        }
                        for (String[] arr : timeTable_arr) System.out.println("/> "+Arrays.toString(arr));
//                        if (isViewUpdate) runOnUiThread(() -> setStudentDashboard_view(stuInfoModel, binding));
                    } catch (Exception e) {
                        if (e instanceof JSONException && e.getCause() instanceof IndexOutOfBoundsException) {
//                        Session expired
                            Log.e(TAG, "setUserTimeTable: Session expired!", e.getCause());
                            runOnUiThread(() -> {
                                Toast.makeText(getApplicationContext(), "Session expired! Please login again",
                                        Toast.LENGTH_LONG).show();
                                logout(MainActivity.this);
                            });
                        } else {
                            Log.e(TAG, "setUserTimeTable: Error in parsing student time table", e);
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Invalid server response",
                                    Toast.LENGTH_LONG).show());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "setUserTimeTable: Error in fetching student time table", e);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to fetch student time table",
                        Toast.LENGTH_LONG).show());
            }
        }).start();

    }

    private void setStudentDashboard_view(@NonNull StuInfoModel model, @NonNull ActivityMainBinding binding) {
        NavigationView navigationView = binding.navView;
        TextView txtView_title = navigationView.getHeaderView(0).findViewById(R.id.nav_txtView_headerTitle);
        TextView txtView_subtitle = navigationView.getHeaderView(0).findViewById(R.id.nav_txtView_headerSubtitle);
        txtView_title.setText(model.getName());
        txtView_subtitle.setText("AFN: " + model.getAfn());

    }

    void setStudentInfoModel(boolean isViewUpdate, ActivityMainBinding binding) {
        String url = "https://erp.kit.ac.in/home.aspx/getStuDashboard_Details";
        String method = "POST";
        String params = "";
        new Thread(() -> {
            String res = sendRequest(url, method, params);
            try {
                if (res == null)
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to fetch response from server",
                            Toast.LENGTH_LONG).show());
                String[] resArr = res.split(":::");
                if (!resArr[0].equals("200")) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error: Server returned code " + resArr[0],
                            Toast.LENGTH_LONG).show());
                } else {
                    try {
                        JSONObject resObj = new JSONObject(resArr[1]).getJSONArray("d").getJSONObject(0);
                        String stuName = resObj.getString("studentFullName_P");
                        String stuCourse = resObj.getString("prgName_P");
                        String stuBranch = resObj.getString("branch_P");
                        String stuSemester = resObj.getString("stuSem_P");
                        String stuSection = resObj.getString("section_P");
                        String stuAfnNo = resObj.getString("stuRegistrationCode_P");
                        String stuRollNo = resObj.getString("rollno_P");
                        String stuPhotoUrl = getString(R.string.erp_domain) + resObj.getString("photoUrl_P");
                        String[] stuHiddenInfo_arr = {
                                resObj.getString("StudentId_P"),
                                resObj.getString("prgId_P"),
                                resObj.getString("branchId_P"),
                                stuSemester
                        };
                        SharedPreferences.Editor editor = getSharedPreferences("com.harshjais369.kitplus.currUser", MODE_PRIVATE).edit();
                        editor.putString("profilePicUrl", stuPhotoUrl).apply();
                        stuInfoModel = new StuInfoModel(stuName, stuCourse, stuBranch, stuSemester, stuSection,
                                stuAfnNo, stuRollNo, stuPhotoUrl, stuHiddenInfo_arr);
                        if (isViewUpdate) runOnUiThread(() -> setStudentDashboard_view(stuInfoModel, binding));
                    } catch (Exception e) {
                        if (e instanceof JSONException && e.getCause() instanceof IndexOutOfBoundsException) {
//                        Session expired
                            Log.e(TAG, "setUserDashboard: Session expired!", e.getCause());
                            runOnUiThread(() -> {
                                Toast.makeText(getApplicationContext(), "Session expired! Please login again",
                                        Toast.LENGTH_LONG).show();
                                logout(MainActivity.this);
                            });
                        } else {
                            Log.e(TAG, "setUserDashboard: Error in parsing student dashboard details", e);
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Invalid server response",
                                    Toast.LENGTH_LONG).show());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "setUserDashboard: Error in fetching student dashboard details", e);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to fetch student dashboard details",
                        Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    void setStudentPic(@NonNull ActivityMainBinding binding) {
        // Fetch student image from link via HTTP request
        ImageView studentImage = binding.navView.getHeaderView(0).findViewById(R.id.nav_imgView_headerImage);
        new Thread(() -> {
            try {
                SharedPreferences currUserPref = getSharedPreferences("com.harshjais369.kitplus.currUser", MODE_PRIVATE);
                String profilePicUrl = currUserPref.getString("profilePicUrl", null);
                URL url = new URL(profilePicUrl);
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                runOnUiThread(() -> studentImage.setImageBitmap(bmp));
            } catch (Exception e) {
                Log.e(TAG, "setUserDashboard: Error in fetching student image", e);
            }
        }).start();
    }

    void logout(@NonNull Activity contextActivity) {
        SharedPreferences.Editor editor = getSharedPreferences("com.harshjais369.kitplus.cookies", MODE_PRIVATE).edit();
        editor.clear().apply();
        editor = getSharedPreferences("com.harshjais369.kitplus.currUser", MODE_PRIVATE).edit();
        editor.clear().apply();
        contextActivity.startActivity(new Intent(contextActivity, AuthActivity.class));
        contextActivity.finish();
    }

    @Nullable
    public String sendRequest(String url, String method, String params) {
        try {
            SharedPreferences cookiesPref = getSharedPreferences("com.harshjais369.kitplus.cookies", MODE_PRIVATE);
            Map<String, ?> cookies = cookiesPref.getAll();
            StringBuilder cookieStr = new StringBuilder();
            for (Map.Entry<String, ?> entry : cookies.entrySet())
                cookieStr.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
            URL urlObj = new URL(url);
            byte[] paramsBytes = params.getBytes(StandardCharsets.UTF_8);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Cookie", cookieStr.toString().trim());
            System.out.println(cookieStr.toString().trim());
            con.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            con.setRequestProperty("Accept-Language", "en-IN,en;q=0.9,hi;q=0.8");
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Content-Length", String.valueOf(paramsBytes.length));
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Host", "erp.kit.ac.in");
            con.setRequestProperty("Origin", "https://erp.kit.ac.in");
            con.setRequestProperty("Referer", "https://erp.kit.ac.in/Dashboard?title=Dashboard&fi=11265171");
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
//            Save cookies in shared preferences ---------------------------------------------------
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
//            Get response -------------------------------------------------------------------------
            StringBuffer response = new StringBuffer();
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                Log.println(Log.INFO, "response", response.toString());
            }
            con.disconnect();
            return responseCode + ":::" + response;
        } catch (Exception e) {
            Log.e(TAG, "sendRequest: Error occurred!", e);
        }
        return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        // This method is called when the user clicks on the back button
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}