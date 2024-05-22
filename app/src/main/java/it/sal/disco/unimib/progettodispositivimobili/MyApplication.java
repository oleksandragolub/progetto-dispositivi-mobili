package it.sal.disco.unimib.progettodispositivimobili;

import android.app.Application;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ITALY);
        cal.setTimeInMillis(timestamp);

        //String date = DateFormat.getDateInstance().format("dd/MM/yyyy", cal).toString();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);
        String date = sdf.format(cal.getTime());

        return date;
    }
}
