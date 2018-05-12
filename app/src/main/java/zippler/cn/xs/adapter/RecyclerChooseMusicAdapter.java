package zippler.cn.xs.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import zippler.cn.xs.R;
import zippler.cn.xs.entity.Music;
import zippler.cn.xs.handler.RecordTimerRunnable;
import zippler.cn.xs.holder.MusicViewHolder;

/**
 * Created by Zipple on 2018/5/6.
 * inject data by using adapter view holder
 */
public class RecyclerChooseMusicAdapter extends RecyclerView.Adapter<MusicViewHolder> {

    private List<Music> musicList;
    private Context context;
    //timer
    private Handler handler;
    private RecordTimerRunnable runnable;

    private boolean isPause = true;

    private ProgressBar progressBar;
    private int progress = -1;
    private int time = -1;

    private int currentPos = -1;

    private static Map<String,MusicViewHolder> maps = new HashMap<>();
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
        progressBar = holder.getMusicProgress();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Music music = musicList.get(position);
        holder.getMusicName().setText(music.getName());
        holder.getMusicLength().setText(music.getLength());
        time = (musicList.get(position).getDuration())/1000;//get seconds
        MediaPlayer player =  MediaPlayer.create(this.getContext(), Uri.parse(musicList.get(position).getLocalStorageUrl()));
        holder.setPlayer(player);
        maps.put("",holder);
        currentPos = position;
        holder.getPlay().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler = new Handler();
                if (isPause){
                    startTimer(currentPos);
                    isPause = false;
                }else{
                    pauseTimer(currentPos);
                    isPause = true;
                }
            }
        });


        //set progress here.

    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }


    /**
     * started the timer
     */
    private void startTimer(int pos){
        Log.d(TAG, "startTimer: ");
        Set<String> keys=   maps.keySet();
        for (String key : keys) {
            if (key.equals(pos+"")){
                continue;
            }
            maps.get(key).getPlayer().stop();
        }

        System.out.println(maps.get(pos+"").getMusicName().getText());

        maps.get(pos+"").getPlayer().start();
        runnable = new RecordTimerRunnable(progress,time);
        runnable.setHandler(handler);
        runnable.setProgressBar(progressBar);
        handler.postDelayed(runnable,1000);
    }

    /**
     * stop the timer
     */
    private void pauseTimer(int pos){
        maps.get(pos+"").getPlayer().pause();
        progress = runnable.getProgress();//get current progress
        handler.removeCallbacks(runnable);
        runnable = null;
        Log.d(TAG, "pauseTimer: pause timer");
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
