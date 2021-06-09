package com.example.LastCapston.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LastCapston.R;
import com.example.LastCapston.data.Code;
import com.example.LastCapston.data.MessageItem;

import java.util.ArrayList;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<MessageItem> myDataList = null;

    public ChatMessageAdapter(ArrayList<MessageItem> dataList) {
        myDataList = dataList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(viewType == Code.ViewType.CENTER_CONTENT){
            view = inflater.inflate(R.layout.room_center_item, parent,false);
            return new CenterViewHolder(view);
        }else if(viewType == Code.ViewType.LEFT_CONTENT){
            view = inflater.inflate(R.layout.room_left_item, parent,false);
            return new LeftViewHolder(view);
        }else{
            view = inflater.inflate(R.layout.room_right_item, parent,false);
            return new RightViewHolder(view);
        }

    }

    // 실제 각 뷰 홀더에 데이터를 연결해주는 함수
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if(viewHolder instanceof CenterViewHolder){
            ((CenterViewHolder)viewHolder).textv.setText(myDataList.get(position).getContent());
        }else if(viewHolder instanceof LeftViewHolder){
            ((LeftViewHolder)viewHolder).textv_nicname.setText(myDataList.get(position).getName());
            ((LeftViewHolder)viewHolder).textv_msg.setText(myDataList.get(position).getContent());
            ((LeftViewHolder)viewHolder).textv_time.setText(myDataList.get(position).getTime());

            if(myDataList.get(position).getImg().equals("joy")){
                ((LeftViewHolder)viewHolder).imgv.setImageResource(R.drawable.joy);
            }else if(myDataList.get(position).getImg().equals("annoy")){
                ((LeftViewHolder)viewHolder).imgv.setImageResource(R.drawable.annoy);
            }else if(myDataList.get(position).getImg().equals("fear")){
                ((LeftViewHolder)viewHolder).imgv.setImageResource(R.drawable.fear);
            }else if(myDataList.get(position).getImg().equals("disgust")){
                ((LeftViewHolder)viewHolder).imgv.setImageResource(R.drawable.disgust);
            }else if(myDataList.get(position).getImg().equals("sad")){
                ((LeftViewHolder)viewHolder).imgv.setImageResource(R.drawable.sad);
            }else if(myDataList.get(position).getImg().equals("surprise")){
                ((LeftViewHolder)viewHolder).imgv.setImageResource(R.drawable.surprise);
            }else{
                ((LeftViewHolder)viewHolder).imgv.setImageResource(R.drawable.none);
            }


        }else{
            ((RightViewHolder)viewHolder).textv_msg.setText(myDataList.get(position).getContent());
            ((RightViewHolder)viewHolder).textv_time.setText(myDataList.get(position).getTime());
            if(myDataList.get(position).getImg().equals("joy")){
                ((RightViewHolder)viewHolder).imgv.setImageResource(R.drawable.joy);
            }else if(myDataList.get(position).getImg().equals("annoy")){
                ((RightViewHolder)viewHolder).imgv.setImageResource(R.drawable.annoy);
            }else if(myDataList.get(position).getImg().equals("fear")){
                ((RightViewHolder)viewHolder).imgv.setImageResource(R.drawable.fear);
            }else if(myDataList.get(position).getImg().equals("disgust")){
                ((RightViewHolder)viewHolder).imgv.setImageResource(R.drawable.disgust);
            }else if(myDataList.get(position).getImg().equals("sad")){
                ((RightViewHolder)viewHolder).imgv.setImageResource(R.drawable.sad);
            }else if(myDataList.get(position).getImg().equals("surprise")){
                ((RightViewHolder)viewHolder).imgv.setImageResource(R.drawable.surprise);
            }else{
                ((RightViewHolder)viewHolder).imgv.setImageResource(R.drawable.none);
            }
        }

    }

    @Override
    public int getItemCount() {
        return myDataList.size();
    }

    //
    @Override
    public int getItemViewType(int position) {
        return myDataList.get(position).getViewType();
    }

    // "리사이클러뷰에 들어갈 뷰 홀더", 그리고 "그 뷰 홀더에 들어갈 아이템들을 셋팅"
    public class CenterViewHolder extends RecyclerView.ViewHolder{
        TextView textv;

        public CenterViewHolder(@NonNull View itemView) {
            super(itemView);
            textv = (TextView)itemView.findViewById(R.id.textv);
        }
    }

    public class LeftViewHolder extends RecyclerView.ViewHolder{
        ImageView imgv;
        TextView textv_nicname;
        TextView textv_msg;
        TextView textv_time;

        public LeftViewHolder(@NonNull View itemView) {
            super(itemView);
            imgv = (ImageView)itemView.findViewById(R.id.imageView);
            textv_nicname = (TextView)itemView.findViewById(R.id.textv_nicname);
            textv_msg = (TextView)itemView.findViewById(R.id.textv_msg);
            textv_time = (TextView)itemView.findViewById(R.id.textv_time);

        }
    }

    public class RightViewHolder extends RecyclerView.ViewHolder{
        ImageView imgv;
        TextView textv_msg;
        TextView textv_time;

        public RightViewHolder(@NonNull View itemView) {
            super(itemView);
            imgv = (ImageView)itemView.findViewById(R.id.imageView);
            textv_msg = (TextView)itemView.findViewById(R.id.textv_msg);
            textv_time = (TextView)itemView.findViewById(R.id.textv_time);
        }
    }

}
