package zippler.cn.xs.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.sql.Timestamp;

import zippler.cn.xs.R;
import zippler.cn.xs.entity.Video;
import zippler.cn.xs.util.ImageFileUtil;

public class DeployActivity extends BaseActivity {

    private String path;
    private Button deployBtn;
    private EditText desc ;
    private ImageView poster;
    private ImageView backBtn;
    private long duration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deploy);

        initViews();
        registerListeners();

    }

    private void initViews(){
        deployBtn = findViewById(R.id.deploy_btn);
        path = getIntent().getStringExtra("videoPath");
        desc = findViewById(R.id.video_desc_text);
        poster = findViewById(R.id.preview_video_img);
        backBtn = findViewById(R.id.back_button);

        ImageFileUtil.setFirstFrame(poster,path);
        duration = ImageFileUtil.getDuration(path);

    }

    private void registerListeners(){
        deployBtn.setOnClickListener(this);
        poster.setOnClickListener(this);
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.deploy_btn:
                //should upload to internet..
                gotoMainActivity();
                break;
            case R.id.preview_video_img:
                preview();
                break;
            case R.id.back_button:
                finish();
                break;
            default:
                break;
        }
    }

    private void preview(){
        Intent intent = new Intent(this, PreviewFullVideoActivity.class);
        intent.putExtra("videoPath",path);
        startActivity(intent);
    }

    //upload the videos...

    private void gotoMainActivity(){
        Log.d(TAG, "gotoMainActivity: deployed to main activity");
        final ProgressDialog dialog = ProgressDialog.show(this,
                "正在发布", "请稍后....", true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    //test for deployed

                    Intent intent = new Intent(DeployActivity.this,MainActivity.class);
                    Video video = new Video();
                    video.setName("default username..");
                    video.setDesc(desc.getText().toString());
                    video.setDeployed(new Timestamp(System.currentTimeMillis()));
                    video.setLength((int) duration);
                    video.setUrl(path);
                    intent.putExtra("video",video);
                    startActivity(intent);
                    dialog.dismiss();
                    finish();
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
