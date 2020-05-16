package com.example.videorecorder;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.Comparator;

public class ListViewActivity extends AppCompatActivity {
    ArrayAdapter<String> arrayAdapter;
    ListView listView;
    File directory;
    VideoView videoView;
    String[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        listView = (ListView) findViewById(R.id.listView);
        doStuff();
        videoView = (VideoView) findViewById(R.id.video_view);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.setVisibility(View.INVISIBLE);
                listView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void doStuff() {
        directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "VideoRecorder");
        files = directory.list();
        if (files != null) {
            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, files);
            arrayAdapter.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o2.compareTo(o1);
                }
            });
            listView.setAdapter(arrayAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listView.setVisibility(View.GONE);
                    videoView.setVisibility(View.VISIBLE);
                    String filePosition = files[position];
                    String filepath = directory.getPath() + "/" + filePosition;
                    videoPlay(filepath);
                    Toast.makeText(getApplicationContext(), "Playing " + filePosition, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void videoPlay(String videoPath) {
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
        MediaController mediaController = new MediaController(ListViewActivity.this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
        videoView.start();
    }

    public void getFilePath(int i) {

        }
    }

