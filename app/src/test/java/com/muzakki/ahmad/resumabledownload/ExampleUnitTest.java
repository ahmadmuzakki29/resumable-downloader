package com.muzakki.ahmad.resumabledownload;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void getFilename()throws Exception{
        String url = "http://web4host.net/10MB.zip";
        String[] filenames = url.split("/");
        String filename = filenames[filenames.length-1];
        System.out.println(filename);
    }
}