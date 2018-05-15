package zippler.cn.xs.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import VideoHandle.OnEditorListener;
import zippler.cn.xs.R;
import zippler.cn.xs.adapter.RecyclerThumbnailsAdapter;
import zippler.cn.xs.entity.Music;
import zippler.cn.xs.listener.CombinedMusicEditorListener;
import zippler.cn.xs.util.FFmpegEditor;
import zippler.cn.xs.util.FileUtil;
import zippler.cn.xs.util.ImageFileUtil;

public class PreviewActivity extends BaseActivity {

    private String path;
    private long duration;
    private ArrayList<String> videoPaths = new ArrayList<>();
    private String output;

    private ImageView back;
    private TextView nextStep;
    private VideoView videoView;
    private ImageView playBtn;
    private RecyclerView thumbnails;
    private ImageView deleteBtn;
    private ImageView loading;

    private List<String> thumbs = new ArrayList<>();
    private MediaMetadataRetriever mediaMetadataRetriever;

    private CombinedMusicEditorListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        path = getIntent().getStringExtra("videoPath");
        Log.d(TAG, "onCreate: path :"+path);
        initViews();
        registerListener();

        videoView.setVideoPath(path);
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

//        initRecycler();

    }

    private void initRecycler(){
        mediaMetadataRetriever= new MediaMetadataRetriever();
        Log.d(TAG, "initViews: path:"+path);
        mediaMetadataRetriever.setDataSource(path);
        duration = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
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
//        thumbnails = findViewById(R.id.thumbnails); // 留待有缘人来实现视频截取功能吧...目前的代码存在内存溢出问题，暂时不做了
        deleteBtn = findViewById(R.id.delete_btn);
        loading = findViewById(R.id.loading);
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
                String root = FileUtil.getCamera2Path()+"cache/";
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
        deleteBtn.setOnClickListener(this);


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

    private void removeListeners(){
        nextStep.setOnClickListener(null);
        back.setOnClickListener(null);
        playBtn.setOnClickListener(null);
        deleteBtn.setOnClickListener(null);

        videoView.setOnTouchListener(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.next_step_after_preview:
                upload();
                break;
            case R.id.back_preview:
                finish();
                break;
            case R.id.delete_btn:
                deleteCurrentVideo();
                Intent intent = new Intent(this,RecorderActivity.class);
                startActivity(intent);
                finish();
            default:
                toast("default clicked");
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
        String root = FileUtil.getCamera2Path()+"cache/";
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
        String root = FileUtil.getCamera2Path()+"cache"+videoPath.substring(videoPath.lastIndexOf("/"),videoPath.lastIndexOf("."))+"/";
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

    /**
     * Upload to backstage and processing.
     */
    private void upload(){

        try {
//          uploadByPost("");

            //视频过滤

            List<Music> musics = depositMp3();
            Log.d(TAG, "upload: music size = "+musics.size());

            //how to load a loading animation

            loading.setVisibility(View.VISIBLE);
            videoView.pause();

            Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.loading);
//            LinearInterpolator lin = new LinearInterpolator();
            operatingAnim.setInterpolator(new BounceInterpolator());
            loading.startAnimation(operatingAnim);


            removeListeners();

            attachBgm(musics);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * upload video
     * @throws IOException error
     * @return return the output
     */
    private StringBuilder uploadByPost(String net) throws IOException {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        URL url = new URL(net);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
        // 允许输入输出流
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setUseCaches(false);
        // 使用POST方法
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
        httpURLConnection.setRequestProperty("Charset", "UTF-8");
        httpURLConnection.setRequestProperty("Content-Type",
                "multipart/form-data;boundary=" + boundary);

        DataOutputStream dos = new DataOutputStream(
                httpURLConnection.getOutputStream());
        dos.writeBytes(twoHyphens + boundary + end);
        dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""
                + path.substring(path.lastIndexOf("/") + 1) + "\"" + end);
        dos.writeBytes(end);

        FileInputStream fis = new FileInputStream(path);
        byte[] buffer = new byte[8192]; // 8k
        int count = 0;
        // 读取文件
        while ((count = fis.read(buffer)) != -1) {
            dos.write(buffer, 0, count);
        }
        fis.close();
        dos.writeBytes(end);
        dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
        dos.flush();

        //how to get json data..
        InputStream is = httpURLConnection.getInputStream();
        InputStreamReader isr = new InputStreamReader(is, "utf-8");
        BufferedReader br = new BufferedReader(isr);
        StringBuilder result = new StringBuilder();
        String temp;
        while((temp = br.readLine())!=null){
            result.append(temp);
        }
        Log.d(TAG, "uploadFile: "+result);
        dos.close();
        is.close();
        return result;
    }

    private List<Music> depositMp3(){
        String cache = "mp3"+File.separator;
        FileUtil.createSavePath(FileUtil.getCamera2Path()+cache);

        List<Music> musics = new ArrayList<>();

        //deposit here...
        Music temp = new Music();
        temp.setLocalStorageUrl(FileUtil.getCamera2Path()+"test.mp3");
        temp.setName("Tank");
        musics.add(temp);

        temp = new Music();
        temp.setLocalStorageUrl(FileUtil.getCamera2Path()+"j.mp3");
        temp.setName("Hora");
        musics.add(temp);

        return musics;
    }


    private  void attachBgm(final List<Music> musics){
        if(musics==null || musics.size()==0) {
            forwardPages();
            return;
        }
        Music temp = musics.remove(0);
        String cache = "videoCache"+File.separator;
        FileUtil.createSavePath(FileUtil.getCamera2Path()+cache);
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        output= FileUtil.getCamera2Path()+cache+"py_"+timeStamp+".mp4";
        FFmpegEditor.music(path, temp.getLocalStorageUrl(), output, 0.3f, 1.0f, new OnEditorListener() {
            @Override
            public void onSuccess() {
                attachBgm(musics);
            }

            @Override
            public void onFailure() {

            }

            @Override
            public void onProgress(float v) {

            }
        });
        videoPaths.add(output);
    }

    private void forwardPages(){
        Intent intent = new Intent(this,PreviewMusicActivity.class);
        intent.putStringArrayListExtra("videoPaths",  videoPaths);
        startActivity(intent);
    }

    private void deleteCurrentVideo(){
        File file = new File(path);
        if (file.exists()){
            if (file.delete()){
                Log.d(TAG, "deleteCurrentVideo: delete current videos successfully");
            }else{
                Log.e(TAG, "deleteCurrentVideo: error in delete current video" );
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView.isPlaying()){
            loading.setVisibility(View.INVISIBLE);
            playBtn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
