package com.hamshif.common.util;

import android.app.Application;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by gideonbar on 05/10/2017.
 */

public class HamshifUtil {

    public static String getDate(long timeStamp){

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeStamp);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ms");
        return sdf.format(d);
    }

    public static String getDate(){

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ms");
        return sdf.format(currentTime);
    }

    public static Application getApplicationUsingReflection() throws Exception {
        return (Application) Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication").invoke(null, (Object[]) null);
    }

    private static Context getContext() throws Exception{
        return getApplicationUsingReflection().getApplicationContext();
    }
}
