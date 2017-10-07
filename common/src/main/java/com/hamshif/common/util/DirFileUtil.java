package com.hamshif.common.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gideonbar on 07/10/2017.
 */

public class DirFileUtil
{
    private static final int MAX_COUNT_OF_LINES = 20000;
    private static boolean sLoggerInitialized=false;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final String TAG = DirFileUtil.class.getSimpleName();

    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(Context context, int type)
    {
        return Uri.fromFile(getOutputMediaFile(context, type));
    }

    /** Create a File for saving an image or video */
    public File getOutputMediaFile(Context context, int type)
    {
        String storage_path = context.getExternalFilesDir(null).getAbsolutePath();
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        if (type == MEDIA_TYPE_IMAGE)
        {
            mediaFile = new File(storage_path + File.separator +
                    "Bulzi_IMG_"+ timeStamp + ".jpg");
        }
        else if(type == MEDIA_TYPE_VIDEO)
        {
            mediaFile = new File(storage_path + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        }
        else
        {
            return null;
        }

        return mediaFile;
    }

    public static String verifyGetMediaPath(Context context)
    {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        String storage_path = context.getExternalFilesDir(null).getAbsolutePath();

        Log.i(TAG, "storage_path: " + storage_path);

        return storage_path;
    }


    public static String verifyPath(String path)
    {

        File f = new File(path);

        // Create the storage directory if it does not exist
        if (! f.exists())
        {
            if (! f.mkdirs())
            {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        return f.getPath();
    }

    public static File createDirs(String path)
    {
        File f = new File(path);

        // Create the storage directory if it does not exist
        if (! f.exists())
        {
            if (! f.mkdirs())
            {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        return f;
    }

    public static void copyFile(File src, File dst) throws IOException
    {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try
        {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        finally
        {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }


    public static void writeToFile(final String dirPath, final String fileName, final String[] data) {

        int mCountOfLines = -1;
        final String path = dirPath + fileName;
        final File outputFile = new File(path);

        FileWriter writer = null;
        try {
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }

            writer = new FileWriter(outputFile, true);

            for (String line: data){

                writer.write(line + "\\");
//                Log.i(TAG, "Wrote line");
            }

            int countOfLines = getCountOfLines(outputFile);
            if (countOfLines > MAX_COUNT_OF_LINES) {
                writer.write("\r\n--------------------------------------");
                writer.write("\r\nCLEAN LOGS, OLD 10%");
                writer.write("\r\nDATE: " + HamshifUtil.getDate());
                writer.write("\r\n--------------------------------------\r\n");
                deleteFirstLines(outputFile, mCountOfLines);
            }
            mCountOfLines++;
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    //do nothing
                    Log.e(TAG, "", e);
                }
            }
        }

    }


    private static void deleteFirstLines(File logFile, int mCountOfLines) throws IOException {
        File inputFile = logFile;
        File tempFile = new File(inputFile.getParent() + ".tmp");
        if (tempFile.exists()) tempFile.delete();

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        FileWriter writer = new FileWriter(tempFile, true);

        String currentLine;
        int counter = 0;
        final int percentLinesForDeleting = 10;
        final int countToDelete = mCountOfLines > 0 ? (mCountOfLines / percentLinesForDeleting) : (MAX_COUNT_OF_LINES / percentLinesForDeleting);
        while ((currentLine = reader.readLine()) != null) {
            counter++;
            if (countToDelete > counter) continue;
            try {
                writer.write(currentLine + "\r\n");
            } finally {
            }
        }
        writer.close();
        reader.close();
        tempFile.renameTo(inputFile);
    }

    private static int getCountOfLines(File textFile) throws IOException {

        LineNumberReader lnr = new LineNumberReader(new FileReader(textFile));
        lnr.skip(Long.MAX_VALUE);
        lnr.close();
        return lnr.getLineNumber();
    }
}

