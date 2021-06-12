package com.example.LastCapston.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.LastCapston.R;
import com.example.LastCapston.data.UserItem;
import com.example.LastCapston.main.MainViewModel;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private MainViewModel mainViewModel;
    private Context context;

    public RecyclerViewAdapter(Context context, MainViewModel mainViewModel) {
        this.context = context;
        this.mainViewModel = mainViewModel;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textview;
        public ImageView imageView;
        public ImageView emotionImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textview = itemView.findViewById(R.id.item_textview);
            imageView = itemView.findViewById(R.id.item_imageView);
            emotionImageView = itemView.findViewById(R.id.emotionImage);
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // context 와 parent.getContext() 는 같다.
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_tv, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        UserItem item = mainViewModel.userList.get(position);
        holder.textview.setText(item.userName);
        if(item.speakState.equals("start")){
            holder.imageView.setImageResource(R.drawable.user_on);
        }else{
            holder.imageView.setImageResource(R.drawable.user_off);
        }

        if(item.userEmotionIcon.equals("joy")){
            holder.emotionImageView.setImageResource(R.drawable.joy);
        }else if(item.userEmotionIcon.equals("annoy")){
            holder.emotionImageView.setImageResource(R.drawable.annoy);
        }else if(item.userEmotionIcon.equals("fear")){
            holder.emotionImageView.setImageResource(R.drawable.fear);
        }else if(item.userEmotionIcon.equals("disgust")){
            holder.emotionImageView.setImageResource(R.drawable.disgust);
        }else if(item.userEmotionIcon.equals("sad")){
            holder.emotionImageView.setImageResource(R.drawable.sad);
        }else if(item.userEmotionIcon.equals("surprise")){
            holder.emotionImageView.setImageResource(R.drawable.surprise);
        }else{
            holder.emotionImageView.setImageResource(R.drawable.none);
        }
    }

    @Override
    public int getItemCount() {
        return mainViewModel.userList.size();
    }
}
