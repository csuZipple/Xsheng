package zippler.cn.xs.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import zippler.cn.xs.R;
import zippler.cn.xs.entity.Video;
import zippler.cn.xs.holder.VideoViewHolder;


/**
 * Created by Zipple on 2018/5/5.
 * Adapter for inject data
 * Set item view and layout
 */
public class RecyclerVideoAdapter extends RecyclerView.Adapter<VideoViewHolder> {

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
        VideoViewHolder holder = new VideoViewHolder(view);
        //add holder listener here. for example
        holder.getPlay().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"you clicked this play button",Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        //change child attribute here.
         Video video = videoList.get(position);
         if (video!=null){
//             holder.getName().setText(video.getName());
             holder.getTime().setText(video.getDeployed().toString());
             holder.getLength().setText(video.getLength());
         }else{
             //keep default settings
             Log.d(TAG, "onBindViewHolder: keep default settings.");
         }

    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }
}
