package com.harshjais369.kitplus.ui.dashboard;

import static android.content.Context.MODE_PRIVATE;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.harshjais369.kitplus.MainActivity;
import com.harshjais369.kitplus.R;
import com.harshjais369.kitplus.databinding.FragmentDashboardBinding;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private static final String TAG = "DashboardFragment";

    TextView txtLastLoginHome;
    RecyclerView timeTableRecyclerView;

    ArrayList<StuTimeTableModel> stuTimeTableModelArrayList;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        MainActivity mainActivity = (MainActivity) getActivity();

        txtLastLoginHome = binding.txtLastLoginHome;
        timeTableRecyclerView = binding.recyclerViewHome;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), txtLastLoginHome::setText);

        setLastVisit(mainActivity);
        setTimeTableModel(mainActivity, true);

        return view;
    }

    private void setLastVisit(MainActivity contextActivity) {
        if (contextActivity == null) return;
        String url = getString(R.string.erp_domain) + "/Home.aspx/getLastVisit";
        String method = "POST";
        String params = "";
        new Thread(() -> {
            String resp = contextActivity.sendRequest(url, method, params);
            contextActivity.runOnUiThread(() -> {
                if (resp == null) {
                    Toast.makeText(contextActivity.getApplicationContext(), "Failed to fetch response from server",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                String[] respArr = resp.split(":::");
                if (!respArr[0].equals("200")) {
                    Toast.makeText(contextActivity.getApplicationContext(), "Error: Server returned code " + respArr[0],
                            Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    String lastVisit_html = new JSONObject(respArr[1]).getString("d");
                    TextView textView = binding.txtLastLoginHome;
                    textView.setText(convertHtmlToText(lastVisit_html));
                } catch (Exception e) {
                    Toast.makeText(contextActivity.getApplicationContext(), "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    public void setTimeTableModel(@NonNull MainActivity contextActivity, boolean isViewUpdate) {
        SharedPreferences currUserPref = contextActivity.getApplicationContext().getSharedPreferences("com.harshjais369.kitplus.currUser", MODE_PRIVATE);
        final String[] stuAfnNo = {currUserPref.getString("stuAfnNo", null)};
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", getResources().getConfiguration().locale);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String todayStr = sdf.format(new Date());
        String url = getString(R.string.erp_domain) + "/json/TimeTableWS.asmx/StudentTimeTableWithAttendance";
        String method = "POST";
        String params = "{\"student\":\"" + stuAfnNo[0] + "\",\"attendanceDate\":\"" + todayStr + "\",\"withAttendanceStatus\":1}";
        new Thread(() -> {
            while (stuAfnNo[0] == null) {
                try {
                    Thread.sleep(2000);
                    System.out.println("Waiting for stuAfnNo...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stuAfnNo[0] = currUserPref.getString("stuAfnNo", null);
            }
            System.out.println(params);
            String res = contextActivity.sendRequest(url, method, params);
            try {
                if (res == null)
                    contextActivity.runOnUiThread(() -> Toast.makeText(contextActivity.getApplicationContext(), "Failed to fetch response from server",
                            Toast.LENGTH_LONG).show());
                String[] resArr = res.split(":::");
                if (!resArr[0].equals("200")) {
                    contextActivity.runOnUiThread(() -> Toast.makeText(contextActivity.getApplicationContext(), "Error: Server returned code " + resArr[0],
                            Toast.LENGTH_LONG).show());
                } else {
                    try {
                        stuTimeTableModelArrayList = new ArrayList<>();
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
//                                Element attndce = col.select("span").get(0);
                            }
                            timeTable_arr[i] = cols_arr;
                        }
//                        for (String[] arr : timeTable_arr) System.out.println("/> "+ Arrays.toString(arr));

//                        Remove first element from each row
                        for (int i = 0; i < timeTable_arr.length; i++) {
                            String[] tmp = new String[timeTable_arr[i].length - 1];
                            System.arraycopy(timeTable_arr[i], 1, tmp, 0, tmp.length);
                            timeTable_arr[i] = tmp;
                        }
//                        Remove first row with backup of it in periodsData_arr
                        String[] periodsData_arr = timeTable_arr[0];
                        timeTable_arr = Arrays.copyOfRange(timeTable_arr, 1, timeTable_arr.length);

                        int periodCount = periodsData_arr.length;
                        for (String[] strings : timeTable_arr) {
                            int periodCount_lastOffset = 0;
                            String[] tmp_subjectsData_arr = new String[periodCount];
                            String[] tmp_teachersData_arr = new String[periodCount];
                            String[] tmp_roomsData_arr = new String[periodCount];
                            String[] tmp_attendancesData_arr = new String[periodCount];
                            for (int j = 0; j < periodCount; j++) {
                                if (Objects.equals(strings[j], "")) {
                                    periodCount_lastOffset++;
                                    continue;
                                }
//                                Split string with ")" and "[" characters
                                String[] tmp_str = strings[j].split("[)\\[]");
                                if (!(tmp_str[0].contains("LUNCH") || tmp_str[0].contains("LIBRARY")))
                                    tmp_subjectsData_arr[j] = tmp_str[0] + ")";
                                else tmp_subjectsData_arr[j] = tmp_str[0].trim();
                                if (tmp_str.length < 2) {
                                    tmp_teachersData_arr[j] = "";
                                    tmp_roomsData_arr[j] = "";
                                    tmp_attendancesData_arr[j] = "";
                                } else {
                                    tmp_teachersData_arr[j] = "" + tmp_str[1].split(" academic", 2)[0].trim();
                                    tmp_roomsData_arr[j] = "[" + tmp_str[2].split("Attendance", 2)[0].trim();
                                    String[] tmp2_str = tmp_str[2].split("]Attendance", 2);
                                    if (tmp2_str.length < 2) tmp_attendancesData_arr[j] = "";
                                    else tmp_attendancesData_arr[j] = tmp2_str[1].replace(":", "").trim();
                                    System.out.println("tmp_attendancesData_arr[" + j + "] = " + tmp_attendancesData_arr[j]);
                                }
                            }
                            StuTimeTableModel model = new StuTimeTableModel(periodCount - periodCount_lastOffset);
                            model.setPeriods(periodsData_arr);
                            model.setSubjects(tmp_subjectsData_arr);
                            model.setTeachers(tmp_teachersData_arr);
                            model.setRooms(tmp_roomsData_arr);
                            model.setAttendances(tmp_attendancesData_arr);
                            stuTimeTableModelArrayList.add(model);
                        }

                        for (StuTimeTableModel model : stuTimeTableModelArrayList) {
                            System.out.println("Periods: " + Arrays.toString(model.getPeriods()));
                            System.out.println("Subjects: " + Arrays.toString(model.getSubjects()));
                            System.out.println("Teachers: " + Arrays.toString(model.getTeachers()));
                            System.out.println("Rooms: " + Arrays.toString(model.getRooms()));
                        }

                        if (isViewUpdate) contextActivity.runOnUiThread(() -> {
                            timeTableRecyclerView.setLayoutManager(new LinearLayoutManager(contextActivity.getApplicationContext(), RecyclerView.VERTICAL, false));
                            binding.recyclerViewHome.setAdapter(new StuTimeTableAdapter(contextActivity.getApplicationContext(), stuTimeTableModelArrayList));
                        });

                    } catch (Exception e) {
                        if (e instanceof JSONException && e.getCause() instanceof IndexOutOfBoundsException) {
//                        Session expired
                            Log.e(TAG, "setUserTimeTable: Session expired!", e.getCause());
                            contextActivity.runOnUiThread(() -> {
                                Toast.makeText(contextActivity.getApplicationContext(), "Session expired! Please login again",
                                        Toast.LENGTH_LONG).show();
                                contextActivity.logout(contextActivity);
                            });
                        } else {
                            Log.e(TAG, "setUserTimeTable: Error in parsing student time table", e);
                            contextActivity.runOnUiThread(() -> Toast.makeText(contextActivity.getApplicationContext(), "Invalid server response",
                                    Toast.LENGTH_LONG).show());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "setUserTimeTable: Error in fetching student time table", e);
                contextActivity.runOnUiThread(() -> Toast.makeText(contextActivity.getApplicationContext(), "Failed to fetch student time table",
                        Toast.LENGTH_LONG).show());
            }
        }).start();

    }


    String convertHtmlToText(@NonNull String htmlSnippet) {
        // Remove HTML tags using regular expression
        String text = htmlSnippet.replaceAll("<[^>]*>", "");
        // Remove extra whitespaces and trim the result
        text = text.replaceAll("\\s+", " ").trim();
        return text;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}