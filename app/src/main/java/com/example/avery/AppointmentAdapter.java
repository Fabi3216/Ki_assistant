package com.example.avery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<Appointment> appointments;

    public AppointmentAdapter(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public void updateAppointments(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textDescription;
        public TextView textDate;

        public ViewHolder(View view) {
            super(view);
            textDescription = view.findViewById(R.id.text_description);
            textDate = view.findViewById(R.id.text_date);
        }
    }

    @Override
    public AppointmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Appointment a = appointments.get(position);
        holder.textDescription.setText(a.description);

        if (a.timestamp > 0) {
            String formattedDate = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN)
                    .format(new Date(a.timestamp));
            holder.textDate.setText(formattedDate);
        } else {
            holder.textDate.setText("Kein Datum");
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }
}
