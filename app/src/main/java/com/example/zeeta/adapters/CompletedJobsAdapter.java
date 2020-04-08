package com.example.zeeta.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.zeeta.R;
import com.example.zeeta.models.CompletedJobs;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class CompletedJobsAdapter extends ArrayAdapter<CompletedJobs> {

    public CompletedJobsAdapter(@NonNull Context context, ArrayList<CompletedJobs> jobsInfos, int resource) {
        super(context, 0, jobsInfos);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {

            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.jobs_list_items, parent, false);
        }

        CompletedJobs jobsInfo = getItem(position);

        // Find the TextView in the list_item.xml layout with the ID version_name
        TextView nameTextView = (TextView) listItemView.findViewById(R.id.employeeName);
        nameTextView.setText("Name: " + jobsInfo.getName());


        // Find the TextView in the list_item.xml layout with the ID version_name
        TextView jobType = (TextView) listItemView.findViewById(R.id.employeeJob);
        jobType.setText("Job: " + jobsInfo.getJob());

        // Find the TextView in the list_item.xml layout with the ID version_name
        TextView jobStatus = (TextView) listItemView.findViewById(R.id.job_status);
        jobType.setText("Job Status: " + jobsInfo.getStatus());

        // Find the TextView in the list_item.xml layout with the ID version_name
        TextView dateTextView = (TextView) listItemView.findViewById(R.id.dateRendered);
        dateTextView.setText("Date: " + jobsInfo.getDateRendered());

        View textContainer = listItemView.findViewById(R.id.job_details_container);

        // find the color
        int color = ContextCompat.getColor(getContext(), R.color.White);

        // set the background color of the text view
        textContainer.setBackgroundColor(color);

        // Return the whole list item layout
        // so that it can be shown in the ListView
        return listItemView;
    }
}