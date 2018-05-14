package zippler.cn.xs.fragment;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import zippler.cn.xs.R;
import zippler.cn.xs.adapter.RecyclerVideoAdapter;
import zippler.cn.xs.entity.Video;
import zippler.cn.xs.listener.SwipedRefreshListener;
import zippler.cn.xs.util.ImageFileUtil;
import zippler.cn.xs.util.LinerLayoutManager;
import zippler.cn.xs.util.PagingScrollHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;



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
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                boolean isDown = dy<0;
                LinearLayoutManager llm = (LinearLayoutManager)recyclerView.getLayoutManager();
                int nowPos = llm.findFirstVisibleItemPosition();
                if (isDown){
                    nowPos--;
                }else{
                    nowPos++;
                }
                //set the last view default settings
                if (nowPos<0) nowPos=1;
                View lastView = llm.findViewByPosition(nowPos);
                if (lastView!=null){
                    VideoView videoView = lastView.findViewById(R.id.video_show);
                    ImageView poster = lastView.findViewById(R.id.poster);
                    ImageView playBtn = lastView.findViewById(R.id.play);
                    if (videoView.isPlaying()){
                        videoView.stopPlayback();//or pause?
                    }
                    if (poster.getVisibility()==View.INVISIBLE){
                        playBtn.setVisibility(View.VISIBLE);
                        poster.setVisibility(View.VISIBLE);
                    }
                }

            }
        });

        initVideo();
        RecyclerVideoAdapter videoAdapter = new RecyclerVideoAdapter(getContext(),videos);
        recyclerView.setAdapter(videoAdapter);

        initSwipedLayout(view);
        return view;
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
        for (int i = 0; i < 10; i++) {
            temp = new Video();
            temp.setDesc("我还是很喜欢你，像风走了八百里.");

            String path = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.v1;
            temp.setLocalStorageUrl(path);
            temp.setLength((int) ImageFileUtil.getDuration(getContext(), Uri.parse(path)));
            temp.setDeployed(new Timestamp(System.currentTimeMillis()));//set current time to test
            videos.add(temp);
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

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<Video> videos) {
        this.videos = videos;
    }

    public Video getDeployedVideo() {
        return deployedVideo;
    }

    public void setDeployedVideo(Video deployedVideo) {
        this.deployedVideo = deployedVideo;
    }
}
