package zippler.cn.xs.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import zippler.cn.xs.R;
import zippler.cn.xs.component.CommentPopView;
import zippler.cn.xs.entity.Comment;
import zippler.cn.xs.entity.Video;
import zippler.cn.xs.holder.VideoViewHolder;
import zippler.cn.xs.util.LinerLayoutManager;
import zippler.cn.xs.util.PxUtil;


/**
 * Created by Zipple on 2018/5/5.
 * Adapter for inject data
 * Set item view and layout
 */
public class RecyclerVideoAdapter extends RecyclerView.Adapter<VideoViewHolder>  {

    private LinerLayoutManager linearLayout;
    private Context context;
    private List<Video> videoList;
    private String TAG=this.getClass().getSimpleName();

    boolean isClicked = false;


    public RecyclerVideoAdapter(Context context, List<Video> videoList, LinerLayoutManager linerLayoutManager) {
        this.context = context;
        this.videoList = videoList;
        this.linearLayout = linerLayoutManager;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // add child layout in recycler view
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_recycler_video_item,parent,false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VideoViewHolder holder, int position) {
        //change child attribute here.
        Log.e(TAG, "onBindViewHolder: bind video " );
        final Video video = videoList.get(position);
        holder.getName().setText(video.getDesc());
        holder.getTime().setText(video.getDeployed());
        holder.getLength().setText(video.getLength());
        final ImageView poster = holder.getPoster();
        final VideoView videoView = holder.getVideoview();
        final ImageView playBtn = holder.getPlay();
        final RelativeLayout root = holder.getVideoRoot();
        final ImageView love = holder.getLove();

        final String url = videoList.get(position).getUrl()==null?videoList.get(position).getLocalStorageUrl():videoList.get(position).getUrl();

        holder.getComment().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    showComment(root);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        });

        holder.getRedeploy().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent textIntent = new Intent(Intent.ACTION_SEND);
                textIntent.setType("text/plain");
                View view = linearLayout.findViewByPosition(holder.getAdapterPosition());
                TextView title = view.findViewById(R.id.video_name);
                textIntent.putExtra(Intent.EXTRA_TEXT, title.getText()+"\n视频:"+url+"\n\n来自形声. \n形声，以形作声.");
                context.startActivity(Intent.createChooser(textIntent, "来自形声的分享"));
            }
        });

        love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isClicked){
                    love.setImageResource(R.mipmap.love_white);
                }else{
                    love.setImageResource(R.mipmap.love_red);
                    toastView("收藏成功",R.mipmap.correct);
                }
            }
        });

        //add holder listener here. for example
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playBtn.setVisibility(View.INVISIBLE);
                poster.setVisibility(View.INVISIBLE);
                videoView.start();
            }
        });
        poster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playBtn.setVisibility(View.INVISIBLE);
                poster.setVisibility(View.INVISIBLE);
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
        //how to solve the black ground problem..?

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: video on touch");
                if (event.getPointerCount()==1&&event.getAction()==MotionEvent.ACTION_UP){
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
    public void onViewAttachedToWindow(@NonNull VideoViewHolder holder) {
        Log.e(TAG, "onViewAttachedToWindow: ");
        final Video video = videoList.get(holder.getAdapterPosition());
        final ImageView poster = holder.getPoster();
        final VideoView videoView = holder.getVideoview();
        final ImageView playBtn = holder.getPlay();

        poster.setVisibility(View.VISIBLE);
        String localUrl = video.getLocalStorageUrl();
        String url = video.getUrl();
        if (url!=null){
            videoView.setVideoURI(Uri.parse(url));//IOException??
//            Glide.with(context).load(new File(url)).thumbnail(1.0f).into(poster);
            Glide.with(context).load(url).thumbnail(1.0f).error( R.drawable.n ).into(poster);//glide行不通
        }else{
            if (localUrl!=null){
                videoView.setVideoURI(Uri.parse(localUrl));
                playBtn.setVisibility(View.VISIBLE);
                Glide.with(context).load(Uri.parse(localUrl)).thumbnail(1.0f).into(poster);
            }
        }
    }
    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public void showComment(View v) throws CloneNotSupportedException {

        List<Comment> comments = new ArrayList<>();

        Comment comment = new Comment();
        comment.setContent("我听见脚步声，意料的软皮鞋跟！");
        comment.setTime("1小时前");
        comment.setName("zipple");
        comment.setPic(R.drawable.avatar);

        comments.add(comment);
        comment = new Comment();
        comment.setContent("哥练的胸肌，你要不要靠！");
        comment.setTime("2小时前");
        comment.setName("icelee");
        comment.setPic(R.drawable.pikachu);
        comments.add(comment);


        CommentPopView commentPopView = new CommentPopView(context,comments);
        commentPopView.showAtLocation(v, Gravity.BOTTOM,0,0);
    }

    protected void toastView(String msg,int drawable){
        Toast customToast = new Toast(context.getApplicationContext());
        View customView = LayoutInflater.from(context).inflate(R.layout.toast_img,null);

        LinearLayout relativeLayout = customView.findViewById(R.id.toast_linear);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) PxUtil.dpToPx(context, 130), (int)PxUtil.dpToPx(context, 130));
        relativeLayout.setLayoutParams(layoutParams);


        ImageView img = customView.findViewById(R.id.img_correct);
        TextView tv = customView.findViewById(R.id.tips_right);
        img.setBackgroundResource(drawable);
        tv.setText(msg);
        customToast.setView(customView);
        customToast.setDuration(Toast.LENGTH_SHORT);
        customToast.setGravity(Gravity.CENTER,0,0);
        customToast.show();
    }

    private Bitmap createVideoThumbnail(String url, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int kind = MediaStore.Video.Thumbnails.MINI_KIND;
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(url, new HashMap<String, String>());
            } else {
                retriever.setDataSource(url);
            }
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }
}
