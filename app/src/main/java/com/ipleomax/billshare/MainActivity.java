package com.ipleomax.billshare;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BILL-SHARE";

    private DatabaseHelper database;

    private BottomNavigationView navigation;

    private LinearLayout layoutNew;
    private LinearLayout layoutCurrent;
    private LinearLayout layoutMonths;

    private EditText inputBillNumber;
    private EditText inputAmount;
    private EditText inputDate; //Do not use

    private EditText inputDateDay;
    private Spinner inputDateMonth;
    private EditText inputDateMonth2;
    private EditText inputDateYear;

    private Button buttonSave;
    private TextView labelBillAdded;

    private ListView listViewCurrent;
    private ListView listViewMonths;
    private BillAdapter billAdapter;
    private MonthAdapter monthAdapter;

    private ArrayList<Bill> billList = new ArrayList<Bill>();
    private ArrayList<Date> monthList = new ArrayList<Date>();
    private Date selectedMonth;

    private ArrayList<Bill> billListLimited = new ArrayList<Bill>();

    private final static String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_new:
                    switchLayout(layoutNew);
                    //mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_current:
                    switchLayout(layoutCurrent);
                    //mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_months:
                    switchLayout(layoutMonths);
                    //mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    private void switchLayout(LinearLayout layout) {
        layoutNew.setVisibility(LinearLayout.GONE);
        layoutCurrent.setVisibility(LinearLayout.GONE);
        layoutMonths.setVisibility(LinearLayout.GONE);
        layout.setVisibility(LinearLayout.VISIBLE);

        if(layout == layoutNew) {
            setTitle(R.string.title_new);
        } else if(layout == layoutCurrent) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy");
            setTitle(dateFormat.format(selectedMonth));
        } else if(layout == layoutMonths) {
            setTitle(R.string.title_months);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.title_new);

        database = new DatabaseHelper(getApplicationContext());

        Cursor res = database.selectAllBills();
        if(res.getCount() != 0) {
            StringBuffer buffer = new StringBuffer();
            while(res.moveToNext()) {
                long id = res.getLong(0);
                int billNumber = res.getInt(1);
                double amount = res.getDouble(2);
                String dateString = res.getString(3);

                SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                try {
                    Date date = iso8601Format.parse(dateString);
                    billList.add(new Bill(id, billNumber, amount, date));
                } catch (ParseException ex) {
                    continue;
                }
            }
        }

        layoutNew = (LinearLayout) findViewById(R.id.layoutNew);
        layoutCurrent = (LinearLayout) findViewById(R.id.layoutCurrent);
        layoutMonths = (LinearLayout) findViewById(R.id.layoutMonths);

        inputBillNumber = (EditText) findViewById(R.id.inputBillNumber);
        inputAmount = (EditText) findViewById(R.id.inputAmount);

        inputDate = (EditText) findViewById(R.id.inputDate); //do not use

        inputDateDay = (EditText) findViewById(R.id.inputDateDay);
        //inputDateMonth = (Spinner) findViewById(R.id.inputDateMonth);
        inputDateMonth2 = (EditText) findViewById(R.id.inputDateMonth2);
        inputDateYear = (EditText) findViewById(R.id.inputDateYear);

        //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        //R.array.months_array, android.R.layout.simple_spinner_item);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //inputDateMonth.setAdapter(adapter);

        inputDateDay.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "31")});
        inputDateMonth2.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "12")});
        inputDateYear.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "2100")});

        labelBillAdded = (TextView) findViewById(R.id.labelBillAdded);

        listViewCurrent = (ListView) findViewById(R.id.listCurrent);
        billAdapter = new BillAdapter(this, billListLimited);
        listViewCurrent.setAdapter(billAdapter);

        listViewCurrent.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                // TODO Auto-generated method stub

                Log.v("long clicked","pos: " + pos);

                final int itemPos = pos;

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:

                                Bill bill = billListLimited.get(itemPos);
                                if(database.removeBill(bill) > 0) {
                                    //Now update the views
                                    billListLimited.remove(bill);
                                    billList.remove(bill);

                                    sortBillsById();
                                    //sortBillsByNumber();

                                    refreshMonthList();
                                    refreshBillList();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Database error", Toast.LENGTH_SHORT).show();
                                }

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                Bill bill = billListLimited.get(itemPos);

                AlertDialog.Builder builder = new AlertDialog.Builder(arg1.getContext());
                builder.setMessage("Do you want to remove bill #" + Long.toString(bill.number) + " with amount " + Double.toString(bill.amount) + "?");
                builder.setPositiveButton("Yes", dialogClickListener);
                builder.setNegativeButton("No", dialogClickListener);
                builder.show();

                return true;
            }
        });

        listViewMonths = (ListView) findViewById(R.id.listMonths);
        monthAdapter = new MonthAdapter(this, monthList);
        listViewMonths.setAdapter(monthAdapter);

        listViewMonths.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long id) {

                //Toast.makeText(getApplicationContext(),"Title => " + monthList.get(position), Toast.LENGTH_SHORT).show();

                selectedMonth = monthList.get(position);
                refreshBillList();

                navigation.setSelectedItemId(R.id.navigation_current);
                switchLayout(layoutCurrent);
            }
        });

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_new);

        selectedMonth = new Date();

        //Test
        //billList.add(new Bill(1001, 100, Bill.parseDate("1/1/2018")));
        //billList.add(new Bill(1002, 200, Bill.parseDate("7/1/2018")));
        //billList.add(new Bill(1003, 300, Bill.parseDate("12/2/2018")));
        //billList.add(new Bill(1004, 400, Bill.parseDate("15/2/2018")));
        //billList.add(new Bill(1005, 500, Bill.parseDate("23/4/2018")));
        //Log.i("MainActivity::onCreate", billMonthList().toString());

        sortBillsById();
        //sortBillsByNumber();

        refreshMonthList();
        refreshBillList();

        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                labelBillAdded.setVisibility(View.INVISIBLE);

                int billNumber = 0;
                double amount = 0;
                Date date = null;
                //String dateText = inputDate.getText().toString();

                try {
                    billNumber = Integer.parseInt(inputBillNumber.getText().toString());
                    amount = Double.parseDouble(inputAmount.getText().toString());
                    //date = Bill.parseDate(dateText);

                    String day = inputDateDay.getText().toString().trim();
                    String month = inputDateMonth2.getText().toString().trim();
                    String year = inputDateYear.getText().toString().trim();
                    String dateText = "";

                    if(day.length() != 0 && month.length() != 0 && year.length() != 0) {
                        if(year.length() == 4) {
                            dateText = day + "/" + month + "/" + year;
                        } else if(year.length() == 2) {
                            dateText = day + "/" + month + "/20" + year;
                        }  else if(year.length() == 1) {
                            dateText = day + "/" + month + "/200" + year;
                        }

                        date = new SimpleDateFormat("dd/MM/yyyy").parse(dateText);

                        //Date parsedDate = new SimpleDateFormat("dd/MM/yyyy").parse(dateText);
                        //date = Bill.parseDate(day + "/" + month + "/" + year);
                    } else {
                        date = new Date();
                    }

                } catch(Exception ex) {
                    Toast.makeText(getApplicationContext(), "Invalid Input(s)", Toast.LENGTH_SHORT).show();
                    return;
                }

                /*Bill existingBill = null;

                for(Bill b : billList) {
                    if(b.number == billNumber) {
                        existingBill = b;
                        //Toast.makeText(getApplicationContext(), "Duplicate Bill!", Toast.LENGTH_SHORT).show();
                        //return;
                    }
                }*/

                if(billNumber <= 0) {
                    Toast.makeText(getApplicationContext(), "Bill # Invalid", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(amount <= 0) {
                    Toast.makeText(getApplicationContext(), "Amount Invalid", Toast.LENGTH_SHORT).show();
                    return;
                }

                //if(existingBill == null && date == null) {
                if(date == null) {
                    Toast.makeText(getApplicationContext(), "Date Invalid", Toast.LENGTH_SHORT).show();
                    return;
                }

                String billStatus = "";

                /*if(existingBill != null) {
                    //Update the existing bill
                    existingBill.amount += amount;
                    if(database.updateBill(existingBill) == 0) {
                        Toast.makeText(getApplicationContext(), "Database Error!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedMonth = existingBill.date;

                    String newAmount = Double.toString(existingBill.amount);
                    billStatus = "Bill #" + Integer.toString(billNumber) + " total " + newAmount;
                } else {
                    //Create a new bill and save it
                    Bill bill = new Bill(billNumber, amount, date);
                    if(!database.insertBill(bill)) {
                        Toast.makeText(getApplicationContext(), "Database Error!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    billList.add(bill);
                    selectedMonth = bill.date;

                    billStatus = "Bill #" + Integer.toString(billNumber) + " added!";
                }*/

                //Create a new bill and save it
                Bill bill = new Bill(0, billNumber, amount, date);
                long insertId = database.insertBill(bill);
                if(insertId == -1) {
                    Toast.makeText(getApplicationContext(), "Database Error!", Toast.LENGTH_SHORT).show();
                    return;
                }
                bill.id = insertId;

                billList.add(bill);
                selectedMonth = bill.date;

                billStatus = "Bill #" + Integer.toString(billNumber) + " added!";

                inputBillNumber.setText("");
                inputAmount.setText("");
                //inputDate.setText("");

                labelBillAdded.setText(billStatus.toUpperCase());
                labelBillAdded.setVisibility(View.VISIBLE);

                //Now update the views
                refreshMonthList();
                refreshBillList();
            }
        });

        //isStoragePermissionGranted();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_share) {

            if(isStoragePermissionGranted())
                exportFileAndShare();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortBillsById() {
        Collections.sort(billListLimited, new Comparator<Bill>() {
            @Override
            public int compare(Bill b1, Bill b2) {
                if (b1.id < b2.id)
                    return 1;
                if (b1.id > b2.id)
                    return -1;
                return 0;
            }
        });
    }

    private void sortBillsByNumber() {
        Collections.sort(billListLimited, new Comparator<Bill>() {
            @Override
            public int compare(Bill b1, Bill b2) {
                if (b1.number < b2.number)
                    return 1;
                if (b1.number > b2.number)
                    return -1;
                return 0;
            }
        });
    }

    private void sortBillsByDate() {
        Collections.sort(billListLimited, new Comparator<Bill>() {
            @Override
            public int compare(Bill b1, Bill b2) {
                if (b1.date.before(b2.date))
                    return 1;
                if (b1.date.after(b2.date))
                    return -1;
                return 0;
            }
        });
    }

    private void refreshBillList() {
        billListLimited.clear();

        for(Bill b : billList) {
            if(selectedMonth.getMonth() != b.date.getMonth() || selectedMonth.getYear() != b.date.getYear())
                continue;

            billListLimited.add(b);
        }

        sortBillsById();
        //sortBillsByNumber();
        billAdapter.notifyDataSetChanged();
    }

    private void refreshMonthList() {
        monthList.clear();
        //ArrayList<Date> monthList = new ArrayList<Date>();

        for(Bill b : billList) {
            boolean exists = false;

            for(Date d : monthList) {
                if(d.getMonth() == b.date.getMonth() && d.getYear() == b.date.getYear()) {
                    exists = true;
                    break;
                }
            }

            if(!exists)
                monthList.add(b.date);
        }

        Collections.sort(monthList, new Comparator<Date>() {
            @Override
            public int compare(Date b1, Date b2) {
                if (b1.before(b2))
                    return 1;
                if (b1.after(b2))
                    return -1;
                return 0;
            }
        });

        monthAdapter.notifyDataSetChanged();
    }

    private void exportFileAndShare() {
        if(billListLimited.size() == 0) {
            Toast.makeText(getApplicationContext(),"No bills to share.", Toast.LENGTH_SHORT).show();
            return;
        }

        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy");
        String fileName = "Bills (" + dateFormat.format(selectedMonth) + ").csv";
        String data = "number, amount, date\n";

        for(Bill b : billListLimited) {
            data += b.toString() + "\n";
        }

        try {
            File file = new File(baseDir + fileName);
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(data.getBytes());
            stream.close();

            Log.i("Success", "File saved successfully: " + baseDir + fileName);

            shareFile(baseDir + fileName);
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Failed to save file on phone.", Toast.LENGTH_SHORT).show();
            Log.e("Exception", "Failed to save file on phone: " + e.toString());
        }
    }

    private void shareFile(String filePath) {

        File f = new File(filePath);

        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        File fileWithinMyDir = new File(filePath);

        if (fileWithinMyDir.exists()) {
            intentShareFile.setType("text/*");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
            this.startActivity(Intent.createChooser(intentShareFile, f.getName()));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+ permissions[0] + " was " + grantResults[0]);
            //resume tasks needing this permission

            exportFileAndShare();
        } else {
            Toast.makeText(getApplicationContext(), "Sharing won't work without storage permissions!", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }
}
