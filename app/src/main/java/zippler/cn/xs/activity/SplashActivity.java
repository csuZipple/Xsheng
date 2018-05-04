package zippler.cn.xs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import zippler.cn.xs.R;

/**
 * This activity is used for splash screen
 * to show some information about the xs app
 */
public class SplashActivity extends BaseActivity {

    private static final int DELAY = 2000;//delay 2 seconds
    private Handler x;
    private SplashHandler splashHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        showSplash();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(x!=null&&splashHandler!=null){
            x.removeCallbacks(splashHandler);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(x!=null&&splashHandler!=null){
            x.postDelayed(splashHandler, DELAY);
        }
    }

    /**
     * It is used to jump pages
     */
    class SplashHandler implements Runnable{
        @Override
        public void run() {
            //Here we should determine whether the user is the first time to enter the program
            //GuideActivity
            Intent intent = new Intent(SplashActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void showSplash(){
          x = new Handler();
          splashHandler = new SplashHandler();
    }

}
