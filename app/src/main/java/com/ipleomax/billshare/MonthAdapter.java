package com.ipleomax.billshare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by iPLEOMAX on 03-Jul-18.
 */

public class MonthAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Date> mDataSource;

    public MonthAdapter(Context context, ArrayList<Date> items) {
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
        //Date date = mDataSource.get(i);
        //String id = Integer.toString(date.getMonth());
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = mInflater.inflate(R.layout.list_item_month, viewGroup, false);

        Date date = (Date) getItem(i);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy");

        TextView labelDate = (TextView) rowView.findViewById(R.id.labelMonth);
        labelDate.setText(dateFormat.format(date));

        return rowView;
    }
}
