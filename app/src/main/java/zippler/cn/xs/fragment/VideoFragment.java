package zippler.cn.xs.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import zippler.cn.xs.R;
import zippler.cn.xs.adapter.RecyclerVideoAdapter;
import zippler.cn.xs.entity.Video;
import zippler.cn.xs.listener.RecyclerScrollListener;
import zippler.cn.xs.listener.SwipedRefreshListener;
import zippler.cn.xs.util.LinerLayoutManager;
import zippler.cn.xs.util.SpaceItemDecoration;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    //the data resource
    private List<Video> videos;

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
        recyclerView.addItemDecoration(new SpaceItemDecoration(0,8));//it works well after I modified some code in this class
//        recyclerView.addItemDecoration(new RemoveLastLineDividerItemDecoration(this.getContext(),DividerItemDecoration.VERTICAL));//why Fragment context might be null?
        //inject data to recycler view == video adapter
        initVideo();
        RecyclerVideoAdapter videoAdapter = new RecyclerVideoAdapter(getContext(),videos);
        recyclerView.setAdapter(videoAdapter);

        //recycler view scroll listener
        recyclerView.addOnScrollListener(new RecyclerScrollListener(linerLayoutManager));

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
//            temp.setName("video"+i);
            temp.setLength("00:0"+i);
            temp.setDeployed(new Timestamp(System.currentTimeMillis()));//set current time to test
            videos.add(temp);
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

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }
}
