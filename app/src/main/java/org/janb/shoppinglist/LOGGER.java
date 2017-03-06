package org.janb.shoppinglist;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;

public class LOGGER {

    private static final String FILE = "ShoLiLog.txt";

    public static void log(Context context, String msg) {
        // Only log to file if debug mode in preferences is turned on
    if(!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("debug", false))
        return;

        msg = msg + "\n";
        try {
            File logFile = new File(Environment.getExternalStorageDirectory(), FILE);
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
            writer.write(msg);
            if (!logFile.exists()){
                logFile.createNewFile();
            }
            logFile.setWritable(true);

            FileOutputStream outputStream = new FileOutputStream(logFile);
            outputStream.write(msg.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
