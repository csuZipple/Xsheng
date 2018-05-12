package zippler.cn.xs.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;

import zippler.cn.xs.R;
import zippler.cn.xs.adapter.RecyclerMusicAdapter;
import zippler.cn.xs.entity.Music;
import zippler.cn.xs.util.LinerLayoutManager;
import zippler.cn.xs.util.PagingScrollHelper;

/**
 * add bgm there
 */
public class PreviewMusicActivity extends BaseActivity {

    private ImageView back;
    private TextView nextStep;
    private VideoView videoView;
    private ImageView playBtn;
    private RelativeLayout guideLayout;
    private RecyclerView musics;


    private List<Music> data;

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
        videoView = findViewById(R.id.video_m);
        playBtn = findViewById(R.id.play_btn_m);
        guideLayout = findViewById(R.id.guide_layout);
        musics = findViewById(R.id.musics);

        //判断是否是第一次进入此页面
        // SharedPreferences
    }

    private void registerListeners(){
        back.setOnClickListener(this);
        nextStep.setOnClickListener(this);
        videoView.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        guideLayout.setOnClickListener(this);
    }

    private void initDatas(){
        data = new ArrayList<>();
        //from intent..
        Music temp ;
        for (int i = 0; i < 10; i++) {
            temp = new Music();
            temp.setName("music_add_"+i);
            temp.setLength("00:0"+i);
            data.add(temp);
        }
    }

    private void initRecyclerView(){
        LinerLayoutManager linerLayoutManager = new LinerLayoutManager(this);
        linerLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        musics.setLayoutManager(linerLayoutManager);

        PagingScrollHelper helper = new PagingScrollHelper();//set scrolled horizontal
        helper.setUpRecycleView(musics);

        RecyclerMusicAdapter adapter = new RecyclerMusicAdapter(data);
        musics.setAdapter(adapter);

    }


    @Override
    public void onClick(View v) {
         switch (v.getId()){
             case R.id.guide_layout:
                 guideLayout.setVisibility(View.GONE);
                 break;
             default:
                 break;
         }
    }

    private void addBgm(){

    }



}
