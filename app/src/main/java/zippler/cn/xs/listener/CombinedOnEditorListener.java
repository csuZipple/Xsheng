package zippler.cn.xs.listener;

import android.util.Log;

import VideoHandle.OnEditorListener;

/**
 * Created by Zipple on 2018/5/11.
 */

public class CombinedOnEditorListener implements OnEditorListener {
    private static final String TAG = "OnEditorListener";

    @Override
    public void onSuccess() {
        Log.d(TAG, "onSuccess: combined success");
    }

    @Override
    public void onFailure() {
        Log.e(TAG, "onFailure: error in combined videos" );
    }

    @Override
    public void onProgress(float progress) {
        //这里获取处理进度
        Log.d(TAG, "onProgress: combined progress : "+progress);
    }
}
