package com.alivc.live.commonutils.viewhierarchy;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author baorunchen
 * @date 2024/5/16
 * @brief Monitor changes in view hierarchy and regularly print view hierarchy and overlaps
 */

/****
 * @par Call Example
 * @code
 * private ViewHierarchyInspector mViewHierarchyInspector;
 *
 *  @Override protected void onCreate(Bundle savedInstanceState) {
 *  super.onCreate(savedInstanceState);
 *
 *  // xxx;
 *
 *  mViewHierarchyInspector = new ViewHierarchyInspector();
 *  mViewHierarchyInspector.setTargetView(mOwnerFrameLayout);
 *  mViewHierarchyInspector.setDecorView(getWindow().getDecorView());
 *  }
 *
 *  @Override protected void onDestroy() {
 *  super.onDestroy();
 *
 *  // xxx;
 *
 *  if (mViewHierarchyInspector != null) {
 *  mViewHierarchyInspector.destroy();
 *  mViewHierarchyInspector = null;
 *  }
 *  }
 * @endcode
 */
public class ViewHierarchyInspector {
    private static final String TAG = "ViewHierarchyInspector";

    private static final long SCHEDULED_EXECUTOR_SERVICE_PERIOD = 2 * 1000L;
    private ScheduledExecutorService mScheduledExecutorService = null;

    private View mTargetView = null;
    private View mDecorView = null;

    /**
     * Sets the target view to inspect and starts the periodic inspection timer.
     * It logs the reference of the view assigned.
     *
     * @param targetView The target view to be inspected.
     */
    public void setTargetView(View targetView) {
        Log.w(TAG, "[UI] setTargetView: [" + targetView + "]");
        mTargetView = targetView;
        startTimer();
    }

    /**
     * Sets the root view (decorView) from which the view hierarchy will be inspected.
     * It logs the reference of the view assigned.
     *
     * @param decorView The decorView (root view) of the Activity or Window.
     */
    public void setDecorView(View decorView) {
        Log.w(TAG, "[UI] setDecorView: [" + decorView + "]");
        mDecorView = decorView;
    }

    /**
     * Stops the inspection timer and releases the references to the view and decorView.
     * This should be called to clean up when the inspector is no longer needed.
     */
    public void destroy() {
        stopTimer();
        mTargetView = null;
        mDecorView = null;
    }

    /**
     * Starts the traversal of the view tree to print the view hierarchy and check for overlaps.
     */
    private void startInspector(View targetView) {
        if (mTargetView != null) {
            Log.i(TAG, "[UI][INSPECTOR][TRAVERSE]: ---------- " + targetView + " ----------");
            traverseViewTree(targetView);
        }

        if (mTargetView != null && mDecorView != null) {
            Log.i(TAG, "[UI][INSPECTOR][OVERLAP]: ---------- " + mDecorView + " ----------");
            printViewHierarchyWithOverlap(mDecorView, mTargetView);
        }
    }

    /**
     * Recursively traverses a view tree, Logging information about each child view.
     * This method should be used for debugging to understand the structure of a view tree.
     *
     * @param targetView The root of the view tree to traverse.
     */
    private static void traverseViewTree(View targetView) {
        if (targetView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) targetView;
            Log.i(TAG, "[UI][TRAVERSE][VIEW_GROUP]: [" + viewGroup
                    + "], Children: [" + viewGroup.getChildCount() + "]");
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View childView = viewGroup.getChildAt(i);
                traverseViewTree(childView);
                Log.i(TAG, "[UI][TRAVERSE][CHILD]: [" + childView + "]");
            }
        } else {
            Log.i(TAG, "[UI][TRAVERSE][VIEW]: [" + targetView + "]");
        }
    }

    /**
     * Prints a single-line representation of the view hierarchy starting from the root view,
     * marking the target view and any overlapping views.
     *
     * @param rootView   The root view from which to start the traversal.
     * @param targetView The view to mark and check for overlaps.
     */
    private static void printViewHierarchyWithOverlap(View rootView, View targetView) {
        StringBuilder builder = new StringBuilder();
        Rect targetRect = new Rect();
        targetView.getGlobalVisibleRect(targetRect); // Get the global position of the target view.

        buildViewHierarchyStringWithOverlap(rootView, targetView, targetRect, 0, builder);

        // Print the built hierarchy string
        Log.i(TAG, "[UI][HIERARCHY][ " + builder + "]");
    }

    /**
     * Recursively traverses the view hierarchy, checking for overlap with the target view
     * and appending to the StringBuilder instance.
     *
     * @param rootView   The current view being inspected.
     * @param targetView The target view to mark and check for overlaps.
     * @param targetRect The global visible rectangle of the target view for overlap testing.
     * @param depth      The depth in the view hierarchy.
     * @param builder    The StringBuilder used to build the output string.
     */
    private static void buildViewHierarchyStringWithOverlap(View rootView, View targetView, Rect targetRect, int depth, StringBuilder builder) {
        builder.append("\n|");
        // Append dashes to indicate the depth of the view in the hierarchy
        for (int i = 0; i < depth; i++) {
            builder.append("-");
        }
        builder.append("[").append(depth).append("]");

        // Append visibility status of the view.
        String visibilityStatus = getVisibilityStatus(rootView);
        builder.append("[").append(visibilityStatus).append("]");

        if (rootView == targetView) {
            builder.append("[!TARGET]");
        }

        // Check for overlap and append the overlap marker if needed
        Rect viewRect = new Rect();
        rootView.getGlobalVisibleRect(viewRect);
        if (Rect.intersects(viewRect, targetRect) && rootView != targetView) {
            builder.append("[!OVERLAP]");
        }

        builder.append(rootView);

        // If the view is a ViewGroup, recursively process its children
        if (rootView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) rootView;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                buildViewHierarchyStringWithOverlap(viewGroup.getChildAt(i), targetView, targetRect, depth + 1, builder);
            }
        }
    }

    /**
     * Gets the visibility status of a view as a string.
     *
     * @param view The view whose visibility status is to be determined.
     * @return A string representing the visibility status of the view.
     */
    private static String getVisibilityStatus(View view) {
        switch (view.getVisibility()) {
            case View.VISIBLE:
                return "VISIBLE";
            case View.INVISIBLE:
                return "INVISIBLE";
            case View.GONE:
                return "GONE";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Starts a scheduled executor service with a fixed delay to periodically inspect the view hierarchy.
     * The inspection looks for overlapping views in the hierarchy and logs them.
     */
    private void startTimer() {
        stopTimer();
        mScheduledExecutorService = Executors.newScheduledThreadPool(8);
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                startInspector(mTargetView);
            }
        }, 0, SCHEDULED_EXECUTOR_SERVICE_PERIOD, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the scheduled executor service and waits for its termination.
     * If the service doesn't terminate within the specified timeout, it's shut down immediately.
     */
    private void stopTimer() {
        try {
            if (mScheduledExecutorService != null) {
                mScheduledExecutorService.shutdown();
                if (!mScheduledExecutorService.awaitTermination(1000, TimeUnit.MICROSECONDS)) {
                    mScheduledExecutorService.shutdownNow();
                }
                mScheduledExecutorService = null;
            }
        } catch (InterruptedException e) {
            if (mScheduledExecutorService != null) {
                mScheduledExecutorService.shutdownNow();
            }
            e.printStackTrace();
        }
    }
}
