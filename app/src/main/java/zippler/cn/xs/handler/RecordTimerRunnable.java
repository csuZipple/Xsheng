package zippler.cn.xs.handler;

import android.util.Log;
import android.widget.ProgressBar;

import android.os.Handler;

/**
 * Created by Zipple on 2018/5/11.
 */

public class RecordTimerRunnable implements Runnable {

    private ProgressBar progressBar;
    private Handler handler;
    private int progress;
    private int time;
    private static final String TAG = "RecordTimerRunnable";

    public RecordTimerRunnable(int progress, int time) {
        this.progress = progress;
        this.time = time;
    }

    @Override
    public void run() {
        time--;
        if (time>0){
            progress++;
            Log.d(TAG, "run: record timer running "+progress);
            progressBar.setProgress(progress);
            handler.postDelayed(this,1000);
        }else{
            //stop
        }
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }


    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }
}
