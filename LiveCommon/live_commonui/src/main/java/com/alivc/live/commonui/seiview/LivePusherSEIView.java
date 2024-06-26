package com.alivc.live.commonui.seiview;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.alivc.live.commonui.R;

import java.util.regex.Pattern;

public class LivePusherSEIView extends LinearLayout {
    private static final int DEFAULT_PAYLOAD_TYPE = 5;
    private static final String DEFAULT_SEI_MESSAGE = "010203=-./,_adcd-&^%$~@";

    private EditText mPayloadEditText;
    private EditText mSeiEditText;
    private Button mSendSEIButton;

    private SendSeiViewListener mListener;

    public LivePusherSEIView(Context context) {
        super(context);
        init(context);
    }

    public LivePusherSEIView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LivePusherSEIView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View inflateView = LayoutInflater.from(context).inflate(R.layout.push_sei_view_layout, this, true);

        mPayloadEditText = inflateView.findViewById(R.id.et_sei_payload_type);
        mPayloadEditText.setText(String.valueOf(DEFAULT_PAYLOAD_TYPE));

        mSeiEditText = inflateView.findViewById(R.id.et_sei);
        mSeiEditText.setText(DEFAULT_SEI_MESSAGE);

        mSendSEIButton = inflateView.findViewById(R.id.btn_send_sei);
        mSendSEIButton.setOnClickListener(view -> {
            if (mListener != null) {
                // 如果输入框为空，默认输出指定的json格式以供测试
                String text = mSeiEditText.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    text = DEFAULT_SEI_MESSAGE;
                }

                // 如果输入框为空，默认payload type=5以供测试
                String payloadEdit = mPayloadEditText.getText().toString();
                int payload = DEFAULT_PAYLOAD_TYPE;
                Pattern pattern = Pattern.compile("[0-9]*");
                if (pattern.matcher(payloadEdit).matches()) {
                    payload = Integer.parseInt(payloadEdit);
                }

                mListener.onSendSeiClick(payload, text);
            }
        });
    }

    public interface SendSeiViewListener {
        void onSendSeiClick(int payload, String text);
    }

    public void setSendSeiViewListener(SendSeiViewListener mListener) {
        this.mListener = mListener;
    }
}
