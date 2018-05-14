package zippler.cn.xs.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.List;

import zippler.cn.xs.R;
import zippler.cn.xs.entity.Video;
import zippler.cn.xs.holder.VideoViewHolder;
import zippler.cn.xs.util.ImageFileUtil;


/**
 * Created by Zipple on 2018/5/5.
 * Adapter for inject data
 * Set item view and layout
 */
public class RecyclerVideoAdapter extends RecyclerView.Adapter<VideoViewHolder>  {

    private Context context;
    private List<Video> videoList;
    private String TAG=this.getClass().getSimpleName();

    public RecyclerVideoAdapter(Context context, List<Video> videoList) {
        this.context = context;
        this.videoList = videoList;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // add child layout in recycler view
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_recycler_video_item,parent,false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        //change child attribute here.
        final Video video = videoList.get(position);
        holder.getName().setText(video.getDesc());
        holder.getTime().setText(video.getDeployed().toString());
        holder.getLength().setText(video.getLength());
        final ImageView poster = holder.getPoster();
        final VideoView videoView = holder.getVideoview();
        final ImageView playBtn = holder.getPlay();

        String localUrl = video.getLocalStorageUrl();
        String url = video.getUrl();
        if (url!=null){
            videoView.setVideoPath(url);
            ImageFileUtil.setFirstFrame(poster,url);//time waste...
        }else{
            if (localUrl!=null){
                videoView.setVideoURI(Uri.parse(localUrl));
                ImageFileUtil.setFirstFrame(poster,context,Uri.parse(localUrl));//time waste...
            }
        }

        //add holder listener here. for example
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playBtn.setVisibility(View.INVISIBLE);
                videoView.start();
            }
        });
        poster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playBtn.setVisibility(View.INVISIBLE);
                poster.setVisibility(View.GONE);
                videoView.start();
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e(TAG, "onError: some error occurs in playing video in the video fragment");
                Toast.makeText(context, "当前视频无法播放", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: video on touch");
                if (event.getAction()==MotionEvent.ACTION_DOWN){
                    if (videoView.isPlaying()){
                        playBtn.setVisibility(View.VISIBLE);
                        videoView.pause();
                    }else{
                        playBtn.setVisibility(View.INVISIBLE);
                        poster.setVisibility(View.GONE);
                        videoView.start();
                    }
                }
                return true;
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.start();
            }
        });

    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }
}
