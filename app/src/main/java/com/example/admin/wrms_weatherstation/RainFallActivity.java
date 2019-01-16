package com.example.admin.wrms_weatherstation;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RainFallActivity extends AppCompatActivity {

    private ArrayList<WeatherSample> weatherSamples = new ArrayList<WeatherSample>();
    RecyclerView recyclerView;
    Spinner dateSpinner;
    Spinner imeiSpinner;
    DBAdapter db;
    private EditText  fromDate, toDate;
    Button getButton;
    String selectedIMEI = null;
    DateAdapter adapterrrr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.rainfall_activity);

        db = new DBAdapter(this);
        db.open();
        getButton = (Button)findViewById(R.id.get_button);

        imeiSpinner = (Spinner)findViewById(R.id.spinner_imei);

        fromDate = (EditText) findViewById(R.id.edit_text_from);
        toDate = (EditText) findViewById(R.id.edit_text_to);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        dateSpinner = (Spinner) findViewById(R.id.date_spinner);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager ddLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(ddLayoutManager);
        // ddRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(this, R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        recyclerView.addItemDecoration(horizontalDecoration);
        String filePath = Environment.getExternalStorageDirectory().toString() + "/rainfall.txt";
        Log.v("filepAthhhh_todb", filePath);
        File file = new File(filePath);
        if (file!=null) {
           //readWeatherData(file);
        }
        ArrayList<String> spinnerList = new ArrayList<String>();
        spinnerList.add("Today");
        spinnerList.add("Yesterday");
        spinnerList.add("Last 7days");
        spinnerList.add("Last 30days");
        ArrayAdapter<String> dateSpinerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerList);
        dateSpinerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.dateSpinner.setAdapter(dateSpinerAdapter);

        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String fromDDD = fromDate.getText().toString().trim();
                String toDDD = toDate.getText().toString().trim();
                ArrayList<String> dateeeeLIST = new ArrayList<String>();
                ArrayList<String> imeiLIST = new ArrayList<String>();
                ArrayList<String> totalRainfallList = new ArrayList<String>();
                final ArrayList<String> imeiSpinnerLIST = new ArrayList<String>();
                imeiSpinnerLIST.add("Select IMEI");
                if (fromDDD!=null && fromDDD.length()>9) {

                    if (toDDD!=null && toDDD.length()>9) {

                        Cursor betweenCur = db.getBetweenData(fromDDD, toDDD);
                        Log.v("betweenDayCount", "-" + betweenCur.getCount() + "--------" + betweenCur.getCount());
                        if (betweenCur.moveToFirst()) {
                            do {
                                String dattaa = betweenCur.getString(betweenCur.getColumnIndex(DBAdapter.DATE));
                                String imeii = betweenCur.getString(betweenCur.getColumnIndex(DBAdapter.IMEI));
                                Log.v("kjskdj", dattaa + "");
                                dateeeeLIST.add(dattaa);
                                imeiLIST.add(imeii);
                                imeiSpinnerLIST.add(imeii);


                                if (dattaa!=null && dattaa.length()>4) {
                                    double total = 0.0;
                                    Cursor dateByCursor = db.getDataByDate(dattaa,imeii);
                                    // Cursor dateByCursor = db.getAllData();
                                    Log.v("dateByCursor_count", "," + dateByCursor.getCount());
                                    if (dateByCursor.moveToFirst()) {
//
                                        do {
                                            String rainfallll = dateByCursor.getString(dateByCursor.getColumnIndex(DBAdapter.RAINFALL));
                                            if (rainfallll!=null){
                                                 total = total+Double.parseDouble(rainfallll);
                                            }
                                        }
                                        while (dateByCursor.moveToNext());
                                    }
                                    totalRainfallList.add(""+total);
                                }



                            }
                            while (betweenCur.moveToNext());
                        }
                    }else {
                        Toast.makeText(getApplicationContext(),"Please select TO Date",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"Please select FROM Date",Toast.LENGTH_SHORT).show();
                }


                Set<String> set = new HashSet<String>(imeiSpinnerLIST);
                imeiSpinnerLIST.clear();
                imeiSpinnerLIST.addAll(set);

                ArrayAdapter<String> imeiAdapter = new ArrayAdapter<String>(RainFallActivity.this, android.R.layout.simple_spinner_item, imeiSpinnerLIST);
                imeiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                imeiSpinner.setAdapter(imeiAdapter);

                imeiSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (i>0) {
                            selectedIMEI = imeiSpinnerLIST.get(i);
                            if (selectedIMEI!=null && selectedIMEI.length()>0) {
                                Log.v("filterStringg",selectedIMEI);
                                adapterrrr.getFilter().filter(selectedIMEI.toString());
                            }
                        }else {
                            selectedIMEI = null;
                           /* if (selectedIMEI!=null && selectedIMEI.length()>0) {

                                Log.v("filterStringg",selectedIMEI);
                                adapterrrr.getFilter().filter(selectedIMEI.toString());
                            }*/
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                if (dateeeeLIST.size()>0){
                    adapterrrr = new DateAdapter(RainFallActivity.this, dateeeeLIST,imeiLIST,totalRainfallList);
                    recyclerView.setAdapter(adapterrrr);
                }
            }
        });


        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentDate = Calendar.getInstance();
                final int year = mcurrentDate.get(Calendar.YEAR);
                final int month = mcurrentDate.get(Calendar.MONTH);
                final int day = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog mDatePicker = new DatePickerDialog(RainFallActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {


                        String day1 = null;
                        String month1 = null;
                        if (selectedday<10){
                            day1 = "0"+selectedday;
                        }else {
                            day1 = ""+selectedday;
                        }

                        if (selectedmonth<10){
                            month1 = "0"+(selectedmonth+1);
                        }else {
                            month1 = ""+(selectedmonth+1);
                        }

                        String strrr = day1+"/"+month1+"/"+selectedyear;
                        fromDate.setText(strrr);

                    }
                }, year, month, day);
                mDatePicker.setTitle("Please select From Date");
                mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());

                mDatePicker.show();
            }
        });


        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentDate = Calendar.getInstance();
                final int year = mcurrentDate.get(Calendar.YEAR);
                final int month = mcurrentDate.get(Calendar.MONTH);
                final int day = mcurrentDate.get(Calendar.DAY_OF_MONTH);


                final DatePickerDialog mDatePicker = new DatePickerDialog(RainFallActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {

                        String day1 = null;
                        String month1 = null;
                        if (selectedday<10){
                            day1 = "0"+selectedday;
                        }else {
                            day1 = ""+selectedday;
                        }

                        if (selectedmonth<10){
                            month1 = "0"+(selectedmonth+1);
                        }else {
                            month1 = ""+(selectedmonth+1);
                        }

                        String strrr = day1+"/"+month1+"/"+selectedyear;
                        toDate.setText(strrr);

                    }
                }, year, month, day);
                mDatePicker.setTitle("Please select TO date");
                // TODO Hide Future Date Here
                mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());

                // TODO Hide Past Date Here
                //  mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis());
                mDatePicker.show();
            }
        });

        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                ArrayList<String> dateeeeLIST = new ArrayList<String>();
                ArrayList<String> imeiLIST = new ArrayList<String>();

                if (i==0) {
                    Cursor todayday = db.getToday();
                    Cursor getAllDate = db.getAllDate();
                    Cursor betweenCur = db.getBetweenData("06/01/2019","08/01/2019");
                    Log.v("betweenDayCount", "-" + betweenCur.getCount()+"--------"+getAllDate.getCount());
                    if (getAllDate.moveToFirst()) {
//
                        do {
                            String dattaa = getAllDate.getString(getAllDate.getColumnIndex(DBAdapter.DATE));
                            Log.v("kjskdj",dattaa+"");
                           // dateeeeLIST.add(dattaa);
                        }
                        while (getAllDate.moveToNext());
                    }
                }else if (i==1) {
                    Cursor c1yesterday = db.getYesterDayData();
                    Log.v("yesterdaydayCount", "-" + c1yesterday.getCount());
                    if (c1yesterday.moveToFirst()) {
//
                        do {
                            String dattaa = c1yesterday.getString(c1yesterday.getColumnIndex(DBAdapter.DATE));
                            dateeeeLIST.add(dattaa);
                        }
                        while (c1yesterday.moveToNext());
                    }
                }else if (i==2) {
                    Cursor c1yesterday = db.getLast7Data();
                    Log.v("yesterdaydayCount", "-" + c1yesterday.getCount());
                    if (c1yesterday.moveToFirst()) {
//
                        do {
                            String dattaa = c1yesterday.getString(c1yesterday.getColumnIndex(DBAdapter.DATE));
                            dateeeeLIST.add(dattaa);
                        }
                        while (c1yesterday.moveToNext());
                    }
                }else if (i==3) {
                    Cursor c1yesterday = db.getLast30Data();
                    Log.v("yesterdaydayCount", "-" + c1yesterday.getCount());
                    if (c1yesterday.moveToFirst()) {
//
                        do {
                            String dattaa = c1yesterday.getString(c1yesterday.getColumnIndex(DBAdapter.DATE));
                            dateeeeLIST.add(dattaa);
                        }
                        while (c1yesterday.moveToNext());
                    }
                }

                if (dateeeeLIST.size()>0){
                    adapterrrr = new DateAdapter(RainFallActivity.this, dateeeeLIST,imeiLIST,imeiLIST);

                    recyclerView.setAdapter(adapterrrr);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    /*public void addListener(){
       *//* editSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                System.out.println("Text ["+s+"]");

                if (s!=null) {
                    adapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });*//*
    }*/
}
