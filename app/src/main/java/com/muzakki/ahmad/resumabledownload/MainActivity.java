package com.muzakki.ahmad.resumabledownload;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ResumableDownloader downloader;
    private ProgressBar progressBar;
    private TextView progressCaption;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    1
            );
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);
        progressCaption = (TextView) findViewById(R.id.progressCaption);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
    }

    public void start(View v){
        String url = ((EditText) findViewById(R.id.txtUrl)).getText().toString();
        String filename = getFilename(url);
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS)+File.separator+filename);
        downloader = new mResumableDownloader(this,file,url);
        downloader.start();
        txtStatus.setText("Downloading");
    }

    public void pause(View v){
        if (downloader != null) {
            txtStatus.setText("Paused");
            downloader.pause();
        }else{
            Toast.makeText(this,"Press start first!",Toast.LENGTH_SHORT).show();
        }
    }

    public String getFilename(String url) {
        String[] filenames = url.split("/");
        return filenames[filenames.length - 1];
    }

    class mResumableDownloader extends ResumableDownloader{

        public mResumableDownloader(Context ctx, File targetFile, String urlStr) {
            super(ctx, targetFile, urlStr);
        }

        @Override
        void progressUpdate(final int value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(value);
                    progressCaption.setText(value+"%");
                }
            });
        }

        @Override
        void onComplete() {
            super.onComplete();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtStatus.setText("Finished");
                }
            });
        }
    }
}
