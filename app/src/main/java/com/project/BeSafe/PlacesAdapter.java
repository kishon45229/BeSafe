package com.project.BeSafe;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.BeSafe.PlacesResponse.Result;

import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder> {
    private final Context context;
    private final List<Result> places;

    public PlacesAdapter(Context context, List<Result> places) {
        this.context = context;
        this.places = places;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Result place = places.get(position);
        holder.placeName.setText(place.name);
        holder.placeAddress.setText(place.vicinity);
        String phoneNumber = place.formatted_phone_number != null ? place.formatted_phone_number : "N/A";
        holder.placePhone.setText(phoneNumber);

        holder.callButton.setOnClickListener(v -> {
            if (!phoneNumber.equals("N/A")) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public void addPlace(Result place) {
        places.add(place);
        notifyItemInserted(places.size() - 1);
    }

    public void clearPlaces() {
        places.clear();
        notifyDataSetChanged();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView placeName;
        TextView placeAddress;
        TextView placePhone;
        Button callButton;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.place_name);
            placeAddress = itemView.findViewById(R.id.place_address);
            placePhone = itemView.findViewById(R.id.place_contact);
            callButton = itemView.findViewById(R.id.call_button);
        }
    }
}

