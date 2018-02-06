package com.uncc.mobileappdev.inclass04;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.uncc.mobileappdev.inclass04.Util.getPassword;

public class PasswordActivity extends AppCompatActivity {

    ExecutorService threadPool;
    ProgressDialog progressDialog;
    Handler handler;
    TextView lengthText;
    TextView countText;
    String selectedPassword;
    TextView passwordVal;

    int passwordLength = 8;
    int passwordCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        setTitle("Password Generator");

        progressDialog = new ProgressDialog(PasswordActivity.this);
        progressDialog.setMessage("Generating passwords");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);

        threadPool = Executors.newFixedThreadPool(2);

        SeekBar seekBarLength = findViewById(R.id.seek_bar_pwd_length);
        SeekBar seekBarCount = findViewById(R.id.seek_bar_pwd_count);
        Button threadButton = findViewById(R.id.button_generate_pwd_thread);
        Button asyncButton = findViewById(R.id.button_generate_pwd_async);
        lengthText = findViewById(R.id.text_view_pwd_length_value);
        countText = findViewById(R.id.text_view_pwd_count_value);
        passwordVal = findViewById(R.id.text_view_pwd_value);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                switch(msg.what){
                    case DoWork.STATUS_START:
                        progressDialog.setMax(passwordCount);
                        progressDialog.setProgress(0);
                        progressDialog.show();
                        Log.d("demo","Starting");
                        break;
                    case DoWork.STATUS_IN_PROGRESS:
                        progressDialog.incrementProgressBy(1);
                        Log.d("demo","In Progress " + msg.getData().getInt(DoWork.PROGRESS_KEY));
                        break;
                    case DoWork.STATUS_STOP:
                        if(msg.getData().getStringArrayList(DoWork.PASSWORD_LIST) != null) {
                            showPopup(msg.getData().getStringArrayList(DoWork.PASSWORD_LIST));
                        }

                        Log.d("demo","Stopping");
                        progressDialog.dismiss();

                        break;
                    default:
                        break;
                }

                return false;
            }

        });


        seekBarLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                passwordLength = progress;
                lengthText.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}


        });

        seekBarCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                passwordCount = progress;
                countText.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

        });

        threadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                threadPool.execute(new DoWork());
            }
        });

        asyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DoWorkAsync().execute(passwordCount);
            }
        });
    }

    private void showPopup(ArrayList<String> passwords) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Passwords");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        for(String str : passwords){
            arrayAdapter.add(str);
            Log.d("Password", str);
        }

        alert.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedPassword = arrayAdapter.getItem(which);
                passwordVal.setText(selectedPassword);
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    class DoWork implements Runnable{


        private static final int STATUS_START = 0;
        private static final int STATUS_IN_PROGRESS = 1;
        private static final int STATUS_STOP = 2;
        private static final String PROGRESS_KEY = "PROGRESS";
        private static final String PASSWORD_LIST = "Password List";

        @Override
        public void run() {
            Log.d("Test", "Called RUN!");
            Message startMsg = new Message();
            startMsg.what = STATUS_START;
            handler.sendMessage(startMsg);

            ArrayList<String> passwords = new ArrayList<>();

            for(int i =0 ; i < passwordCount; i++){
                passwords.add(getPassword(passwordLength));
                //Log.d("Passwords", passwords.get(i));

                Message progMsg = new Message();
                progMsg.what = STATUS_IN_PROGRESS;

                Bundle bundle = new Bundle();
                bundle.putInt(PROGRESS_KEY, (Integer)i);
                progMsg.setData(bundle);
                handler.sendMessage(progMsg);

            }

            Message stopMsg = new Message();
            stopMsg.what = STATUS_STOP;
            Bundle finalBundle = new Bundle();
            finalBundle.putStringArrayList(PASSWORD_LIST, passwords);
            stopMsg.setData(finalBundle);
            handler.sendMessage(stopMsg);
        }
    }

    class DoWorkAsync extends AsyncTask<Integer, Integer, Double> {
        ArrayList<String> passwords = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(PasswordActivity.this);
            progressDialog.setMessage("Generating passwords");
            progressDialog.setMax(passwordCount);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Double aDouble) {
            progressDialog.setProgress(passwords.size());
            if(passwords.size() != 0) {
                showPopup(passwords);
            }
            progressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setMessage("Generating Passwords");
            progressDialog.incrementProgressBy(1);
        }

        @Override
        protected Double doInBackground(Integer... params) {
            for(int i =0 ; i < params[0]; i++) {
                passwords.add(getPassword(passwordLength));
                publishProgress(i);
            }

            return (double) passwords.size();
        }
    }
}
