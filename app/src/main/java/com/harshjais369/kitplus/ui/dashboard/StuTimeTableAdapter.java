package com.harshjais369.kitplus.ui.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.harshjais369.kitplus.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class StuTimeTableAdapter extends RecyclerView.Adapter<StuTimeTableAdapter.ViewHolder> {

        private Context context;
        private final ArrayList<StuTimeTableModel> stuTimeTableModelArrayList;

        private final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        private int dayToday;

        public StuTimeTableAdapter(Context context, ArrayList<StuTimeTableModel> stuTimeTableModelArrayList) {
            this.context = context;
            this.stuTimeTableModelArrayList = stuTimeTableModelArrayList;
//            Set today's day
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
            calendar.setTime(new Date());
            int tmpDayToday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
            dayToday = (tmpDayToday < 0) ? 6 : tmpDayToday;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.time_table_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            StuTimeTableModel stuTimeTableModel = stuTimeTableModelArrayList.get(dayToday);

            String period = stuTimeTableModel.getPeriods()[position];
            String subject = stuTimeTableModel.getSubjects()[position];
            String teacher = stuTimeTableModel.getTeachers()[position];
            String room = stuTimeTableModel.getRooms()[position];
            String attendance = stuTimeTableModel.getAttendances()[position];

            holder.period.setText(period);
            holder.subject.setText(subject);
            holder.teacher.setText(teacher);
            holder.room.setText(room);
            holder.attendance.setText(attendance);

            if (subject.equalsIgnoreCase("LUNCH") || subject.equalsIgnoreCase("LIBRARY")) {
                holder.subject.setTextColor(context.getResources().getColor(R.color.material_red));
                holder.teacher.setVisibility(View.GONE);
                holder.room.setVisibility(View.GONE);
                holder.attendance.setVisibility(View.GONE);
            }

            if (attendance.equalsIgnoreCase("Present")) {
                holder.attendance.setBackgroundColor(context.getResources().getColor(R.color.material_green));
            } else if (attendance.equalsIgnoreCase("Absent")) {
                holder.attendance.setBackgroundColor(context.getResources().getColor(R.color.material_red));
            } else {
                holder.attendance.setBackgroundColor(context.getResources().getColor(R.color.material_yellow));
            }

        }

        @Override
        public int getItemCount() {
            return stuTimeTableModelArrayList.get(dayToday).getPeriodCount();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView period;
            private final TextView subject;
            private final TextView teacher;
            private final TextView room;
            private final TextView attendance;

            public ViewHolder(View itemView) {
                super(itemView);
                period = itemView.findViewById(R.id.tv_period_timeTableItem);
                subject = itemView.findViewById(R.id.tv_subjectName_timeTableItem);
                teacher = itemView.findViewById(R.id.tv_subjectTeacher_timeTableItem);
                room = itemView.findViewById(R.id.tv_roomNo_timeTableItem);
                attendance = itemView.findViewById(R.id.tv_attendance_timeTableItem);
            }
        }
}
