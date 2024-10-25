package com.project.BeSafe;

import android.app.Application;
import androidx.room.Room;

public class MyApplication extends Application {
    private static MyApplication instance;
    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "notification-db").build();
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        return database;
    }
}

