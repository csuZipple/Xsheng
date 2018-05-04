package zippler.cn.xs.activity;

import android.os.Bundle;
import android.util.Log;

import zippler.cn.xs.R;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: "+TAG);//TAG is defined in base activity
    }
}
