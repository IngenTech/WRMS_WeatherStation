package com.example.admin.wrms_weatherstation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends Fragment {

    private static final String TAG = "Fragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_PICK_FILE = 4;

    // Layout Views
    private EditText mOutEditText, fromDate, toDate;
    private Button mSendButton;
    private Button mSendButton1;

    DBAdapter db;
    int counttt = 0;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;
    private TextView mLog;
    private Button mFileBtn;
    private ProgressBar pb;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;

    String recievedData = "";

    List<String> dates = new ArrayList<String>();
    boolean flagPop = false;
    String fromDateeee = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();

        }


    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services

                mChatService.start();
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        fromDate = (EditText) view.findViewById(R.id.edit_text_from);
        toDate = (EditText) view.findViewById(R.id.edit_text_to);
        mSendButton = (Button) view.findViewById(R.id.button_send);
        mSendButton1 = (Button) view.findViewById(R.id.button_send1);
        mLog = (TextView) view.findViewById(R.id.logs);
        mFileBtn = (Button) view.findViewById(R.id.button_file);
        pb = (ProgressBar) view.findViewById(R.id.pb);

        db = new DBAdapter(getActivity());
        db.open();
        //  db.deleteDataOlderThan30Days();


/*
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
            ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);
            String stttsss = "";
            if(uuids != null) {
                for (ParcelUuid uuid : uuids) {


                    stttsss =stttsss+","+ uuid.getUuid().toString();

                }
            }else{
                Log.d("VISHALLLLLLLLL", "Uuids not found, be sure to enable Bluetooth!");
            }
            Log.d("VISHALLLLLLLLL", "UUID: " +stttsss);
            mLog.setText(""+stttsss);
            // Toast.makeText(getActivity(),"UUID: " + stttsss,Toast.LENGTH_LONG).show();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
*/

        mOutEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* new DatePickerDialog(getActivity(), date1, myCalendar1
                        .get(Calendar.YEAR), myCalendar1.get(Calendar.MONTH),
                        myCalendar1.get(Calendar.DAY_OF_MONTH)).show();
*/


                Calendar mcurrentDate = Calendar.getInstance();
                final int year = mcurrentDate.get(Calendar.YEAR);
                final int month = mcurrentDate.get(Calendar.MONTH);
                final int day = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {

                        String day1 = null;
                        String month1 = null;
                        if (selectedday < 10) {
                            day1 = "0" + selectedday;
                        } else {
                            day1 = "" + selectedday;
                        }

                        if (selectedmonth < 10) {
                            month1 = "0" + (selectedmonth + 1);
                        } else {
                            month1 = "" + (selectedmonth + 1);
                        }

                        String strrr = day1 + "" + month1 + "" + selectedyear;
                        mOutEditText.setText(strrr);

                    }
                }, year, month, day);
                mDatePicker.setTitle("Please select date");
                // TODO Hide Future Date Here
                mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());

                // TODO Hide Past Date Here
                //  mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis());
                mDatePicker.show();

            }
        });


        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentDate = Calendar.getInstance();
                final int year = mcurrentDate.get(Calendar.YEAR);
                final int month = mcurrentDate.get(Calendar.MONTH);
                final int day = mcurrentDate.get(Calendar.DAY_OF_MONTH);


                final DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {


                        fromDateeee = "";
                        fromDate.setText("");
                        String day1 = null;
                        String month1 = null;
                        if (selectedday < 10) {
                            day1 = "0" + selectedday;
                        } else {
                            day1 = "" + selectedday;
                        }

                        if (selectedmonth < 10) {
                            month1 = "0" + (selectedmonth + 1);
                        } else {
                            month1 = "" + (selectedmonth + 1);
                        }

                        String strrr = selectedyear + "-" + month1 + "-" + day1;
                        fromDate.setText(strrr);

                        fromDateeee = day1 + "" + month1 + "" + selectedyear;

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


                final DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {

                        String day1 = null;
                        String month1 = null;
                        if (selectedday < 10) {
                            day1 = "0" + selectedday;
                        } else {
                            day1 = "" + selectedday;
                        }

                        if (selectedmonth < 10) {
                            month1 = "0" + (selectedmonth + 1);
                        } else {
                            month1 = "" + (selectedmonth + 1);
                        }

                        String strrr = selectedyear + "-" + month1 + "-" + day1;
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
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");


        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                 counttt = 0;
                dates = new ArrayList<String>();
                recievedData = "";
                String smsms = mOutEditText.getText().toString().trim();
                if (smsms != null && smsms.length() > 4) {
                    View view = getView();
                    if (null != view) {
                        TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                        String message = textView.getText().toString();
                       /* message = "#Start;\n" +
                                "07/01/2019,00:00:01,0.5\n" +
                                "07/01/2019,01:00:01,0.0 \n" +
                                "07/01/2019,04:00:01,0.25 \n" +
                                "07/01/2019,05:00:01,0.0 \n" +
                                "07/01/2019,06:00:01,0.0 \n" +
                                "07/01/2019,07:00:05,0.0 \n" +
                                "07/01/2019,08:02:01,2.5 \n" +
                                "07/01/2019,09:00:01,5.5 \n" +
                                "07/01/2019,10:00:01,0.5 \n" +
                                "07/01/2019,11:00:01,10.5 \n" +
                                "07/01/2019,12:00:01,0.0 \n" +
                                "07/01/2019,13:00:01, 0.0\n" +
                                "07/01/2019,17:00:01,0.0\n" +
                                "07/01/2019,18:00:01,0.0\n" +
                                "07/01/2019,19:00:01,0.0\n" +
                                "07/01/2019,20:00:01,0.0\n" +
                                "07/01/2019,21:00:01,0.0\n" +
                                "07/01/2019,22:00:01,0.0\n" +
                                "#End;";*/
                        sendMessage(message);
                    }
                } else {
                    Toast.makeText(getActivity(), "Please select correct date", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSendButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget

                counttt = 0;
                recievedData = "";
                dates = new ArrayList<String>();

                String fromD = fromDate.getText().toString().trim();
                String toD = toDate.getText().toString().trim();
                if (fromD != null && fromD.length() > 4) {
                    if (toD != null && toD.length() > 4) {

                        dates = getDates(fromD, toD);

                        View view = getView();
                        if (null != view) {

                            if (fromDateeee != null && fromDateeee.length() > 4) {
                                sendMessage(fromDateeee);
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "Please select to date", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Please select from date", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile();
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendFile() {
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, REQUEST_PICK_FILE);
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }


    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    private void setStatus(CharSequence subTitle) {
        Log.v("xyxyxyxyxy_subtitle", "xyxyxyxyx" + subTitle);
        mLog.setText("\n" + subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {

            final FragmentActivity activity = getActivity();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus("connecting...");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus("not connected");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //  mLog.append("\nMe:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    // mLog.append("\n" + mConnectedDeviceName + ":  " + readMessage);

                    if (recievedData != null && recievedData.length() > 4) {
                        recievedData = recievedData + readMessage;
                    } else {
                        recievedData = readMessage;
                    }

                    if (recievedData != null && recievedData.contains("#END;")) {
                        readMessagePopup(recievedData + "", counttt);
                    }

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        mLog.setText("\n" + msg.getData().getString(Constants.TOAST));
                    }
                    break;
                case Constants.MESSAGE_END:
                    final File file = (File) msg.obj;
                    pb.setVisibility(View.GONE);
                    mLog.setText("\n" + "file transfer end");
                    if (file != null) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        if (isNetworkAvailable()) {
                                            final Dialog dialog11 = new Dialog(getActivity());

                                            dialog11.setCanceledOnTouchOutside(false);

                                            dialog11.requestWindowFeature(Window.FEATURE_NO_TITLE);

                                            new Thread(new Runnable() {
                                                public void run() {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        public void run() {
                                                        }
                                                    });
                                                    if (isNetworkAvailable()) {
                                                        //   sendData(file);
                                                        readWeatherData(file);
                                                    } else {
                                                        Toast.makeText(getActivity(), "No internet connections", Toast.LENGTH_SHORT).show();

                                                    }

                                                    dialog11.dismiss();

                                                }
                                            }).start();

                                        } else {
                                            Toast.makeText(getActivity(), "No internet connections", Toast.LENGTH_SHORT).show();

                                        }
                                        // FileOpen.openFile(activity, file);

                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Submit file to server?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("Cancel", dialogClickListener).show();
                    } else {
                        Toast.makeText(getActivity(), "File not found", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_PERCENT:
                    pb.setProgress(msg.arg1);
                    break;
                case Constants.MESSAGE_START:
                    pb.setVisibility(View.VISIBLE);
                    mLog.setText("\n" + "file transfer start...");
                    break;
                case Constants.MESSAGE_FILE_ERROR:
                    pb.setVisibility(View.GONE);
                    mLog.setText("\n" + "error file transfer");
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
            case REQUEST_PICK_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    //      Log.v("filepAthhhh", data.getData().getPath());

                  /*  String filePath = Environment.getExternalStorageDirectory() + "/rainfall.csv";
                    Log.v("filepAthhhh", filePath);
                    mChatService.writeFile(new File(filePath));*/
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        Log.v("cocoococococ", "cococooc");
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
            case R.id.show_data: {
                // Ensure this device is discoverable by others
                Intent serverIntent = new Intent(getActivity(), RainFallActivity.class);
                startActivity(serverIntent);
                return true;
            }

            case R.id.send_error_log: {


                boolean resultPhone = Utility.checkPermissionGallery(getActivity());
                if (resultPhone) {

                    final File path =
                            Environment.getExternalStoragePublicDirectory
                                    (
                                            //Environment.DIRECTORY_PICTURES
                                            Environment.DIRECTORY_DCIM + "/wrms_bluetooth/"
                                    );


                    final File logFile = new File(path, "rainfall.txt");


                    //  File logFile = new File(Environment.getExternalStorageDirectory(), "wrms_station_error.txt");
                    String filePath = Environment.getExternalStorageDirectory().toString() + "/rainfall.txt";
                    //   File logFile = new File(filePath);
                    if (logFile.exists()) {

                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        // set the type to 'email'
                        emailIntent.setType("vnd.android.cursor.dir/email");
                        String to[] = {"vishal.tripathi@weather-risk.com"};
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
                        // the attachment
                        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logFile));
                        // the mail subject
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Error log");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "Sent from WRMS app");

                        if (emailIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(Intent.createChooser(emailIntent, "Send email..."));
                        } else {
                            Toast.makeText(getActivity(), "No email application is available to share error log file", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(getActivity(), "ErrorLog file does not exist ", Toast.LENGTH_LONG).show();
                    }

                    return true;
                }
            }
        }
        return false;
    }

    public void sendData(File file) {

        readWeatherData(file);

        String url = "http://pdjalna.myfarminfo.com/PDService.svc/UploadVoice/";
        Log.v("kldjsklj", "" + url);
        //  File file = new File(savePathInDevice);
        try {
            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httppost = new HttpPost(url);

            InputStreamEntity reqEntity = new InputStreamEntity(
                    new FileInputStream(file), -1);
            reqEntity.setContentType("binary/octet-stream");
            // reqEntity.setContentType("application/vnd.ms-excel");

            reqEntity.setChunked(true); // Send in multiple parts if needed
            httppost.setEntity(reqEntity);
            HttpResponse response = httpclient.execute(httppost);

            BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();

            String result = sb.toString();
            System.out.println("Result : " + result);
            String responsePath = null;
            if (result != null) {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("UploadVoiceResult")) {
                    responsePath = jsonObject.getString("UploadVoiceResult");
                } else {
                    responsePath = null;
                }
            }
            if (responsePath != null) {
                final String finalResponsePath = responsePath;
                new Thread(new Runnable() {
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {


                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Success").
                                        setMessage("Added successfully.").
                                        setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.cancel();
                                                // createNewLogStringToServer("", "Voice_Message");
                                                Intent in = new Intent(getActivity(), RainFallActivity.class);
                                                startActivity(in);
                                            }
                                        });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                                Log.v("jksajkjsk", finalResponsePath + "");
                            }
                        });

                    }
                }).start();


            }

        } catch (Exception e) {
            // show error
        }
    }


    private List<WeatherSample> weatherSamples = new ArrayList<WeatherSample>();


    public void readMessagePopup(final String str, int countttt) {


       /* AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Received Rainfall");


        alertDialog.setMessage("Successfully received " + str);
        alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                readWeatherStringData(str);
                writeToFile(str,getActivity());
                dialog.cancel();

               *//* if (dates.size()>1) {
                    for (int ii = 1; ii < dates.size(); ii++) {
                        System.out.println(dates.get(ii));


                        View view = getView();
                        if (null != view) {

                            String date1string = ""+dates.get(ii);

                            if (date1string != null) {
                                sendMessage("" + date1string);
                            }
                        }
                    }
                }*//*
            }
        });

        alertDialog.show();*/

        readWeatherStringData(str);
        writeToFile(str, getActivity());
        String date1string = null;
        if (dates.size() > 1) {
            if (countttt < dates.size() - 1) {

                View view = getView();
                if (null != view) {

                     date1string = dates.get(countttt);

                    if (date1string != null) {
                        countttt++;
                        counttt = countttt;
                        recievedData = "";

                        final String finalDate1string = date1string;
                        new Thread(new Runnable() {
                            public void run() {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {

                                        sendMessage("" + finalDate1string);
                                    }
                                });
                            }
                        }).start();


                    }
                }

            } else {
                successPopup(date1string);
            }

        } else {
            successPopup(date1string);
        }

    }

    private void writeToFile(String data, Context context) {
        /*  OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("rainfall.txt", Context.MODE_PRIVATE));
          outputStreamWriter.write(data);
          outputStreamWriter.close();

*/

        final File path =
                Environment.getExternalStoragePublicDirectory
                        (
                                //Environment.DIRECTORY_PICTURES
                                Environment.DIRECTORY_DCIM + "/wrms_bluetooth/"
                        );

        // Make sure the path directory exists.
        if (!path.exists()) {
            // Make it, if it doesn't exit
            path.mkdirs();
        }

        final File file = new File(path, "rainfall.txt");

        // Save your stream, don't forget to flush() it before closing it.

        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();

            Log.e("createdddd", "File write created: ");
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }


    }

    private void readWeatherData(File f) {
        weatherSamples = new ArrayList<WeatherSample>();
        // Read the raw csv file
        // InputStream is = getResources().openRawResource(R.raw.data);
        FileInputStream stream = null;
        String line = "";
        try {
            stream = new FileInputStream(f);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));

            // Initialization
            // Step over headers
            reader.readLine();

            // If buffer is not empty
            while ((line = reader.readLine()) != null) {
                Log.d("MyActivity", "Line: " + line);
                // use comma as separator columns of CSV
                String[] tokens = line.split(",");
                // Read the data
                WeatherSample sample = new WeatherSample();
                // Setters
                sample.setDate(tokens[0]);
                sample.setSumHours(tokens[1]);
                sample.setRainfall(tokens[2]);
                // Adding object to a class
                weatherSamples.add(sample);
                // Log the object
                Log.d("CSVFILE", "Justcreated: " + sample);
            }
            boolean flag = false;
            String dddddaaatt = "";
            for (int i = 0; i < weatherSamples.size(); i++) {
                dddddaaatt = weatherSamples.get(i).getDate();
                String rainfall_value = weatherSamples.get(i).getRainfall();
                String hourrrr = weatherSamples.get(i).getSumHours();
                if (dddddaaatt != null && dddddaaatt.length() > 4) {
                    Cursor cursor = db.getDateList();


                    try {
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            do {
                                String dateString = cursor.getString(cursor.getColumnIndex(DBAdapter.DATE));
                                Log.v("savedate", "" + dateString + "---" + dddddaaatt);
                                if (dateString != null && !dateString.equalsIgnoreCase(dddddaaatt)) {
                                    flag = true;
                                } else {
                                    flag = false;
                                }

                            } while (cursor.moveToNext());
                        } else {

                            // insertRainfallData(dddddaaatt,hourrrr,rainfall_value);
                            insertDateData(dddddaaatt);
                        }
                    } finally {
                        cursor.close();
                    }
                }

            }
            if (flag) {
                System.out.println("Inserting to db");
                insertDateData(dddddaaatt);
            }


        } catch (IOException e) {
            // Logs error with priority level
            Log.wtf("CSVFILE", "Error reading data file on line" + line, e);
            // Prints throwable details
            e.printStackTrace();
        }
    }


    private void readWeatherStringData(String strrrrr) {

        if (strrrrr != null) {
            Log.v("itemssss1", strrrrr);

            String[] splits = strrrrr.split(";");
            if (splits.length > 1) {
                Log.v("itemssss2", strrrrr);
                String ssttrr = splits[1];
                String[] items = ssttrr.split("\n");
                weatherSamples = new ArrayList<WeatherSample>();
                for (int i = 0; i < items.length - 1; i++) {
                    Log.v("itemssss3", "0089uoiiohj");
                    WeatherSample sample = new WeatherSample();
                    String[] itemsss = items[i].split(",");
                    // Setters
                    if (itemsss.length > 2) {

                        Log.v("itemssss", itemsss[0] + "--" + itemsss[1] + "--" + itemsss[2]);

                        sample.setDate(itemsss[0]);
                        sample.setSumHours(itemsss[1]);
                        sample.setRainfall(itemsss[2]);
                        // Adding object to a class
                        weatherSamples.add(sample);
                    }
                }

            }
        }


        boolean flag = true;
        String dddddaaatt = "";
        for (int i = 0; i < weatherSamples.size(); i++) {
            dddddaaatt = weatherSamples.get(i).getDate();
            String rainfall_value = weatherSamples.get(i).getRainfall();
            String hourrrr = weatherSamples.get(i).getSumHours();
            if (dddddaaatt != null && dddddaaatt.length() > 4) {
                Cursor cursor = db.getDateList();


                try {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            String dateString = cursor.getString(cursor.getColumnIndex(DBAdapter.DATE));
                            Log.v("savedate", "" + dateString + "---" + dddddaaatt);
                            if (dateString != null && dateString.equalsIgnoreCase(dddddaaatt)) {
                                flag = false;

                            }

                        } while (cursor.moveToNext());
                    } else {

                        // insertRainfallData(dddddaaatt,hourrrr,rainfall_value);
                        insertDateData(dddddaaatt);
                    }
                } finally {
                    cursor.close();
                }
            }

        }
        if (flag) {
            System.out.println("Inserting to db");
            insertDateData(dddddaaatt);
        }


    }

    private void insertDateData(final String date) {


        ContentValues values = new ContentValues();
        values.put(DBAdapter.DATE, date);
        db.db.insert(DBAdapter.TABLE_DATE, null, values);


        for (int i = 0; i < weatherSamples.size(); i++) {

            Log.v("weatherdatasize", weatherSamples.size() + "");
            String dddddaaatt = weatherSamples.get(i).getDate();
            String rainfall_value = weatherSamples.get(i).getRainfall();
            String hourrrr = weatherSamples.get(i).getSumHours();
            insertRainfallData(dddddaaatt, hourrrr, rainfall_value);


        }
    }

    private synchronized void insertRainfallData(String dateee, String hoursss, String rainfalll) {

        ContentValues values = new ContentValues();
        values.put(DBAdapter.DATE, dateee);
        values.put(DBAdapter.HOURS, hoursss);
        values.put(DBAdapter.RAINFALL, rainfalll);

        db.db.insert(DBAdapter.TABLE_DATE_RAINFALL, null, values);

        Log.v("data_rainfall", "" + values.toString());
    }

    private static List<String> getDates(String dateString1, String dateString2) {
        ArrayList<String> dates = new ArrayList<String>();
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");

        Date date1 = null;
        Date date2 = null;

        try {
            date1 = df1.parse(dateString1);
            date2 = df1.parse(dateString2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);


        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        while (!cal1.after(cal2)) {
            // dates.add(cal1.getTime());
            cal1.add(Calendar.DATE, 1);
            Date st = cal1.getTime();

            final int yyyy = cal1.get(Calendar.YEAR);
            final int mm = cal1.get(Calendar.MONTH) + 1;
            final int dd = cal1.get(Calendar.DAY_OF_MONTH);

            String mmString = null;
            String ddString = null;
            String yyyyString = null;

            yyyyString = "" + yyyy;
            if (mm < 10) {
                mmString = "0" + mm;
            } else {
                mmString = "" + mm;
            }
            if (dd < 10) {
                ddString = "0" + dd;
            } else {
                ddString = "" + dd;
            }

            String stringDatee = ddString + "" + mmString + "" + yyyyString;
            Log.v("dadadadada", stringDatee + "");
            dates.add(stringDatee);

        }
        return dates;
    }


    public void successPopup(String str) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Received Rainfall ");
        alertDialog.setMessage("Successfully received all data ");
        alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.show();

    }
}
