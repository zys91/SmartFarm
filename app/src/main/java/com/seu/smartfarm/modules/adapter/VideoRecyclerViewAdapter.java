package com.seu.smartfarm.modules.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.seu.smartfarm.R;
import com.seu.smartfarm.modules.adapter.listener.OnItemChildClickListener;
import com.seu.smartfarm.modules.adapter.listener.OnItemClickListener;
import com.seu.smartfarm.modules.bean.VideoBean;

import java.util.List;

import xyz.doikki.videocontroller.component.PrepareView;

public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<VideoRecyclerViewAdapter.VideoHolder> {

    private List<VideoBean> videos;

    private OnItemChildClickListener mOnItemChildClickListener;

    private OnItemClickListener mOnItemClickListener;

    public VideoRecyclerViewAdapter(List<VideoBean> videos) {
        this.videos = videos;
    }

    @Override
    @NonNull
    public VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoHolder holder, @SuppressLint("RecyclerView") int position) {

        VideoBean videoBean = videos.get(position);

        Glide.with(holder.mThumb.getContext())
                .load(videoBean.getThumb())
                .placeholder(android.R.color.darker_gray)
                .into(holder.mThumb);
        holder.mTitle.setText(videoBean.getTitle());

        holder.mPosition = position;
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public void addData(List<VideoBean> videoList) {
        int size = videos.size();
        videos.addAll(videoList);
        //使用此方法添加数据，使用notifyDataSetChanged会导致正在播放的视频中断
        notifyItemRangeChanged(size, videos.size());
    }

    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        mOnItemChildClickListener = onItemChildClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public class VideoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public int mPosition;
        public FrameLayout mPlayerContainer;
        public TextView mTitle;
        public ImageView mThumb;
        public PrepareView mPrepareView;

        VideoHolder(View itemView) {
            super(itemView);
            mPlayerContainer = itemView.findViewById(R.id.player_container);
            mTitle = itemView.findViewById(R.id.tv_title);
            mPrepareView = itemView.findViewById(R.id.prepare_view);
            mThumb = mPrepareView.findViewById(R.id.thumb);
            if (mOnItemChildClickListener != null) {
                mPlayerContainer.setOnClickListener(this);
            }
            if (mOnItemClickListener != null) {
                itemView.setOnClickListener(this);
            }
            //通过tag将ViewHolder和itemView绑定
            itemView.setTag(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.player_container) {
                if (mOnItemChildClickListener != null) {
                    mOnItemChildClickListener.onItemChildClick(mPosition);
                }
            } else {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mPosition);
                }
            }
        }
    }
}