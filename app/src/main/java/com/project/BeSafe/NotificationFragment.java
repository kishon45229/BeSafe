package com.project.BeSafe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.project.BeSafe.AppDatabase;
import com.project.BeSafe.NotificationAdapter;
import com.project.BeSafe.NotificationEntity;
import com.project.BeSafe.R;

import java.util.List;

public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationEntity> notificationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Fetch notifications from the database
        AppDatabase db = Room.databaseBuilder(getContext(), AppDatabase.class, "notification-db").allowMainThreadQueries().build();
        notificationList = db.notificationDao().getAllNotifications();

        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);
        return view;
    }
}

