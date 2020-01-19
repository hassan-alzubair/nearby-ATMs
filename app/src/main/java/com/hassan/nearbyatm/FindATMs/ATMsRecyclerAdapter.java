package com.hassan.nearbyatm.FindATMs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//import com.bumptech.glide.Glide;
//import com.bumptech.glide.RequestBuilder;

import com.hassan.nearbyatm.R;

import java.text.DecimalFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ATMsRecyclerAdapter extends RecyclerView.Adapter<ATMsRecyclerAdapter.ATMViewHolder> {

    private Context context;
    private List<ATM> atmList;
    private OnATMClickedListener listener;

    public ATMsRecyclerAdapter(Context context, List<ATM> atmList) {
        this.context = context;
        this.atmList = atmList;
    }

    @NonNull
    @Override
    public ATMViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ATMViewHolder(LayoutInflater.from(context).inflate(R.layout.atm_row_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ATMViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onATMClick(atmList.get(position));
                }
            }
        });
        holder.txtName.setText(atmList.get(position).getName());
        holder.txtVicinity.setText(atmList.get(position).getVicinity());
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        holder.txtDistance.setText(decimalFormat.format(atmList.get(position).getDistance()) + " KM from you");
    }

    @Override
    public int getItemCount() {
        return atmList.size();
    }

    public static class ATMViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView txtDistance;
        TextView txtVicinity;
        CircleImageView circleImageView;

        public ATMViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.atm_name);
            circleImageView = itemView.findViewById(R.id.atm_icon);
            txtDistance = itemView.findViewById(R.id.atm_distance);
            txtVicinity = itemView.findViewById(R.id.atm_vicinity);
        }
    }

    public void setOnATMClickListener(OnATMClickedListener listener) {

        this.listener = listener;
    }

    public interface OnATMClickedListener {
        void onATMClick(ATM atm);
    }

}
