package zippler.cn.xs.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import zippler.cn.xs.R;
import zippler.cn.xs.adapter.RecyclerVideoAdapter;
import zippler.cn.xs.entity.Video;
import zippler.cn.xs.listener.OnPageChangedListener;
import zippler.cn.xs.listener.SwipedRefreshListener;
import zippler.cn.xs.util.FileUtil;
import zippler.cn.xs.util.ImageFileUtil;
import zippler.cn.xs.util.LinerLayoutManager;
import zippler.cn.xs.util.PagingScrollHelper;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private String[] defaultTitle ={"阳光正好，微风不燥",
            " 不是无情，亦非薄幸",
            "再多的语言与眼泪，也无法让另一个人知道你的悲伤",
            "这是一个流行离开的世界，但是我们都不擅长告别",
            "有些人看起来毫不在乎你，其实你不知道他忍住了多少次想要联系你的冲动",
            " 她的头发梳得很整齐，像一顶光亮的大帽子",
            "有人牵挂的漂泊不叫流浪",
            "但行眼前事，莫愁眼前人"};



    //the data resource
    private ArrayList<Video> videos;
    private Video deployedVideo;//recent videos from deploy activity

    public VideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_video, container, false);
        recyclerView = view.findViewById(R.id.recycle_view);
        LinerLayoutManager linerLayoutManager = new LinerLayoutManager(this.getContext());
        recyclerView.setLayoutManager(linerLayoutManager);

        PagingScrollHelper helper = new PagingScrollHelper();
        helper.setUpRecycleView(recyclerView);
        helper.setOnPageChangedListener(new OnPageChangedListener() {
            @Override
            public void onChanged(int position) {
                Log.e(TAG, "onChanged: current page is "+position);
            }
        });

        initVideo();
        RecyclerVideoAdapter videoAdapter = new RecyclerVideoAdapter(getContext(),videos);
        recyclerView.setAdapter(videoAdapter);

        initSwipedLayout(view);
        return view;
    }

    private void hide(View lastView){
        if (lastView!=null){
            VideoView videoView = lastView.findViewById(R.id.video_show);
            ImageView poster = lastView.findViewById(R.id.poster);
            ImageView playBtn = lastView.findViewById(R.id.play);
            if (videoView.isPlaying()){
                videoView.stopPlayback();//or pause?
            }
            Log.e(TAG, "hide: poster :"+poster.getVisibility()+"");

            playBtn.setVisibility(View.VISIBLE);
            poster.setVisibility(View.VISIBLE);
        }
    }


    /**
     * init refresh layout
     * @param view Fragment view
     */
    private void initSwipedLayout(View view){
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorDark));
        swipeRefreshLayout.setOnRefreshListener(new SwipedRefreshListener(swipeRefreshLayout,getContext()));
    }

    /**
     * init video resource
     */
    private void initVideo(){
        videos = new ArrayList<>();
        Video temp ;
        String basePath = FileUtil.getCamera2Path()+"deploy"+ File.separator;
        List<String> pathList = FileUtil.traverseFolder(basePath);
        if (pathList != null){
            Log.d(TAG, "initVideo: get local videos size = "+pathList.size());
            for (String url:pathList) {
                temp = new Video();
                temp.setUrl(url);
                temp.setDesc(defaultTitle[(int) (Math.random()*8)]);
                temp.setLength((int) ImageFileUtil.getDuration(url));
                temp.setDeployed(FileUtil.getLastModifiedTime(url));
                videos.add(temp);
            }
        }else{
            Log.d(TAG, "initVideo: no deployed video here..");
        }


        if (deployedVideo!=null){
            videos.add(0,deployedVideo);
        }
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }


    public void setDeployedVideo(Video deployedVideo) {
        this.deployedVideo = deployedVideo;
    }
}
