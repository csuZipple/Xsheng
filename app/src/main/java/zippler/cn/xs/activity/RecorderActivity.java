package zippler.cn.xs.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import zippler.cn.xs.R;

public class RecorderActivity extends BaseActivity implements TextureView.SurfaceTextureListener{

    //views
    private TextureView preview;
    private ImageView back;
    private ImageView exposure;
    private ImageView reverse;
    private ImageView recordBtn;
    private TextView nextStep;

    private SurfaceTexture surface;

    //about record videos
    private Camera camera;
    private Camera.Parameters parameters;
    private MediaPlayer music;
    private MediaRecorder mediaRecorder;
    private String savedVideoPath;
    private int backCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int frontCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean isBackCameraOn ;
    private boolean isRecordOn = false;

    //listener


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);


        initViews();
        registerListeners();
    }

    private void initViews(){
        preview = findViewById(R.id.preview);
        back = findViewById(R.id.back);
        exposure = findViewById(R.id.exposure);
        reverse = findViewById(R.id.camera_id);
        recordBtn = findViewById(R.id.record_btn);
        nextStep = findViewById(R.id.next_step);
    }

    private void registerListeners(){
        preview.setSurfaceTextureListener(this);
        back.setOnClickListener(this);
        exposure.setOnClickListener(this);
        reverse.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
        nextStep.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                break;
            case R.id.exposure:
                break;
            case R.id.camera_id:
                break;
            case R.id.record_btn:
                startRecord();
                break;
            case R.id.next_step:
                break;
            default:
                Log.d(TAG, "onClick: default clicked");
                break;
        }
    }

    /**
     *  about life cycle
     *  open camera and release resource.
     */

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * About when to open camera
     */

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.surface = surface;
        openCamera(backCamera);
    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        this.surface = surface;
        openCamera(backCamera);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        //release resources
        camera.stopPreview();
        camera.release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    /**
     * about how to use camera
     */


    /**
     * open camera
     * @param position which camera
     */
    private void openCamera(int position) {
        isBackCameraOn = (position == Camera.CameraInfo.CAMERA_FACING_BACK);

         camera = Camera.open(position);
         parameters = camera.getParameters();

         //set auto focused , There should be a clicking focus feature here
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        parameters.setPreviewSize(1920,1080);//choose the right size of phone.

        camera.setParameters(parameters);
        camera.setDisplayOrientation(90);
        try {
            camera.setPreviewTexture(surface);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        Log.d(TAG, "openCamera: start preview");
    }

    private void startRecord(){
        if (!isRecordOn){
            isRecordOn = true;
            playMusic(R.raw.di);
            record();
        }else{
            isRecordOn = false;
            playMusic(R.raw.po);
            stop();

            Intent intent = new Intent(this,PreviewActivity.class);
            intent.putExtra("videoPath",savedVideoPath);
            startActivity(intent);
        }
    }


    private void record() {
        mediaRecorder = new MediaRecorder();
        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        mediaRecorder.setVideoSize(1920, 1080);
        mediaRecorder.setVideoEncodingBitRate(5*1024*1024);
        mediaRecorder.setVideoFrameRate(60);

        if (isBackCameraOn){
            mediaRecorder.setOrientationHint(90);
        }else{
            mediaRecorder.setOrientationHint(270);
        }

        //set record lengths here.


        //save video
        String root = getCamera2Path();
        createSavePath(root);//判断有没有这个文件夹，没有的话需要创建
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        savedVideoPath = root + "形声_" + timeStamp + ".mp4";
        mediaRecorder.setOutputFile(savedVideoPath);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();

    }


    private void stop() {
        camera.lock();
        mediaRecorder.stop();
        mediaRecorder.release();
    }
    /**
     * about utils function
     */

    private void playMusic(int musicId) {
        music = MediaPlayer.create(this, musicId);
        music.start();
        music.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                music.release();
            }
        });
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

}
