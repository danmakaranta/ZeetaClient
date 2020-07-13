package com.example.zeeta.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.zeeta.R;
import com.example.zeeta.TransactionData;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class TransactionsAdapter extends ArrayAdapter<TransactionData> {

    public TransactionsAdapter(@NonNull Context context, ArrayList<TransactionData> transactionInformation, int resource) {
        super(context, 0, transactionInformation);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.transaction_list_item, parent, false);
        }

        TransactionData transactionData = getItem(position);

        TextView details = listItemView.findViewById(R.id.transaction_details);
        String tempDetails = transactionData.getDetail();
        details.setText(tempDetails);

        TextView transactionType = listItemView.findViewById(R.id.transaction_type);
        String tempType = transactionData.getType();
        transactionType.setText(tempType);

        TextView transactionDate = listItemView.findViewById(R.id.transaction_date);
        Timestamp tempDate = transactionData.getDate();
        if (tempDate != null) {
            Date dt = tempDate.toDate();
            String day = "" + dt.getDay();
            String month = "" + dt.getMonth();
            String year = "" + dt.getYear();
            //transactionDate.setText(day+"/"+month+"/"+year);
            transactionDate.setText("" + dt.toLocaleString());

        } else {
            transactionDate.setText("Date:");
        }


        TextView transactionAmount = listItemView.findViewById(R.id.transaction_amount);
        long tempAmount = transactionData.getAmountPaid();
        transactionAmount.setText("N" + tempAmount);

        View textContainer = listItemView.findViewById(R.id.transaction_list_container);
        // find the color
        int color = ContextCompat.getColor(getContext(), R.color.White);

        // Return the whole list item layout
        // so that it can be shown in the ListView
        return listItemView;
    }
}

