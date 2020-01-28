package com.example.datlichcattoc.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datlichcattoc.Common.Common;
import com.example.datlichcattoc.Interface.IRecyclerItemSelectedListener;
import com.example.datlichcattoc.Model.EventBus.EnableNextButton;
import com.example.datlichcattoc.Model.TimeSlot;
import com.example.datlichcattoc.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<TimeSlot> timeSlotList;
    List<CardView> cardViewList;
    //LocalBroadcastManager localBroadcastManager;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        //this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<TimeSlot> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        //this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_time_slot, viewGroup, false);
        return new MyViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.txt_time_slot.setText(new StringBuilder(Common.convertTimeSlotToString(i)).toString());
        String myTimeString = myViewHolder.txt_time_slot.getText().toString();
        String[] convertString = myTimeString.split("-");
        String startTime = convertString[0];
        String[] startTimepart = startTime.split(":");
        int startTimeHour = Integer.valueOf(startTimepart[0]);
        int startTimeMinute = Integer.valueOf(startTimepart[1]);

        Calendar now = Calendar.getInstance();
        int nowHour = now.get(Calendar.HOUR_OF_DAY);
        int nowMin = now.get(Calendar.MINUTE);
        boolean timeCompare = 60*nowHour+nowMin > 60*startTimeHour+startTimeMinute;
        if (timeSlotList.size() == 0) //Nếu tất cả các vị trí chưa được đặt thì show list ra
        {
            if (timeCompare &&
                    Common.bookingDate.get(Calendar.DATE) == now.get(Calendar.DATE))
            {
                myViewHolder.txt_time_slot_description.setText("Full");
                myViewHolder.card_time_slot.setTag(Common.DISABLE_TAG);
                myViewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black));
                myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));
                myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources()
                        .getColor(android.R.color.holo_red_light));
                myViewHolder.card_time_slot.setEnabled(false);
            }
            else
            {
                //Nếu tất cả các slot trống thì tất cả các card được enable
                myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources()
                        .getColor(android.R.color.white));
                myViewHolder.txt_time_slot_description.setText("Available");
                myViewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black));
                myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));
                myViewHolder.card_time_slot.setEnabled(true);
            }
        }
        else //neu vi tri da duoc dat
        {
            if (timeCompare == true &&  Common.bookingDate.get(Calendar.DATE) == now.get(Calendar.DATE))
            {
                myViewHolder.txt_time_slot_description.setText("Full");
                myViewHolder.card_time_slot.setTag(Common.DISABLE_TAG);
                myViewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black));
                myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));
                myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources()
                        .getColor(android.R.color.holo_red_light));
                myViewHolder.card_time_slot.setEnabled(false);
            }
            if (timeCompare == false && Common.bookingDate.get(Calendar.DATE) == now.get(Calendar.DATE))
            {
                //Nếu tất cả các slot trống thì tất cả các card được enable
                myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources()
                        .getColor(android.R.color.white));
                myViewHolder.txt_time_slot_description.setText("Available");
                myViewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black));
                myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));
                myViewHolder.card_time_slot.setEnabled(true);
            }
            for (TimeSlot slotValue:timeSlotList)
            {
                //Tao vong lap cho lich va chinh mau lai
                int slot = Integer.parseInt(slotValue.getSlot().toString());
                if (slot == i) // neu slot == vi tri
                {
                    myViewHolder.card_time_slot.setEnabled(false);
                    myViewHolder.card_time_slot.setTag(Common.DISABLE_TAG);
                    myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                    myViewHolder.txt_time_slot_description.setText("Full");
                    myViewHolder.txt_time_slot_description.setTextColor(context.getResources()
                            .getColor(android.R.color.white));

                    myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));
                }
            }
        }



        //them cac card time slot vao list
        //ko them card ma da co trong cardViewList
        if (!cardViewList.contains(myViewHolder.card_time_slot))
            cardViewList.add(myViewHolder.card_time_slot);

        //kiem tra xem slot nao chua duoc dat
        if (!timeSlotList.contains(i))
        {
            myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                @Override
                public void onItemSelectedListener(View view, int pos) {
                    //tao vong lap tat ca cac card trong ds
                    for (CardView cardView:cardViewList)
                    {
                        if (cardView.getTag() == null) //Chỉ có những card chưa được đặt mới set nó màu trắng
                            cardView.setCardBackgroundColor(context.getResources()
                                    .getColor(android.R.color.white));
                    }
                    //nếu chọn card thì card đổi màu
                    myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources()
                            .getColor(android.R.color.holo_orange_dark));

                    //xong rui gui broadcast de hien nut next
//                    Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
//                    intent.putExtra(Common.KEY_TIME_SLOT, i); // bo index cua time slot ma ta da chon
//                    intent.putExtra(Common.KEY_STEP, 3); //di den buoc 3
//                    localBroadcastManager.sendBroadcast(intent);

                    //Event Bus
                    EventBus.getDefault().postSticky(new EnableNextButton(3, i));

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_time_slot, txt_time_slot_description;
        CardView card_time_slot;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            card_time_slot = (CardView) itemView.findViewById(R.id.card_time_slot);
            txt_time_slot = (TextView) itemView.findViewById(R.id.txt_time_slot);
            txt_time_slot_description = (TextView) itemView.findViewById(R.id.txt_time_slot_description);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view,getAdapterPosition());
        }
    }
}
