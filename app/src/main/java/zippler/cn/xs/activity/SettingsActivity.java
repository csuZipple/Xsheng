package zippler.cn.xs.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import zippler.cn.xs.R;
import zippler.cn.xs.util.FileUtil;

public class SettingsActivity extends BaseActivity {
    private RelativeLayout clearCache;
    private TextView cacheSize;
    private List<String> videoCache;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();

        registerListeners();
    }

    private void initViews(){
        clearCache = findViewById(R.id.clear_cache);
        cacheSize = findViewById(R.id.cache_size);
        cacheSize.setText(getCacheSize()+"MB");
    }


    private void registerListeners() {
        clearCache.setOnClickListener(this);
    }

    private String getCacheSize(){
        //ergodic videoCache.xsheng.
        String result;
        videoCache = FileUtil.traverseFolder(FileUtil.getCamera2Path()+"videoCache"+ File.separator);
        File file = new File(FileUtil.getCamera2Path());
        File[] files = file.listFiles();
        if (videoCache==null){
            videoCache = new ArrayList<>();
        }
        if (files.length != 0) {
            for (File file2 : files) {
                if (!file2.isDirectory()) {
                    if (FileUtil.isVedioFile(file2.getAbsolutePath())){
                        videoCache.add(file2.getAbsolutePath());
                    }
                }
            }
        }
        long size=0;
        for (String path:videoCache) {
            size+=new File(path).length();
        }
        result = (float)size/1024/1024+"";
        return result;
    }

    private void clearCache(){
        if (videoCache!=null&&videoCache.size()>0){
            for (String path:videoCache) {
                File temp = new File(path);
                if (temp.exists()){
                    temp.delete();
                }
            }
        }
        cacheSize.setText("0.0MB");
        toast("已成功清理缓存");
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.clear_cache:
                clearCache();
                break;

            default:
                Log.d(TAG, "onClick: default clicked");
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.acticity_open_anim,R.anim.acticity_close_anim);
    }
}
