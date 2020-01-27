package com.example.fd.wifisignal;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.WifiListViewHolder> {
    private List<WifiModel> wifiList;

    WifiListAdapter(List<WifiModel> wifiList) {
        this.wifiList = wifiList;
    }

    List<WifiModel> getWifiList() {
        return wifiList;
    }

    void setWifiList(List<WifiModel> wifiList) {
        this.wifiList = wifiList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WifiListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WifiListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WifiListViewHolder holder, int position) {
        holder.wifiName.setText(wifiList.get(position).getSsid());
        holder.wifiSignal.setText(wifiList.get(position).getLevel());

    }

    @Override
    public int getItemCount() {
        return wifiList.size();
    }

    class WifiListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.wifi_name)
        TextView wifiName;
        @BindView(R.id.wifi_signal)
        TextView wifiSignal;

        WifiListViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
