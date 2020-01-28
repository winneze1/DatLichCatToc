package com.example.datlichcattoc.Adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Rating;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datlichcattoc.Common.Common;
import com.example.datlichcattoc.Interface.IRecyclerItemSelectedListener;
import com.example.datlichcattoc.Model.Barber;
import com.example.datlichcattoc.Model.EventBus.EnableNextButton;
import com.example.datlichcattoc.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class MyBarberAdapter extends RecyclerView.Adapter<MyBarberAdapter.MyViewHolder> {

    Context context;
    List<Barber> barberList;
    List<CardView> cardViewList;
    //LocalBroadcastManager localBroadcastManager;

    public MyBarberAdapter(Context context, List<Barber> barberList) {
        this.context = context;
        this.barberList = barberList;
        cardViewList = new ArrayList<>();
        //localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_barber, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
            holder.txt_barber_name.setText(barberList.get(i).getName());
            if (barberList.get(i).getRatingTimes() != null)
                holder.ratingBar.setRating(barberList.get(i).getRating().floatValue() / barberList.get(i).getRatingTimes());
            else
                holder.ratingBar.setRating(0);
            if(!cardViewList.contains(holder.card_barber))
                cardViewList.add(holder.card_barber);

            holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                @Override
                public void onItemSelectedListener(View view, int pos) {
                    //Tao bg cho item ko dc chon :D
                    for (CardView cardView : cardViewList){
                        cardView.setCardBackgroundColor(context.getResources()
                                .getColor(android.R.color.white));
                    }
                        //Tao bg cho item dc chon
                    holder.card_barber.setCardBackgroundColor(
                            context.getResources()
                                    .getColor(android.R.color.holo_orange_dark)
                        );

                    //Gui local broadcast toi de hien nut next
//                    Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
//                    intent.putExtra(Common.KEY_BARBER_SELECTED, barberList.get(pos));
//                    intent.putExtra(Common.KEY_STEP, 2);
//                    localBroadcastManager.sendBroadcast(intent);

                    //EVENT BUS
                    EventBus.getDefault().postSticky(new EnableNextButton(2, barberList.get(i)));

                }
            });

    }

    @Override
    public int getItemCount() {
        return barberList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_barber_name;
        RatingBar ratingBar;
        CardView card_barber;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            card_barber = (CardView) itemView.findViewById(R.id.card_barber);
            txt_barber_name = (TextView) itemView.findViewById(R.id.txt_barber_name);
            ratingBar = (RatingBar) itemView.findViewById(R.id.rtb_barber);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view, getAdapterPosition());
        }
    }
}
