package com.example.studerande.upg32;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class MainActivity extends AppCompatActivity {

    private static final int RECORDING_ACTIVE = 1;
    private static final int PERMISSION_REQUEST_CODE = 3;
    private static final String PREFS_NAME = "MyPrefFile";

    private String last_filename = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        String restoredAudioname = settings.getString("audio_file", null);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView filenametextview = (TextView) findViewById(R.id.thetextView);
        Button recordbutton = (Button) findViewById(R.id.record_button);

        if(restoredAudioname != null)
        {
            last_filename = settings.getString("audio_file", null);
             filenametextview.setText("you have a previous recording.." + last_filename);
        }

        /* start of play prev sound*/
        File sourceFile = new File(last_filename);
        if (sourceFile.isFile()) {
            audioPlayer(last_filename);
        }
        /* end of play prev sound*/

        recordbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Button record_button = (Button) findViewById(R.id.record_button);
                record_button.setText("starting recording..");
                startRecord();
            }
        });
    }

    public File getTempFile(Context context, String url) {
        File file = null;
        try {
            String fileName = Uri.parse(url).getLastPathSegment();
            file = File.createTempFile(fileName, null, context.getCacheDir());
        } catch (IOException e) {
            // Error while creating file
        }
        return file;
    }

    public void startRecord() {
        // NOTE THAT THE USER MUST HAVE A RECORDING SOFTWARE. NOT ALL DEVICES HAVE THIS.
        Intent recordingApplicationIntent = new Intent();
        recordingApplicationIntent.setAction(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        startActivityForResult(recordingApplicationIntent, RECORDING_ACTIVE);
    }

    private String getAudioFilePathFromUri(Uri uri) {
        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null);
        cursor.moveToFirst();
        int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
        return cursor.getString(index);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORDING_ACTIVE && resultCode == RESULT_OK) {
            Context context;
            Toast.makeText(this, "Sound recoding intent success", Toast.LENGTH_SHORT).show();
            String thefilename = "";

            Bundle extras = data.getExtras();
            Uri uri = data.getData();

            String filePath = getAudioFilePathFromUri(uri);
            last_filename = filePath;
            thefilename = filePath;
            Button record_button = (Button) findViewById(R.id.record_button);
            // record_button.setText(thefilename);
            record_button.setText("RECORD NEW");


            File sourceFile = new File(filePath);
            if (!sourceFile.isFile()) {
                Log.e("recordapp", "file does not exist");
                // return 0;
            } else {
                // set storage

                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putString("audio_file", last_filename);
                editor.commit();

                Log.e("recordapp", "File exist");
                audioPlayer(filePath);
            }
        }
    }

    public void audioPlayer(String path) {
        //set up MediaPlayer
        MediaPlayer mp = new MediaPlayer();
        Boolean AcessToPlay = true;
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);

            // PERMISSION_REQUEST_CODE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
        // has permission
        if ((ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)) {
            try {
                mp.setDataSource(path); // + File.separator + fileName
                mp.prepare();
                mp.start();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("MYAPP", "exception", e);
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    audioPlayer(last_filename);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // rip
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}