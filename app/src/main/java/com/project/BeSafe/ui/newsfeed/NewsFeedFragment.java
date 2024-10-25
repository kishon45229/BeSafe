/*package com.project.BeSafe.ui.newsfeed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.BeSafe.GetIncidentData;
import com.project.BeSafe.NewsFeedAdapter;
import com.project.BeSafe.R;

import java.util.ArrayList;
import java.util.List;

public class NewsFeedFragment extends Fragment {
    private RecyclerView recyclerView;
    private NewsFeedAdapter adapter;
    private List<GetIncidentData> incidentReportList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_newsfeed, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewNewsFeed);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        incidentReportList = new ArrayList<>();
        adapter = new NewsFeedAdapter(incidentReportList);
        recyclerView.setAdapter(adapter);

        loadIncidentReports();

        return view;
    }

    private void loadIncidentReports() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("incidents");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                incidentReportList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    GetIncidentData incidentReport = snapshot.getValue(GetIncidentData.class);
                    incidentReportList.add(incidentReport);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}
*/

package com.project.BeSafe.ui.newsfeed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.BeSafe.GetIncidentData;
import com.project.BeSafe.NewsFeedAdapter;
import com.project.BeSafe.R;

import java.util.ArrayList;
import java.util.List;

public class NewsFeedFragment extends Fragment {
    private static final String TAG = "NewsFeedFragment";
    private RecyclerView recyclerView;
    private NewsFeedAdapter adapter;
    private List<GetIncidentData> incidentReportList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_newsfeed, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewNewsFeed);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Initialize the incident report list and adapter
        incidentReportList = new ArrayList<>();
        adapter = new NewsFeedAdapter(incidentReportList);
        recyclerView.setAdapter(adapter);

        loadIncidentReports();

        return view;
    }

    //Retrieve incident reports from the Firebase database
    private void loadIncidentReports() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("incidents");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    incidentReportList.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        GetIncidentData incidentReport = snapshot.getValue(GetIncidentData.class);

                        if (incidentReport != null) {
                            incidentReportList.add(incidentReport);
                        } else {
                            Log.e(TAG, "Encountered null incident report");
                        }
                    }

                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(TAG, "Error processing incident reports", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Failed to load incident reports", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
