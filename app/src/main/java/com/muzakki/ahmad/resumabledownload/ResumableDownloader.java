package com.muzakki.ahmad.resumabledownload;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by jeki on 12/02/17.
 */

public class ResumableDownloader {
    private final File file;
    private final String urlStr;
    private final Context ctx;
    /**
     * last modified time for file
     */
    private String lastModified;

    private int timeout = 9000;
    private boolean startNewDownload;
    /**
     * total length of the file
     */
    private long fileLength = 0;

    // CONSTANT
    public static final int DOWNLOADING = 0;
    public static final int COMPLETE = 1;
    public static final int PAUSE = 2;
    public static final int ERROR = 3;
    public static final int BUFFER_SIZE = 8 * 1024;
    /**
     * downloading status: downloading; complete; pause; error;
     */
    private int status;
    private String[] statuses= new String[]{"Downloading", "Complete", "Pause", "Error"};
    private Downloader downloader;

    public ResumableDownloader(Context ctx,File targetFile, String urlStr) {
        file = targetFile;
        this.urlStr = urlStr;
        this.ctx = ctx;
    }

    public void start(){
        downloader = new Downloader();
        downloader.execute();
    }

    public void pause(){
        if (downloader!=null){
            downloader.cancel(true);
            setStatus(PAUSE);
            log("download paused");
        }
    }

    private String getLastModified(String url){
        try {
            url = URLEncoder.encode(url, "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }

        SharedPreferences prefs = ctx.getSharedPreferences("RESUMABLE_DOWNLOAD",Context.MODE_PRIVATE);
        return prefs.getString(url, "");
    }

    private void setLastModified(String url,String lastModified){
        try {
            url = URLEncoder.encode(url, "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }
        SharedPreferences prefs = ctx.getSharedPreferences("RESUMABLE_DOWNLOAD",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(url,lastModified);
        editor.apply();
    }

    private void prepareDownload() throws IOException {
        log("prepare download ....");
        HttpURLConnection conn = createConnection(urlStr);
        String remoteLastModified = conn.getHeaderField("Last-Modified");
        fileLength = conn.getContentLength();
        conn.disconnect();

        String lastModif = getLastModified(urlStr);
        startNewDownload = (!file.exists() ||
                file.length() >= fileLength ||
                !remoteLastModified.equalsIgnoreCase(lastModif));

        if (startNewDownload){
            log("start a new download");
        }else{
            log("resuming download");
        }
    }

    /**
     * @param urlStr           url string
     * @return An URLConnection for HTTP
     * @throws IOException
     */
    private HttpURLConnection createConnection(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setRequestMethod("GET");
        conn.setReadTimeout(timeout);
        conn.setConnectTimeout(timeout);
        return conn;
    }

    public void setStatus(int status){
        this.status = status;
    }

    public int getStatus(){
        return status;
    }

    class Downloader extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try {
                prepareDownload();
                setStatus(DOWNLOADING);
                HttpURLConnection connection = createConnection(urlStr);
                if (!startNewDownload) {
                    connection.setRequestProperty("Range", "bytes=" + file.length() + "-");
                }
                log("ResponseCode: " + connection.getResponseCode() +
                        "; file length:" + fileLength + "\nResponseMessage: " +
                        connection.getResponseMessage());
                InputStream in = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE);
                FileOutputStream writer;
                long progressLength = 0;
                if (!startNewDownload) {
                    progressLength += file.length();
                    log("resume download to: " + file.getAbsolutePath());
                    // append to exist downloadedFile
                    writer = new FileOutputStream(file, true);
                } else {
                    log("new download to: " + file.getAbsolutePath());
                    writer = new FileOutputStream(file);
                    // save remote last modified data to local
                    setLastModified(urlStr,connection.getHeaderField("Last-Modified"));
                }
                try {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int count;
                    while (ResumableDownloader.this.getStatus() == DOWNLOADING &&
                            (count = in.read(buffer)) != -1) {
                        progressLength += count;
                        writer.write(buffer, 0, count);
                        // progress....
                        progressUpdate( (int)(progressLength * 100 / fileLength));
                        if (progressLength == fileLength) {
                            progressLength = 0;
                            onComplete();
                        }
                    }
                } finally {
                    writer.close();
                    in.close();
                    connection.disconnect();
                }
            }catch (IOException e){
                log(e.getMessage());
            }
            return null;
        }
    }

    void progressUpdate(int value){
        Log.d("JEKI","Downloading "+value+"%");
    }

    void log(String msg){
        Log.d("JEKI",msg);
    }

    void onComplete(){
        setStatus(COMPLETE);
    }
}
