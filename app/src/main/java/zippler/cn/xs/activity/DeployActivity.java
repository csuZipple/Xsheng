package zippler.cn.xs.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.sql.Timestamp;

import zippler.cn.xs.R;
import zippler.cn.xs.entity.Video;

public class DeployActivity extends BaseActivity {

    private String path;
    private Button deployBtn;
    private EditText desc ;
    private ImageView poster;
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

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        Bitmap bitmap = retriever.getFrameAtTime(0);
        poster.setImageBitmap(bitmap);//Is there a more efficient way？
        duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

    }

    private void registerListeners(){
        deployBtn.setOnClickListener(this);
        poster.setOnClickListener(this);
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
            default:
                break;
        }
    }

    private void preview(){
//        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, poster, "videoImg");
        Intent intent = new Intent(this, PreviewFullVideoActivity.class);
        intent.putExtra("videoPath",path);
//        startActivity(intent,options.toBundle());
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
                    Thread.sleep(3000);
                    Intent intent = new Intent(DeployActivity.this,MainActivity.class);
                    Video video = new Video();
                    video.setName("default username..");
                    video.setDesc(desc.getText().toString());
                    video.setDeployed(new Timestamp(System.currentTimeMillis()));
                    video.setLength((int) duration);
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
