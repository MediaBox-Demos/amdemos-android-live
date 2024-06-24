package com.alivc.live.baselive_push.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.alivc.live.commonui.bean.MusicInfo;
import com.alivc.live.baselive_push.R;

import java.io.File;
import java.util.ArrayList;

public class MusicSelectAdapter extends RecyclerView.Adapter<MusicSelectAdapter.MusicViewHolder> {

    private static final String BGM_ASSETS_PATH = "alivc_resource/BGM/";
    private static final String NETWORK_BGM_URL = "http://docs-aliyun.cn-hangzhou.oss.aliyun-inc.com/assets/attach/51991/cn_zh/1511776743437/JUST%202017.mp3";

    private final ArrayList<MusicInfo> sBGMList = new ArrayList<>();

    private OnItemClick mOnItemClick = null;

    private int mPosition = 0;

    public MusicSelectAdapter(Context context) {
        MusicInfo info = new MusicInfo(context.getResources().getString(R.string.no_music), "", "", "");
        sBGMList.add(info);
        ArrayList<MusicInfo> list = getMusicInfoResources(context);
        sBGMList.addAll(list);
        MusicInfo info1 = new MusicInfo(context.getResources().getString(R.string.internet_music), "", "", NETWORK_BGM_URL);
        sBGMList.add(info1);
    }

    private static ArrayList<MusicInfo> getMusicInfoResources(Context context) {
        ArrayList<MusicInfo> musicInfos = new ArrayList<>();
        String bgmDirectoryPath = context.getFilesDir() + File.separator + BGM_ASSETS_PATH;
        File bgmDirectory = new File(bgmDirectoryPath);
        if (bgmDirectory.isDirectory()) {
            File[] bgmFiles = bgmDirectory.listFiles((dir, name) -> name.endsWith(".mp3"));
            if (bgmFiles != null) {
                for (File file : bgmFiles) {
                    MusicInfo musicInfo = new MusicInfo();
                    musicInfo.setMusicName(file.getName());
                    musicInfo.setPath(file.getAbsolutePath());

                    musicInfos.add(musicInfo);
                }
            }
        }
        return musicInfos;
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_select_item_info, parent, false);
        MusicViewHolder holder = new MusicViewHolder(itemView);
        holder.tvMusicName = (TextView) itemView.findViewById(R.id.music_name);
        holder.tvMusicCheck = (ImageView) itemView.findViewById(R.id.music_check);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mOnItemClick != null) {
                    mOnItemClick.onItemClick(sBGMList.get(holder.getAdapterPosition()), holder.getAdapterPosition());
                }
                int lastPosition = mPosition;
                mPosition = holder.getAdapterPosition();
                notifyItemChanged(lastPosition);
                notifyItemChanged(mPosition);

            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final MusicViewHolder holder, final int position) {
        MusicInfo musicInfo = sBGMList.get(position);

        if (mPosition == position) {
            holder.tvMusicCheck.setVisibility(View.VISIBLE);
        } else {
            holder.tvMusicCheck.setVisibility(View.GONE);
        }
        if (musicInfo != null) {
            holder.tvMusicName.setText(musicInfo.getMusicName());
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return sBGMList.size();
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView tvMusicName;
        ImageView tvMusicCheck;

        public MusicViewHolder(View itemView) {
            super(itemView);
        }
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        mOnItemClick = onItemClick;
    }

    public interface OnItemClick {
        void onItemClick(MusicInfo musicInfo, int position);
    }
}
