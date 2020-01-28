package com.example.appcuathocattoc.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appcuathocattoc.Common.Common;
import com.example.appcuathocattoc.DoneServicesActivity;
import com.example.appcuathocattoc.Interface.IRecyclerItemSelectedListener;
import com.example.appcuathocattoc.Model.BookingInformation;
import com.example.appcuathocattoc.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<BookingInformation> timeSlotList;
    List<CardView> cardViewList;


    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        this.cardViewList = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<BookingInformation> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
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

        //Log.d("Time day", ""+Common.bookingDate.getTimeInMillis());
        boolean timeCompare = 60*nowHour+nowMin > 60*startTimeHour+startTimeMinute;

        if (timeSlotList.size() == 0) //neu tat ca vi tri chua co nguoi dat, thi hien ds
        {
            //set tag cho time slot la full
            //dua vao cac  tag nay de set may cai card con lai ma ko doi het time slot
            if (timeCompare &&
                    Common.bookingDate.get(Calendar.DATE) == now.get(Calendar.DATE))
            {
                myViewHolder.txt_time_slot_description.setText("Full");
                myViewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black));
                myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));
                myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources()
                        .getColor(android.R.color.holo_red_light));
            }
            else
            {
                myViewHolder.txt_time_slot_description.setText("Available");
                myViewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black));
                myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));
                myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources()
                        .getColor(android.R.color.white));
            }


            //Thêm Event
            myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                @Override
                public void onItemSelectedListener(View view, int pos) {
                    //Thêm function này ko thì crash
                }
            });
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
            for (BookingInformation slotValue:timeSlotList)
            {
                //Tao vong lap cho lich va chinh mau lai
                int slot = Integer.parseInt(slotValue.getSlot().toString());
                if (slot == i) // neu slot == vi tri
                {
                    if (!slotValue.isDone()) {
                        myViewHolder.card_time_slot.setTag(Common.DISABLE_TAG);

                        myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                        myViewHolder.txt_time_slot_description.setText("Full");
                        myViewHolder.txt_time_slot_description.setTextColor(context.getResources()
                                .getColor(android.R.color.white));

                        myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));
                        //myViewHolder.card_time_slot.setEnabled(false);
                        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelectedListener(View view, int pos) {
                                //Chỉ thêm cho những time slot có màu xám
                                //lấy thông tin booking và lưu nó vào Common.currentBookingInformation
                                //Sau đó mới chuyển sang DoneServiceActivity
                                FirebaseFirestore.getInstance()
                                        .collection("AllSalon")
                                        .document(Common.district_name)
                                        .collection("Branch")
                                        .document(Common.selectedSalon.getSalonID())
                                        .collection("Barbers")
                                        .document(Common.currentBarber.getBarberId())
                                        .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                                        .document(slotValue.getSlot().toString())
                                        .get()
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    if (task.getResult().exists()) {

                                                        Common.currentBookingInformation = task.getResult().toObject(BookingInformation.class);
                                                        Common.currentBookingInformation.setBookingId(task.getResult().getId());
                                                        context.startActivity(new Intent(context, DoneServicesActivity.class));
                                                    }
                                                }
                                            }
                                        });

                            }
                        });
                    }
                    else
                    {
                        //Nếu đã thanh toán xong
                        myViewHolder.card_time_slot.setTag(Common.DISABLE_TAG);

                        myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));

                        myViewHolder.txt_time_slot_description.setText("Done");
                        myViewHolder.txt_time_slot_description.setTextColor(context.getResources()
                                .getColor(android.R.color.white));
                        myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));

                        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelectedListener(View view, int pos) {
                                //
                            }
                        });


                    }
                }
                else
                {
                    //Thêm cái này vào thì hết crash
                    if (myViewHolder.getiRecyclerItemSelectedListener() == null)
                    {
//                        if ((60*nowHour+nowMin > 60*startTimeHour+startTimeMinute) &&
//                                Common.bookingDate.get(Calendar.DATE) == now.get(Calendar.DATE))
//                        {
//                            myViewHolder.txt_time_slot_description.setText("Full");
//                            myViewHolder.card_time_slot.setTag(Common.DISABLE_TAG);
//                            myViewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black));
//                            myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));
//                            myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources()
//                                    .getColor(android.R.color.holo_red_light));
//                            myViewHolder.card_time_slot.setEnabled(false);
//                        }
//                        else
//                        {
//                            //Nếu tất cả các slot trống thì tất cả các card được enable
//                            myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources()
//                                    .getColor(android.R.color.white));
//                            myViewHolder.txt_time_slot_description.setText("Available");
//                            myViewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black));
//                            myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));
//                            myViewHolder.card_time_slot.setEnabled(false);
//                        }
                        //Thêm event cho view holder mà ko dc click
                        //Nếu ko thêm thằng này
                        //Thì tất cả các timeslot có thời gian lớn hơn thằng hiện tại thì sẽ bị chèn event
                        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelectedListener(View view, int pos) {

                            }
                        });
                    }
                }
            }
        }

        //them cac card time slot vao list
        //ko them card ma da co trong cardViewList
        if (!cardViewList.contains(myViewHolder.card_time_slot))
            cardViewList.add(myViewHolder.card_time_slot);


//        //kiem tra xem slot nao chua duoc dat
//        if (!timeSlotList.contains(i))
//        {
//            myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
//                @Override
//                public void onItemSelectedListener(View view, int pos) {
//                    //tao vong lap tat ca cac card trong ds
//                    for (CardView cardView:cardViewList)
//                    {
//                        if (cardView.getTag() == null) //chi may cai card chua duoc dat moi dc chon
//                            cardView.setCardBackgroundColor(context.getResources()
//                                    .getColor(android.R.color.white));
//                    }
//                    //neu card dc chon thi se doi mau
//                    myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources()
//                            .getColor(android.R.color.holo_orange_dark));
//
//
//                }
//            });
//        }
    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_time_slot, txt_time_slot_description;
        CardView card_time_slot;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public IRecyclerItemSelectedListener getiRecyclerItemSelectedListener() {
            return iRecyclerItemSelectedListener;
        }

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