package zippler.cn.xs.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;

import zippler.cn.xs.R;
import zippler.cn.xs.adapter.RecyclerMusicAdapter;
import zippler.cn.xs.listener.OnPageChangedListener;
import zippler.cn.xs.util.LinerLayoutManager;
import zippler.cn.xs.util.PagingScrollHelper;

/**
 * add bgm there
 */
public class PreviewMusicActivity extends BaseActivity {

    private ImageView back;
    private TextView nextStep;
    private VideoView video;
    private ImageView playBtn;
    private RelativeLayout guideLayout;
    private RecyclerView musics;


    private ArrayList<String> data;
    private int currentPosition = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_music);

        initViews();
        registerListeners();
        initDatas();
        initRecyclerView();
    }

    private void initViews(){
        back = findViewById(R.id.back_preview_m);
        nextStep = findViewById(R.id.next_step_after_preview_m);
        video = findViewById(R.id.video_m);
        playBtn = findViewById(R.id.play_btn_m);
        guideLayout = findViewById(R.id.guide_layout);
        musics = findViewById(R.id.musics);

        if (isFirstIn()){
            guideLayout.setVisibility(View.GONE);
        }

        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                video.start();
            }
        });
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                video.start();
            }
        });
    }

    private void registerListeners(){
        back.setOnClickListener(this);
        nextStep.setOnClickListener(this);
        video.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        guideLayout.setOnClickListener(this);
    }

    private void initDatas(){
        //get intent
        data = getIntent().getStringArrayListExtra("videoPaths");
        Log.d(TAG, "initDatas: data size = "+data.size());
    }

    private void initRecyclerView(){
        LinerLayoutManager linerLayoutManager = new LinerLayoutManager(this);
        linerLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        musics.setLayoutManager(linerLayoutManager);

        PagingScrollHelper helper = new PagingScrollHelper();//set scrolled horizontal
        helper.setUpRecycleView(musics);
        //set page changed listener
        helper.setOnPageChangedListener(new OnPageChangedListener() {
            @Override
            public void onChanged(int position) {
                Log.d(TAG, "onChanged: page changed!!!!  "+position);
                 //change video here
                if (position<data.size()&&position>=0){
                    video.setVideoPath(data.get(position));
                    currentPosition = position;
                }
            }
        });

        RecyclerMusicAdapter adapter = new RecyclerMusicAdapter(data);
        musics.setAdapter(adapter);

        if (guideLayout.getVisibility()==View.GONE){
           //play the first one
            if (data.size()>0){
                video.setVideoPath(data.get(0));
            }
        }
    }

    private void putSharedData(){
        SharedPreferences sp = getSharedPreferences("firstIn", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("isFirstIn", "false");
        editor.commit();
    }

    private String getSharedData(){
        SharedPreferences sp = getSharedPreferences("firstIn", Context.MODE_PRIVATE);
        return sp.getString("isFirstIn","true");
    }

    private boolean isFirstIn(){
        if (getSharedData().equals("false")){
            return false;
        }else{
            putSharedData();
            return false;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.guide_layout:
                guideLayout.setVisibility(View.GONE);
                //play the video
                if (data.size()>0){
                    video.setVideoPath(data.get(0));
                }
                break;
            case R.id.next_step_after_preview_m:
                gotoDeploy();
                break;
            default:
                break;
        }
    }

    private void gotoDeploy(){
        Intent intent = new Intent(this,DeployActivity.class);
        intent.putExtra("videoPath",data.get(currentPosition));
        startActivity(intent);
    }




    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //pause the video
        if (video!=null){
            video.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //restart
        if (video!=null){
            video.start();
        }
    }

    public static String getCamera2Path() {
        String picturePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/xsheng/";
        File file = new File(picturePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return picturePath;
    }
}
