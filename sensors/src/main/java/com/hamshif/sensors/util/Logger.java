package com.hamshif.sensors.util;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by yura on 08.06.17.
 */

public class Logger {

    private final static String TAG = Logger.class.getSimpleName();
    private final static SimpleDateFormat SIMPLE_DATE_FORMAT_MAIN_LOGS = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
    private final static ThreadPoolExecutor LOGS_THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private static final String FILE_NAME = "somatix.log";
    private static final String DIR_NAME = "/sdcard/";
    private static final int MAX_COUNT_OF_LINES = 20000;

    private static final boolean LOG = true;

    private static final String ERROR = "E";
    private static final String DEBUG = "D";
    private static final String WARN = "W";
    private static final String INFO = "I";

    private static int mCountOfLines = -1;
    private static boolean sLoggerInitialized=false;
    private static String sAppVersion;

    public static void d(final String tag, final String message) {
        Log.d(tag, message);
        writeToFile(DEBUG, tag, message);
    }

    public static void e(final String tag, final String message) {
        Log.e(tag, message);
        writeToFile(ERROR, tag, message);
    }

    public static void w(final String tag, final String message) {
        Log.w(tag, message);
        writeToFile(WARN, tag, message);
    }

    public static void i(final String tag, final String message) {
        Log.w(tag, message);
        writeToFile(INFO, tag, message);
    }

    public static void e(final String tag, final String message, final Exception e) {
        e(tag, message + "  Exception=[" + (e != null ? e.getMessage() : null) + "]");
    }

    private static void writeToFile(final String logType, final String logTag, final String logMessage) {
        if (LOG) {
            writeToFile(logType, logTag, logMessage, true);
        }
    }

    private static void writeToFile(final String logType, final String logTag, final String logMessage, boolean async) {
        final long time = System.currentTimeMillis();
        if (LOG) {
            final Runnable saveLog = new Runnable() {
                @Override
                public void run() {
                    final String currentDateAndTime = SIMPLE_DATE_FORMAT_MAIN_LOGS.format(new Date(time));
                    final String path = DIR_NAME + FILE_NAME;
                    final File outputFile = new File(path);

                    FileWriter writer = null;
                    try {
                        if (!outputFile.exists()) {
                            outputFile.createNewFile();
                        }

                        writer = new FileWriter(outputFile, true);
                        if (!sLoggerInitialized) {
                            sLoggerInitialized = true;
                            try {
                                Context context = getApplicationUsingReflection();
                                final PackageInfo packageInfo = context
                                        .getPackageManager().getPackageInfo(context.getPackageName(), 0);
                                sAppVersion = packageInfo.versionName;
                            } catch (Exception ex) {
                                writer.write("\r\nAPP VERSION: UNKNOWN, e=" + ex.getMessage());
                                sAppVersion = "unknown";
                                Log.e(TAG, "", ex);
                            }

                        }
                        final String line = currentDateAndTime + " " + sAppVersion + "  " + logType + "/" + logTag + ": "
                                + logMessage + "\r\n";
                        writer.write(line);

                        Log.i(TAG, "Wrote line");

                        int countOfLines = getCountOfLines(outputFile);
                        if (countOfLines > MAX_COUNT_OF_LINES) {
                            writer.write("\r\n--------------------------------------");
                            writer.write("\r\nCLEAN LOGS, OLD 10%");
                            writer.write("\r\nDATE: " + currentDateAndTime);
                            writer.write("\r\n--------------------------------------\r\n");
                            deleteFirstLines(outputFile);
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
            };

            if (async) {
                LOGS_THREAD_POOL_EXECUTOR.execute(saveLog);
            } else {
                saveLog.run();
            }
        }
    }


    private static void deleteFirstLines(File logFile) throws IOException {
        File inputFile = logFile;
        File tempFile = new File(DIR_NAME + FILE_NAME + ".tmp");
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
        mCountOfLines = -1;
    }

    private static int getCountOfLines(File textFile) throws IOException {
        if (mCountOfLines <= 0) {
            LineNumberReader lnr = new LineNumberReader(new FileReader(textFile));
            lnr.skip(Long.MAX_VALUE);
            lnr.close();
            mCountOfLines = lnr.getLineNumber();
            return mCountOfLines;
        } else {
            return mCountOfLines;
        }
    }

    private static Application getApplicationUsingReflection() throws Exception {
        return (Application) Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication").invoke(null, (Object[]) null);
    }


}