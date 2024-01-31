package com.harshjais369.kitplus.ui.home;

import java.util.HashMap;

public class StuTimeTableModel {
    public static int periodCount = 0;
    public static HashMap<Integer, String> periodMap;
    public static HashMap<Integer, String> teacherMap;
    public static HashMap<Integer, String> roomMap;
    public static HashMap<Integer, String> dayMap;
    public static HashMap<Integer, String> timeMap;

    public StuTimeTableModel() {
        periodMap = new HashMap<>();
        teacherMap = new HashMap<>();
        roomMap = new HashMap<>();
        dayMap = new HashMap<>();
        timeMap = new HashMap<>();

//        dayMap.put(1, "Monday");
//        periodMap.put(1, "Ecology");
//        teacherMap.put(1, "Mr. A");
//        roomMap.put(1, "Room 1");
//        timeMap.put(1, "9:10 AM");
    }
}
