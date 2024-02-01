package com.harshjais369.kitplus.ui.dashboard;

public class StuTimeTableModel {
    private int periodCount;

    private String[] periods;
    private String[] subjects;
    private String[] teachers;
    private String[] rooms;
    private String[] attendances;

    public StuTimeTableModel(int periodCount) {
        this.periodCount = periodCount;
        this.periods = new String[periodCount];
        this.subjects = new String[periodCount];
        this.teachers = new String[periodCount];
        this.rooms = new String[periodCount];
        this.attendances = new String[periodCount];
    }

    public int getPeriodCount() {
        return periodCount;
    }

    public String[] getPeriods() {
        return periods;
    }

    public void setPeriods(String[] periods) {
        this.periods = periods;
    }

    public String[] getSubjects() {
        return subjects;
    }

    public void setSubjects(String[] subjects) {
        this.subjects = subjects;
    }

    public String[] getTeachers() {
        return teachers;
    }

    public void setTeachers(String[] teachers) {
        this.teachers = teachers;
    }

    public String[] getRooms() {
        return rooms;
    }

    public void setRooms(String[] rooms) {
        this.rooms = rooms;
    }

    public String[] getAttendances() {
        return attendances;
    }

    public void setAttendances(String[] attendances) {
        this.attendances = attendances;
    }
}
