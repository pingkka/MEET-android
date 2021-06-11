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


    //private ArrayList<UserItem> itemList;
    private MainViewModel mainViewModel;
    private Context context;
    //private View.OnClickListener onClickItem;

// , View.OnClickListener onClickItem
    public RecyclerViewAdapter(Context context, MainViewModel mainViewModel) {
        this.context = context;
        this.mainViewModel = mainViewModel;
        //this.itemList = itemList;
        //this.onClickItem = onClickItem;
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
        holder.imageView.setImageResource(R.drawable.user_off);
        //holder.textview.setTag(item);
        //holder.textview.setOnClickListener(onClickItem);
    }

    @Override
    public int getItemCount() {
        return mainViewModel.userList.size();
    }




}
