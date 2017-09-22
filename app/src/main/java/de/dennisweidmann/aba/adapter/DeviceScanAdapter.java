package de.dennisweidmann.aba.adapter;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.dennisweidmann.aba.Model.BTLE.BTLEHandler;
import de.dennisweidmann.aba.R;

/**
 * Created by Jaysainath on 9/12/2017.
 */

public class DeviceScanAdapter extends RecyclerView.Adapter<DeviceScanAdapter.MyItemViewHolder> {


    private List<BluetoothDevice> foundDevices;
    private OnItemClickListner onItemClickListner;
    public interface OnItemClickListner {
        public void itemClickListner(View v, int position);
    }

    public void setOnItemClickListner(OnItemClickListner onItemClickListner){
        this.onItemClickListner = onItemClickListner;
    }

    public void setDeviceFound(List<BluetoothDevice> foundDevices) {
        this.foundDevices = foundDevices;
        notifyDataSetChanged();
    }

    @Override
    public MyItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_device, parent, false);
        return new MyItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyItemViewHolder holder, final int position) {

        holder.bluetoothText.setText(foundDevices.get(position).getAddress());

        if (BTLEHandler.sharedInstance().isConnectedDevice(foundDevices.get(position).getAddress())){
            holder.bluetoothImage.setImageResource(R.drawable.ic_bluetooth_black_48dp);
        }else{
            holder.bluetoothImage.setImageResource(R.drawable.ic_bluetooth_disabled_black_48dp);
        }


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setTag(foundDevices.get(position).getAddress());
                onItemClickListner.itemClickListner(v,position);

            }
        });

    }

    @Override
    public int getItemCount() {
        return foundDevices.size();
    }


    class MyItemViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        FrameLayout cardSeparator;
        FrameLayout cardShadow;
        FrameLayout circleView;
        FrameLayout clickLayout;
        ImageView bluetoothImage;
        TextView bluetoothText;
        ImageButton deleteButton;

        MyItemViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.row_device_card);
            cardSeparator = (FrameLayout) view.findViewById(R.id.row_device_separator);
            cardShadow = (FrameLayout) view.findViewById(R.id.row_device_shadow);
            circleView = (FrameLayout) view.findViewById(R.id.row_device_circle);
            clickLayout = (FrameLayout) view.findViewById(R.id.row_device_click_layout);
            bluetoothImage = (ImageView) view.findViewById(R.id.row_device_bluetooth_image);
            bluetoothText = (TextView) view.findViewById(R.id.row_device_bluetooth_text);
            deleteButton = (ImageButton) view.findViewById(R.id.row_device_delete_button);

        }
    }
}
