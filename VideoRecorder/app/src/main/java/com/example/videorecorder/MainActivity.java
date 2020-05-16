package com.example.videorecorder;

import android.Manifest;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Camera camera, camera2;
    Button recordBtn, switchCam, radioButtonOk;
    CheckBox checkBox;

    FrameLayout frameLayout;
    private MediaRecorder mediaRecorder;
    private static final String TAG = "Recorder";
    private boolean isRecording = false;

    ShowCamera showCamera;
    private File outputFile;
    private String[] permissions;
    private Spinner spinner;
    private ArrayAdapter<String> myAdapter;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private SeekBar seekBar;
    private Chronometer chronometer;
    private TextView maxDuration;
    private EditText maxDurationEdit;
    private Button editTextbutton;
    CamcorderProfile profile;
    private int MAX_DURATION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        defineViews();
        setViewActions();

     /*   dropButton = (Button) findViewById(R.id.buttonDrop);
        dropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent drop = new Intent(MainActivity.this, DropboxActivity4.class);
                startActivity(drop);
                finish();
            }
        }); */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem itemsetDuration = menu.findItem(R.id.duration);
        MenuItem itemCamera = menu.findItem(R.id.camera);
        MenuItem itemQuality = menu.findItem(R.id.quality);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.duration) {
            showView(maxDuration);
            showView(maxDurationEdit);
            showView(editTextbutton);
            showView(seekBar);
            hideView(radioGroup);
            hideView(spinner);
        }
        else if (id == R.id.camera) {
            if (isRecording) {
                Toast.makeText(getApplicationContext(), "Cannot switch camera while recording", Toast.LENGTH_LONG).show();
            } else {
                showView(radioGroup);
                hideView(maxDuration);
                hideView(maxDurationEdit);
                hideView(editTextbutton);
                hideView(seekBar);
            }
        }

        else if(id == R.id.quality) {
            if (myAdapter != null) {
                myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(myAdapter);
                showView(spinner);
            } else {
                Toast.makeText(getApplicationContext(), "Select Camera First",Toast.LENGTH_SHORT).show();
                hideView(maxDuration);
                hideView(maxDurationEdit);
                hideView(editTextbutton);
                hideView(seekBar);
                hideView(radioGroup);
            }
        }

        else if(id == R.id.listItem) {
            Intent listIntent = new Intent(this, ListViewActivity.class);
            startActivity(listIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void defineViews() {
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        recordBtn = (Button) findViewById(R.id.btnRecord);
        spinner = (Spinner) findViewById(R.id.spinner);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioButtonOk = (Button) findViewById(R.id.radioButtonOk);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        maxDuration = (TextView) findViewById(R.id.maxDuration);
        maxDurationEdit = (EditText) findViewById(R.id.maxDurationEdit);
        maxDurationEdit.setFilters(new InputFilter[]{new InputFilterMinMax("0", "120")});
        editTextbutton = (Button) findViewById(R.id.editTextButton);
        switchCam = (Button) findViewById(R.id.switchButton);
    }

    public void setViewActions() {
        editTextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               hideView(editTextbutton);
               hideView(maxDurationEdit);
               hideView(maxDuration);
               hideView(seekBar);
               MAX_DURATION = Integer.parseInt(maxDurationEdit.getText().toString());
               seekBar.setProgress(MAX_DURATION);
               Toast.makeText(getApplicationContext(), "Max Duration set to " + MAX_DURATION + " minutes",Toast.LENGTH_SHORT).show();
            }
        });

        checkPermission();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MAX_DURATION = progress;
                if (MAX_DURATION == 0) {
                    maxDuration.setText("Unlimited");
                    maxDurationEdit.setHint("Unlimited");
                } else {
                    maxDuration.setText("Set Recording Duration: " + MAX_DURATION + "-min");
                    maxDurationEdit.setHint("Set Recording Duration:   " + MAX_DURATION + "-min");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
                        hideView(spinner);
                        break;

                    case 1:
                        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                        hideView(spinner);
                        break;

                    case 2:
                        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                        hideView(spinner);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
            }
        });


        recordBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                startCapture();
            }
        });

        checkBox = (CheckBox) findViewById(R.id.showPreview);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    /*(final int color = 0xFF000000;
                    final Drawable drawable = new ColorDrawable(color);
                    frameLayout.setForeground(drawable);
                } else {
                    frameLayout.setForeground(null);
                }*/
                    frameLayout.setVisibility(View.INVISIBLE);
                } else {
                    frameLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void checkPermission() {
        permissions = new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                radioButtonOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int radioId = radioGroup.getCheckedRadioButtonId();
                        radioButton = findViewById(radioId);
                        selectCamera(radioButton.getText().toString());
                        showCamera = new ShowCamera(getApplicationContext(), camera);
                        frameLayout.removeAllViews();
                        frameLayout.addView(showCamera);
                        hideView(radioGroup);
                        showView(checkBox);
                        showView(recordBtn);
                    }
                });
            }
        });
    }
    public void selectCamera(String cam) {
        if(camera == null) {
            cameraSet(cam);
        } else {
            camera.release();
            camera = null;
            cameraSet(cam);
        }
    }

    public void cameraSet(String cam) {
        myAdapter = null;
        if (cam.equals("FRONT")) {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    getResources().getStringArray(R.array.QUALITY_FRONT));
        } else {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    getResources().getStringArray(R.array.QUALITY_BACK));
        }
    }

    public void showView(View v) {
        v.setVisibility(View.VISIBLE);
    }

    public void hideView(View v) {
        v.setVisibility(View.INVISIBLE);
    }


    public boolean prepareVideoRecorder() {
        mediaRecorder = new MediaRecorder();
        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setMaxDuration(MAX_DURATION*60000);
        if(this.profile != null) {
            mediaRecorder.setProfile(this.profile);
        } else {
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
        }
        outputFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO);
        if (outputFile == null) {
            return false;
        }
        mediaRecorder.setOutputFile(outputFile.getPath());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    public void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    public void startCapture() {
        if(isRecording){
            try{
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                outputFile.delete();
            }
            releaseMediaRecorder();
            chronometer.stop();
            chronometer.setVisibility(View.INVISIBLE);
            camera.lock();
            recordBtn.setText("RECORD");
            isRecording = false;
           // releaseCamera();
        } else {
            MediaPrepareTask mediaPrepareTask = new MediaPrepareTask();
            mediaPrepareTask.execute(null, null, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseMediaRecorder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    public void releaseCamera() {
        if(camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onBackPressed() {
        if(isRecording) {
            Toast.makeText(this, "Recording is going on" , Toast.LENGTH_LONG).show();
        } else {
            super.onBackPressed();
        }
    }


    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {


            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mediaRecorder.start();

                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                chronometer.stop();
                chronometer.setVisibility(View.INVISIBLE);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                MainActivity.this.finish();
            }
            // inform the user that recording has started
            recordBtn.setText("STOP");
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.setVisibility(View.VISIBLE);
            chronometer.start();
        }
    }
}

