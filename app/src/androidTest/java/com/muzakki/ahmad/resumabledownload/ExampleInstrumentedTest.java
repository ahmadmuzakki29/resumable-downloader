package com.muzakki.ahmad.resumabledownload;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void downloadTest() throws Exception{
        Context ctx = InstrumentationRegistry.getContext();
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS)+"/resumabledownload.zip");
        ResumableDownloader downloader = new ResumableDownloader(ctx,file,"http://web4host.net/10MB.zip");
        downloader.start();
        Thread.sleep(5000);
        downloader.pause();
        Thread.sleep(2000);
        downloader.start();
        Thread.sleep(5000);
    }


}
