package com.project.BeSafe;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    void insert(NotificationEntity notification);

    @Query("SELECT * FROM notifications")
    List<NotificationEntity> getAllNotifications();
}
