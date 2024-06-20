package com.alivc.live.interactive_pk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.alivc.live.interactive_common.widget.InteractiveControlView;
import com.alivc.live.interactive_common.widget.RoomAndUserInfoView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiPKLiveRecyclerViewAdapter extends RecyclerView.Adapter<MultiPKLiveRecyclerViewAdapter.MultiPKLiveViewHolder> {
    private final Map<Integer, Integer> mFrameLayoutWithPositionMap = new HashMap<>();

    private OnPKItemClickListener mListener;
    private List<Boolean> mData;
    private OnPKItemViewChangedListener mOnPKItemViewChangedListener;

    private boolean isScene16In = false;

    public static final float ITEM_CONTAINER_WIDTH_16IN = 80f;

    public MultiPKLiveRecyclerViewAdapter(boolean scene16In) {
        isScene16In = scene16In;
    }

    public boolean resetFrameLayout(int position, int frameLayoutHashCode) {
        if (mFrameLayoutWithPositionMap.containsKey(position)) {
            if (frameLayoutHashCode == mFrameLayoutWithPositionMap.get(position)) {
                return false;
            } else {
                mFrameLayoutWithPositionMap.put(position, frameLayoutHashCode);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onViewAttachedToWindow(MultiPKLiveRecyclerViewAdapter.MultiPKLiveViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (mOnPKItemViewChangedListener != null) {
            mOnPKItemViewChangedListener.onItemViewAttachedToWindow(holder.getAdapterPosition());
        }
    }

    @Override
    public void onViewDetachedFromWindow(MultiPKLiveRecyclerViewAdapter.MultiPKLiveViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (mOnPKItemViewChangedListener != null) {
            mOnPKItemViewChangedListener.onItemViewDetachedToWindow(holder.getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public MultiPKLiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_multi_pklive_item, parent, false);
        return new MultiPKLiveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiPKLiveViewHolder holder, int position) {
        Boolean isPKing = mData.get(position);
        holder.getRenderFrameLayout().setVisibility(isPKing ? View.VISIBLE : View.INVISIBLE);
        holder.getUnConnectFrameLayout().setVisibility(isPKing ? View.INVISIBLE : View.VISIBLE);
        if (isPKing) {
            holder.getConnectView().setText(holder.itemView.getContext().getResources().getString(R.string.pk_stop_connect));
            holder.getConnectView().setBackground(holder.itemView.getContext().getResources().getDrawable(R.drawable.shape_interact_live_un_connect_btn_bg));
        } else {
            holder.getConnectView().setText(holder.itemView.getContext().getResources().getString(R.string.pk_start_connect));
            holder.getConnectView().setBackground(holder.itemView.getContext().getResources().getDrawable(R.drawable.shape_pysh_btn_bg));
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void setOnPKConnectClickListener(OnPKItemClickListener listener) {
        this.mListener = listener;
    }

    public void setOnPKItemViewChangedListener(OnPKItemViewChangedListener listener) {
        this.mOnPKItemViewChangedListener = listener;
    }

    public void setData(List<Boolean> dataList) {
        this.mData = dataList;
    }

    public class MultiPKLiveViewHolder extends RecyclerView.ViewHolder {

        private final RoomAndUserInfoView mUserInfoView;
        private InteractiveControlView mUserCtrlView;
        private final FrameLayout mOtherFrameLayout;
        private final FrameLayout mUnConnectFrameLayout;
        private final TextView mConnectTextView;

        public MultiPKLiveViewHolder(@NonNull View itemView) {
            super(itemView);
            mUserInfoView = itemView.findViewById(R.id.view_userinfo);

            mUserCtrlView = itemView.findViewById(R.id.view_ctrl);
            mUserCtrlView.enableMute(true);
            mUserCtrlView.initListener(new InteractiveControlView.OnClickEventListener() {
                @Override
                public void onClickMuteAudio(boolean mute) {
                    if (mListener != null) {
                        mListener.onPKMuteClick(getAdapterPosition(), mute);
                    }
                }

                @Override
                public void onClickEmptyView(boolean empty) {
                    if (mListener != null) {
                        mListener.onEmptyView(getAdapterPosition(), empty);
                    }
                }
            });

            mOtherFrameLayout = itemView.findViewById(R.id.render_container);
            mUnConnectFrameLayout = itemView.findViewById(R.id.fl_un_connect);

            mConnectTextView = itemView.findViewById(R.id.tv_connect);
            if (isScene16In) {
                mConnectTextView.setVisibility(View.GONE);

                int widthPixel = dip2px(itemView.getContext(), ITEM_CONTAINER_WIDTH_16IN);
                int heightPixel = dip2px(itemView.getContext(), ITEM_CONTAINER_WIDTH_16IN / 9 * 16);
                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(widthPixel, heightPixel);
                mOtherFrameLayout.setLayoutParams(layoutParams);

                itemView.setOnClickListener(view -> {
                    connectPK();
                });
            } else {
                mConnectTextView.setOnClickListener(view -> {
                    connectPK();
                });
            }
        }

        public void updateUserInfo(String channelId, String userId) {
            mUserInfoView.setUserInfo(channelId, userId);
        }

        public FrameLayout getRenderFrameLayout() {
            return mOtherFrameLayout;
        }

        public FrameLayout getUnConnectFrameLayout() {
            return mUnConnectFrameLayout;
        }

        public TextView getConnectView() {
            return mConnectTextView;
        }

        private void connectPK() {
            if (mListener != null) {
                mFrameLayoutWithPositionMap.put(getAdapterPosition(), mOtherFrameLayout.hashCode());
                mListener.onPKConnectClick(getAdapterPosition());
            }
        }
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @param dipValue DisplayMetrics类中属性density
     * @return
     */
    private static int dip2px(Context context, float dipValue) {
        if (context == null || context.getResources() == null)
            return 1;
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public interface OnPKItemClickListener {
        void onPKConnectClick(int position);

        void onPKMuteClick(int position, boolean mute);

        void onEmptyView(int position, boolean empty);
    }

    public interface OnPKItemViewChangedListener {
        void onItemViewAttachedToWindow(int position);

        void onItemViewDetachedToWindow(int position);
    }
}
