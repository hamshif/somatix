package com.hamshif.sensors.views;

import android.hardware.Sensor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hamshif.sensors.R;
import com.hamshif.sensors.interfaces.SensorSetter;
import com.hamshif.sensors.model.SomatixSensor;

import java.util.List;

/**
 * Created by gideonbar on 05/10/2017.
 */

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.SensorViewHolder> {

    private List<SomatixSensor> sensorList;
    private SensorSetter sensorSetter;

    public SensorAdapter(List<SomatixSensor> sensorList, SensorSetter sensorSetter){
        this.sensorList = sensorList;
        this.sensorSetter = sensorSetter;
    }

    @Override
    public SensorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sensor, parent, false);

        SensorViewHolder holder = new SensorViewHolder(itemView);

        itemView.setOnClickListener(v -> {
            final int position = holder.getAdapterPosition();
            final Sensor sensor = sensorList.get(position).getSensor();
            sensorSetter.setSensor(sensor.getType());
            Toast.makeText(v.getContext(), sensor.getVendor(), Toast.LENGTH_SHORT).show();
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(SensorViewHolder holder, int position) {

        SomatixSensor sensor = sensorList.get(position);
        holder.sensorType.setText(sensor.getSensor().getName());
        holder.sensorIcon.setImageResource(sensor.getIconId());
    }

    @Override
    public int getItemCount() {
        return sensorList.size();
    }

    public class SensorViewHolder extends RecyclerView.ViewHolder {

        public TextView sensorType;
        public ImageView sensorIcon;

        public SensorViewHolder(View itemView) {
            super(itemView);

            this.sensorType = (TextView) itemView.findViewById(R.id.tv_sensor_type);
            this.sensorIcon = (ImageView) itemView.findViewById(R.id.iv_sensor_icon);
        }
    }
}
