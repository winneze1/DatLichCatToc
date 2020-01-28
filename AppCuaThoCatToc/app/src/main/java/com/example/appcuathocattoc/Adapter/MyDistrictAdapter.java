package com.example.appcuathocattoc.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appcuathocattoc.Common.Common;
import com.example.appcuathocattoc.Interface.IRecyclerItemSelectedListener;
import com.example.appcuathocattoc.Model.District;
import com.example.appcuathocattoc.R;
import com.example.appcuathocattoc.SalonListActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyDistrictAdapter extends RecyclerView.Adapter<MyDistrictAdapter.MyViewHolder> {

    Context context;
    List<District> districtList;

    int lastPosition = -1;

    public MyDistrictAdapter(Context context, List<District> districtList) {
        this.context = context;
        this.districtList = districtList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_district, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.txt_state_name.setText(districtList.get(i).getName());

        setAnimeation(myViewHolder.itemView,i);

        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int pos) {
                Common.district_name = districtList.get(pos).getName();
                context.startActivity(new Intent(context, SalonListActivity.class));
            }
        });
    }

    private void setAnimeation(View itemView, int position) {
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context,
                    android.R.anim.slide_in_left);
            itemView.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return districtList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.txt_district_name)
        TextView txt_state_name;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view, getAdapterPosition());
        }
    }
}
