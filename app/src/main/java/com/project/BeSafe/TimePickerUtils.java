package com.project.BeSafe;

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Locale;

public class TimePickerUtils {

    public static void showTimePickerDialog(Context context, final EditText timeEditText) {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Format the selected time in 12-hour format with AM/PM
                String period = hourOfDay >= 12 ? "PM" : "AM";
                int hour = hourOfDay % 12;
                if (hour == 0) hour = 12; // handle midnight and noon
                String formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, period);
                timeEditText.setText(formattedTime);
            }
        };

        // Create a new instance of TimePickerDialog and show it
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        new TimePickerDialog(context, timeSetListener, hour, minute, false).show(); // 'false' for 12-hour format
    }
}
