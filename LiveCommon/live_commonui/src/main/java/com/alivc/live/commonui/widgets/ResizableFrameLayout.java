package com.alivc.live.commonui.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.Random;

/**
 * @author keria
 * @date 2024/7/9
 * @brief 可缩放的FrameLayout
 * @note 备注：该FrameLayout容器仅用于API测试，测试在不同FrameLayout切换策略下的表现及性能
 * @note 如果您对接直播连麦，可直接使用FrameLayout容器作为预览和拉流的渲染窗口
 */
public class ResizableFrameLayout extends FrameLayout {
    private static final int ANIMATION_DURATION = 200;

    private int originalWidth;
    private int originalHeight;
    private boolean isResized = false;

    private static final ResizeStrategy[] strategies = new ResizeStrategy[]{new HalveStrategy(), new EqualStrategy()};
    private ResizeStrategy currentStrategy;
    private final Random random = new Random();

    public ResizableFrameLayout(Context context) {
        super(context);

        initializeDefaultStrategy();
    }

    public ResizableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        initializeDefaultStrategy();
    }

    public ResizableFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initializeDefaultStrategy();
    }

    private void initializeDefaultStrategy() {
        currentStrategy = new HalveStrategy(); // 默认为缩放策略
    }

    public void resize() {
        useRandomStrategy();
        toggleSize();
    }

    public void resize(ResizeStrategy strategy) {
        setResizeStrategy(strategy);
        toggleSize();
    }

    private void toggleSize() {
        // 保存原始宽高
        if (originalWidth == 0 || originalHeight == 0) {
            originalWidth = getWidth();
            originalHeight = getHeight();
        }

        // 根据策略计算目标宽高
        int targetWidth = isResized ? originalWidth : currentStrategy.getTargetWidth(originalWidth, originalHeight);
        int targetHeight = isResized ? originalHeight : currentStrategy.getTargetHeight(originalWidth, originalHeight);

        // 创建动画
        animateResize(targetWidth, targetHeight);

        // 切换标志位
        isResized = !isResized;
    }

    private void animateResize(int targetWidth, int targetHeight) {
        ValueAnimator widthAnimator = ValueAnimator.ofInt(getWidth(), targetWidth);
        ValueAnimator heightAnimator = ValueAnimator.ofInt(getHeight(), targetHeight);

        widthAnimator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.width = (int) animation.getAnimatedValue();
            setLayoutParams(layoutParams);
        });

        heightAnimator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.height = (int) animation.getAnimatedValue();
            setLayoutParams(layoutParams);
        });

        widthAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        widthAnimator.setDuration(ANIMATION_DURATION);
        heightAnimator.setDuration(ANIMATION_DURATION);

        widthAnimator.start();
        heightAnimator.start();
    }

    public void setResizeStrategy(ResizeStrategy strategy) {
        this.currentStrategy = strategy;
    }

    public void useRandomStrategy() {
        int randomIndex = random.nextInt(strategies.length);
        setResizeStrategy(strategies[randomIndex]);
    }

    /**
     * 缩放策略（宽高除以2策略）
     */
    public static class HalveStrategy implements ResizeStrategy {
        @Override
        public int getTargetWidth(int originalWidth, int originalHeight) {
            return originalWidth / 2;
        }

        @Override
        public int getTargetHeight(int originalWidth, int originalHeight) {
            return originalHeight / 2;
        }
    }

    /**
     * 正方形策略（宽高相等）
     */
    public static class EqualStrategy implements ResizeStrategy {
        @Override
        public int getTargetWidth(int originalWidth, int originalHeight) {
            return Math.min(originalWidth, originalHeight);
        }

        @Override
        public int getTargetHeight(int originalWidth, int originalHeight) {
            return Math.min(originalWidth, originalHeight);
        }
    }

    public interface ResizeStrategy {
        int getTargetWidth(int originalWidth, int originalHeight);

        int getTargetHeight(int originalWidth, int originalHeight);
    }
}
