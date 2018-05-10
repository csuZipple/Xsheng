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
import zippler.cn.xs.component.LinearProgressBar;
import zippler.cn.xs.entity.Music;
import zippler.cn.xs.holder.MusicViewHolder;

/**
 * Created by Zipple on 2018/5/6.
 * inject data by using adapter view holder
 */
public class RecyclerChooseMusicAdapter extends RecyclerView.Adapter<MusicViewHolder> {

    private List<Music> musicList;
    private Context context;
    private String TAG=this.getClass().getSimpleName();
    public RecyclerChooseMusicAdapter(Context context,List<Music> musicList) {
        this.musicList = musicList;
        this.context = context;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_choose_music_item,parent,false);
        MusicViewHolder holder = new MusicViewHolder(view);
        Log.d(TAG, "onCreateViewHolder: "+musicList.size());
        holder.getPlay().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"you clicked this play button",Toast.LENGTH_SHORT).show();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Music music = musicList.get(position);
        holder.getMusicName().setText(music.getName());
//        holder.getMusicAuthor().setText();
        holder.getMusicLength().setText(music.getLength());
//        holder.getMusicAvatar().setImageURI(Uri.parse(music.getPoster()));
        LinearProgressBar view1 = holder.getMusicProgress();
        //set progress here.
        /*int progress1 = view1.getProgress();
        view1.updatePaintColors();
        view1.setProgress(progress1);*/
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public List<Music> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
