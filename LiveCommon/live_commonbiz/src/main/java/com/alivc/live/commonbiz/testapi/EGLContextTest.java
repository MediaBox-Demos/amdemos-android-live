package com.alivc.live.commonbiz.testapi;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * @author keria
 * @date 2024/7/5
 * @brief FIXME keria:测试代码
 */
public class EGLContextTest {

    private static final String TAG = "EGLContextTest";

    private EGLContextTest() {
    }

    /**
     * 创建 EGLContext
     */
    public static void testGLContext() {

        int maxContexts = 0;

        // 假设尝试创建最多1000个上下文
        EGLContext[] contexts = new EGLContext[1000];

        EGLDisplay eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.e(TAG, "Unable to get EGL14 display");
            return;
        }

        int[] version = new int[2];
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            Log.e(TAG, "Unable to initialize EGL14");
            return;
        }

        int[] configAttribs = {
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 24,
                EGL14.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, configs, 0, 1, numConfigs, 0)) {
            Log.e(TAG, "Unable to choose config");
            return;
        }

        int[] contextAttribs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };

        for (int i = 0; i < contexts.length; i++) {
            contexts[i] = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0);
            if (contexts[i] == EGL14.EGL_NO_CONTEXT) {
                Log.e(TAG, "Failed to create EGL context at index " + i + ": " + EGL14.eglGetError() + " - " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
                break;
            }
            maxContexts++;
        }

        Log.d(TAG, "Maximum number of OpenGL ES contexts supported: " + maxContexts);

        // 清理所有创建的上下文
        for (int i = 0; i < maxContexts; i++) {
            if (contexts[i] != EGL14.EGL_NO_CONTEXT) {
                EGL14.eglDestroyContext(eglDisplay, contexts[i]);
            }
        }

        EGL14.eglTerminate(eglDisplay);
    }
}
