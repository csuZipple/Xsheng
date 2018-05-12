package zippler.cn.xs.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import zippler.cn.xs.R;
import zippler.cn.xs.adapter.RecyclerThumbnailsAdapter;
import zippler.cn.xs.util.ImageFileUtil;

public class PreviewActivity extends BaseActivity {

    private String path;
    private long duration;

    private ImageView back;
    private TextView nextStep;
    private VideoView videoView;
    private ImageView playBtn;
    private RecyclerView thumbnails;

    private List<String> thumbs = new ArrayList<>();
    private MediaMetadataRetriever mediaMetadataRetriever;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        path = getIntent().getStringExtra("videoPath");
        initViews();
        registerListener();

        Uri uri = Uri.parse(path);
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.start();
            }
        });

        //init recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);//set horizontal
        thumbnails.setLayoutManager(linearLayoutManager);
        RecyclerThumbnailsAdapter adapter = new RecyclerThumbnailsAdapter(thumbs);
        thumbnails.setAdapter(adapter);

        //init thumbnails bitmaps;
        new Thread(new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                try {
                    initThumbnails();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "run after init Thumbnails: spend time: "+(System.currentTimeMillis()-time));
            }
        }).start();
    }


    private void initViews(){
        back = findViewById(R.id.back_preview);
        nextStep = findViewById(R.id.next_step_after_preview);
        videoView = findViewById(R.id.video);
        playBtn = findViewById(R.id.play_btn);
        thumbnails = findViewById(R.id.thumbnails);
        mediaMetadataRetriever= new MediaMetadataRetriever();
        Log.d(TAG, "initViews: path:"+path);
        mediaMetadataRetriever.setDataSource(path);
        duration = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    private void initThumbnails() {
        Log.d(TAG, "initThumbnails:  duration: "+duration);
        boolean hasFinishedBefore = false;
        //get thumbs from any fragment
        if (duration!=-1){   //milliseconds
            Bitmap bitmap;
            for (int i = 0; i < duration; i++) {
                //every 500 ms
                if (i % 500 == 0){
                    Log.d(TAG, "initThumbnails: current time is :"+i+" ms");
                    bitmap = mediaMetadataRetriever.getFrameAtTime(i*1000,MediaMetadataRetriever.OPTION_CLOSEST);//No need to be a key frame
                    try {
                        if (bitmap!=null){
                            String thumbnailsPath = saveBitmap(bitmap,path);
                            bitmap.recycle();//release memory.
                            if (thumbnailsPath.equals("exist")){
                                hasFinishedBefore = true;
                                break;
                            }else{
                                thumbs.add(thumbnailsPath);
                                PreviewActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "run: thumbs.size = "+thumbs.size());
                                        thumbnails.getAdapter().notifyItemInserted(thumbs.size()-1);
                                    }
                                });
                            }
                        }else{
                            Log.e(TAG, "initThumbnails: the bitmap at getFrameAtTime return null!" );
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            //after created thumbnails . set a flag
            if (!hasFinishedBefore){
                mkfComplete(path);
            }else{
                String root = getCamera2Path()+"cache/";
                String temporary  =root+path.substring(path.lastIndexOf("/")+1,path.lastIndexOf("."))+"/";
                thumbs.clear();
                thumbs.addAll(ImageFileUtil.getImagesInPath(temporary));
                PreviewActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: set data from sd card = "+thumbs.size());
                        thumbnails.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        }else{
            toast("error in getting thumbnails,caused by error duration.");
        }

    }


    private void registerListener(){
        nextStep.setOnClickListener(this);
        back.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        videoView.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if (event.getPointerCount()==1){
                            if (videoView.isPlaying()){
                                playBtn.setVisibility(View.VISIBLE);
                                videoView.pause();
                            }else{
                                playBtn.setVisibility(View.GONE);
                                videoView.start();
                            }
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.next_step_after_preview:
                toast("after preview");
                break;
            case R.id.back_preview:
                finish();
                break;
        }
    }

    /**
     * create bitmap cache
     * @param bitmap file
     * @throws IOException error
     * @return path
     */
    private String saveBitmap(@NonNull Bitmap bitmap, String videoPath) throws IOException{
        String root = getCamera2Path()+"cache/";
        String temporary  =root+videoPath.substring(videoPath.lastIndexOf("/")+1,videoPath.lastIndexOf("."))+"/";
        String path;

        File okFile = new File(temporary+"complete");
        if (okFile.exists()){
            Log.d(TAG, "saveBitmap: do not need get thumbnails");
            return "exist";
        }else{
            File file = new File(root);
            if (!file.exists()){
                if (file.mkdir()){
                    Log.d(TAG, "saveBitmap: cache directory created!");
                }else{
                    Log.e(TAG, "saveBitmap: cache directory created error!");
                }
            }else{
                File cache = new File(temporary);
                if (!cache.exists()){
                    if (cache.mkdir()){
                        Log.d(TAG, "saveBitmap: cache sub file directory created!");
                    }else{
                        Log.e(TAG, "saveBitmap: cache sub file directory created error!");
                    }
                }
            }

            @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            path = temporary+timeStamp+".jpg";
            File thumbs = new File(path);
            FileOutputStream out;
            try{
                out = new FileOutputStream(thumbs);
                if(bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)){
                    out.flush();
                    out.close();
                }
                Log.d(TAG, "saveBitmap: create cache image successfully.");
            } catch (IOException e){
                e.printStackTrace();
                Log.e(TAG, "saveBitmap: created cache jpeg images failed." );
            }
            return path;
        }

    }

    private void mkfComplete(String videoPath){
        String root = getCamera2Path()+"cache"+videoPath.substring(videoPath.lastIndexOf("/"),videoPath.lastIndexOf("."))+"/";
        File file = new File(root);
        if (file.exists()){
            File okFile = new File(root+"complete");
            try {
                okFile.createNewFile();
                Log.d(TAG, "mkfComplete: complete");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Log.d(TAG, "mkfComplete: null thumbnails");
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

    public static void createSavePath(String path){
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView.isPlaying()){
            playBtn.setVisibility(View.VISIBLE);
            videoView.pause();
        }else{
            playBtn.setVisibility(View.GONE);
            videoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
