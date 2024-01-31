package com.harshjais369.kitplus;

public class StuInfoModel {
    private String name, course, branch, semester, section, afn, roll, photoUrl;
    private String[] stuHiddenInfo_arr;

    public StuInfoModel(String name, String course, String branch, String semester, String section,
                        String afn, String roll, String photoUrl, String[] stuHiddenInfo_arr) {
        this.name = name;
        this.course = course;
        this.branch = branch;
        this.semester = semester;
        this.section = section;
        this.afn = afn;
        this.roll = roll;
        this.photoUrl = photoUrl;
        this.stuHiddenInfo_arr = stuHiddenInfo_arr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getAfn() {
        return afn;
    }

    public void setAfn(String afn) {
        this.afn = afn;
    }

    public String getRoll() {
        return roll;
    }

    public void setRoll(String roll) {
        this.roll = roll;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String[] getStuHiddenInfo_arr() {
        return stuHiddenInfo_arr;
    }

    public void setStuHiddenInfo_arr(String[] stuHiddenInfo_arr) {
        this.stuHiddenInfo_arr = stuHiddenInfo_arr;
    }
}
