package de.troido.bledemo.epd.bt;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.troido.bledemo.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class BluetoothDeviceAdapter
        extends RecyclerView.Adapter<BluetoothDeviceAdapter.BluetoothDeviceHolder> {

    private final List<BluetoothDevice> devices;
    private final Context context;

    public BluetoothDeviceAdapter(List<BluetoothDevice> devices, Context context) {
        this.devices = new ArrayList<>(devices);
        this.context = context;
    }

    public void addAll(Collection<BluetoothDevice> devices) {
        this.devices.addAll(devices);
    }

    public void add(BluetoothDevice device) {
        if (!this.devices.contains(device)) {
            this.devices.add(device);
        }
    }

    public void clear() {
        this.devices.clear();
    }

    public ArrayList<BluetoothDevice> getDevices() {
        return new ArrayList<>(devices);
    }

    @Override
    public BluetoothDeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BluetoothDeviceHolder(
                LayoutInflater.from(context)
                              .inflate(R.layout.list_item_scan, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(BluetoothDeviceHolder holder, int position) {
        ((TextView) holder.itemView.findViewById(R.id.tv_scan))
                .setText(devices.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public class BluetoothDeviceHolder extends RecyclerView.ViewHolder {
        public BluetoothDeviceHolder(View itemView) {
            super(itemView);
        }
    }
}
