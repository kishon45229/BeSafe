package com.project.BeSafe;

import static androidx.constraintlayout.widget.StateSet.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.BeSafe.GetIncidentData;
import com.project.BeSafe.R;

import java.util.ArrayList;
import java.util.List;

public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.NewsFeedViewHolder> {

    private final List<GetIncidentData> incidentReportList;
    private DatabaseReference mDatabase;

    public NewsFeedAdapter(List<GetIncidentData> incidentReportList) {
        this.incidentReportList = incidentReportList;
    }

    @NonNull
    @Override
    public NewsFeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_newsfeed, parent, false);
        return new NewsFeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsFeedViewHolder holder, int position) {
        GetIncidentData incidentReport = incidentReportList.get(position);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        String userId = incidentReport.getUserId();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    if (firstName != null) {
                        holder.postOwner.setText(firstName);
                    } else {
                        Log.d(TAG, "First name is null");
                    }
                } else {
                    Log.d(TAG, "User data snapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Database error: " + error.getMessage());
            }
        });

        holder.incidentType.setText(incidentReport.getIncidentType());
        holder.date.setText(incidentReport.getDob());
        holder.time.setText(incidentReport.getTime());
        holder.address.setText(incidentReport.getStreetAddress() + ", " + incidentReport.getCity() + ", " + incidentReport.getDistrict());
        holder.severity.setText(incidentReport.getSeverity());
        holder.description.setText(incidentReport.getDescriptionValue());
        holder.childInjuries.setText("Child injuries :- " + incidentReport.getInjuredChildrenCount());
        holder.childFatalities.setText("Child fatalities :- " + incidentReport.getChildFatalitiesCount());
        holder.adultInjuries.setText("Adult injuries :- " + incidentReport.getInjuredAdultsCount());
        holder.adultFatalities.setText("Adult fatalities :- " + incidentReport.getAdultFatalitiesCount());

        List<String> mediaUrls = new ArrayList<>(incidentReport.getMedia().values());
        com.project.BeSafe.MediaAdapter mediaAdapter = new com.project.BeSafe.MediaAdapter(holder.itemView.getContext(), mediaUrls);
        holder.viewPagerMedia.setAdapter(mediaAdapter);

        new TabLayoutMediator(holder.tabLayoutDots, holder.viewPagerMedia, (tab, position1) -> {
            // You can customize the tab here if needed
        }).attach();
    }

    @Override
    public int getItemCount() {
        return incidentReportList.size();
    }

    static class NewsFeedViewHolder extends RecyclerView.ViewHolder {
        TextView postOwner, incidentType, date, time, address, severity, description, childInjuries, childFatalities, adultInjuries, adultFatalities;
        ViewPager2 viewPagerMedia;
        TabLayout tabLayoutDots;

        public NewsFeedViewHolder(@NonNull View itemView) {
            super(itemView);
            postOwner = itemView.findViewById(R.id.postOwner);
            incidentType = itemView.findViewById(R.id.incidentType);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            address = itemView.findViewById(R.id.address);
            severity = itemView.findViewById(R.id.severity);
            description = itemView.findViewById(R.id.description);
            childInjuries = itemView.findViewById(R.id.childInjuries);
            childFatalities = itemView.findViewById(R.id.childFatalities);
            adultInjuries = itemView.findViewById(R.id.adultInjuries);
            adultFatalities = itemView.findViewById(R.id.adultFatalities);
            viewPagerMedia = itemView.findViewById(R.id.viewPagerMedia);
            tabLayoutDots = itemView.findViewById(R.id.tabLayoutDots);
        }
    }
}
