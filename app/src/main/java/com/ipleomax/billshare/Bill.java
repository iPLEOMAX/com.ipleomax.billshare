package com.ipleomax.billshare;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by iPLEOMAX on 03-Jul-18.
 */

public class Bill {
    public long id;
    public int number;
    public double amount;
    public Date date;

    public Bill(long id, int number, double amount, Date date) {
        this.id = id;
        this.number = number;
        this.amount = amount;
        this.date = date;
    }

    public static Date parseDate(String dateText) {
        try {
            Date date = new SimpleDateFormat("dd/MM/yyyy").parse(dateText);
            if(date.getYear() < 100) {
                date.setYear(date.getYear() + 2000);
            }
            return date;
        } catch(ParseException ex) {
            return null;
        }
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String amt = Double.toString(this.amount);
        //double amt = Math.round(this.amount * 100) / 100;
        return Integer.toString(this.number) + ", " + amt + ", " + dateFormat.format(this.date);
    }
}
