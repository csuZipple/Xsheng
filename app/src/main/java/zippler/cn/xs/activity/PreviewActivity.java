package zippler.cn.xs.activity;

import android.os.Bundle;
import android.widget.TextView;

import zippler.cn.xs.R;

public class PreviewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        TextView path = findViewById(R.id.path);
        path.setText(getIntent().getStringExtra("videoPath"));
    }
}
