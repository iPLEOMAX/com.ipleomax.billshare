package com.ipleomax.billshare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by iPLEOMAX on 03-Jul-18.
 */

public class BillAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Bill> mDataSource;

    public BillAdapter(Context context, ArrayList<Bill> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int i) {
        return mDataSource.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mDataSource.get(i).id;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // Get view for row item
        View rowView = mInflater.inflate(R.layout.list_item_bill, viewGroup, false);

        Bill bill = (Bill) getItem(i);

        TextView labelBillNumber = (TextView) rowView.findViewById(R.id.labelBillNumber);
        labelBillNumber.setText("#" + Integer.toString(bill.number));

        TextView labelAmount = (TextView) rowView.findViewById(R.id.labelAmount);
        //String amt = Math.round(bill.amount * 100) / 100);
        String amt = Double.toString(bill.amount);
        labelAmount.setText(amt + " AED");

        TextView labelDate = (TextView) rowView.findViewById(R.id.labelDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        labelDate.setText(dateFormat.format(bill.date));

        return rowView;
    }
}
