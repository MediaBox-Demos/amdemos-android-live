package com.alivc.live.interactive_common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alivc.live.commonbiz.test.AliLiveStreamURLUtil;
import com.alivc.live.interactive_common.R;
import com.alivc.live.interactive_common.bean.InteractiveUserData;

import java.util.HashMap;

/**
 * 用户连麦窗口显示'房间ID'和'用户ID'
 */
public class RoomAndUserInfoView extends LinearLayout {
    private TextView mInfoTv;

    public RoomAndUserInfoView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public RoomAndUserInfoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RoomAndUserInfoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View inflateView = LayoutInflater.from(context).inflate(R.layout.layout_room_user_info_view, this, true);
        mInfoTv = inflateView.findViewById(R.id.tv_info);
    }

    public void setUserInfo(String channelId, String userId) {
        mInfoTv.setText(String.format("Room:%s\nUser:%s", channelId, userId));
    }

    public void setUrl(String url) {
        HashMap<String, String> params = AliLiveStreamURLUtil.parseUrl(url);
        mInfoTv.setText(String.format("Room:%s\nUser:%s",
                AliLiveStreamURLUtil.parseURLStreamName(params),
                params.get(AliLiveStreamURLUtil.USER_ID)));
    }

    public void setUserData(InteractiveUserData userData) {
        mInfoTv.setText(String.format("Room:%s\nUser:%s",
                userData != null ? userData.channelId : "",
                userData != null ? userData.userId : ""));
    }
}
