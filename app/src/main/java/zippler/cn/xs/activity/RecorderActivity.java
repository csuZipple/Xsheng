package zippler.cn.xs.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import zippler.cn.xs.R;

public class RecorderActivity extends BaseActivity implements TextureView.SurfaceTextureListener{

    //views
    private TextureView preview;
    private ImageView back;
    private ImageView exposure;
    private ImageView reverse;
    private ImageView recordBtn;
    private TextView nextStep;
    private ProgressBar record_progress;

    private SurfaceTexture surface;

    //about record videos
    private Camera camera;
    private Camera.Parameters parameters;
    private MediaPlayer music;
    private MediaRecorder mediaRecorder;
    private String savedVideoPath;
    private int backCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int frontCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean isLightOn;
    private boolean isBackCameraOn ;
    private boolean isRecordOn = false;

    private float oldDist =1f;
    private static final int DURATION = 15000 ;//set max video duration

    //listener

    private VideoCaptureDurationListener durationListener ;

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
        record_progress = findViewById(R.id.record_progress);
    }

    private void registerListeners(){
        preview.setSurfaceTextureListener(this);
        back.setOnClickListener(this);
        exposure.setOnClickListener(this);
        reverse.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
        nextStep.setOnClickListener(this);

        addTouchListeners();
    }


    private void addTouchListeners() {

        preview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getPointerCount()==1){
                    touchFocus(event);
                }else {
                    changeZoom(event);
                }
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.exposure:
                switchFlash();
                break;
            case R.id.camera_id:
                switchCamera();
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
        if (parameters==null){
            parameters = camera.getParameters();
            isLightOn = false;
            //set auto focused , There should be a clicking focus feature here
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            int length = previewSizes.size();
            for (int i = 0; i < length; i++) {
                Log.d(TAG,"SupportedPreviewSizes : " + previewSizes.get(i).width + "x" + previewSizes.get(i).height);
            }
            parameters.setPreviewSize(1920,1080);//choose the right size of phone.
        }


        camera.setParameters(parameters);
        camera.setDisplayOrientation(90);//but the front camera is mirror
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
            record_progress.setVisibility(View.VISIBLE);
//            recordBtn.setImageResource(R.mipmap.stop);
            recordBtn.setImageResource(R.mipmap.record);
            playMusic(R.raw.di);
            record();
        }else{
            isRecordOn = false;
            record_progress.setVisibility(View.INVISIBLE);
            playMusic(R.raw.po);
            recordBtn.setImageResource(R.mipmap.record);
            stop();
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
        mediaRecorder.setMaxDuration(DURATION);
        durationListener = new VideoCaptureDurationListener();
        mediaRecorder.setOnInfoListener(durationListener);


        //save video
        String root = getCamera2Path();
        createSavePath(root);
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


        Intent intent = new Intent(this,PreviewActivity.class);
        intent.putExtra("videoPath",savedVideoPath);
        startActivity(intent);
    }


    private void switchCamera(){
        releaseCamera();
        if (isBackCameraOn){
            openCamera(frontCamera);
        }else{
            openCamera(backCamera);
        }
    }

    private void releaseCamera(){
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private void switchFlash(){
         if (parameters!=null){
             if (!isLightOn){
                 exposure.setImageResource(R.mipmap.exposure);
                 parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                 isLightOn = true;
                 camera.setParameters(parameters);
             }else{
                 exposure.setImageResource(R.mipmap.flash_off);
                 parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                 isLightOn = false;
                 camera.setParameters(parameters);
             }
         }
    }

    /**
     * scaling
     * @param event touch event
     */
    private void changeZoom(MotionEvent event){
        Log.d(TAG, "changeZoom: scaling");
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = getFingerSpacing(event);
                break;
            case MotionEvent.ACTION_MOVE:
                float newDist = getFingerSpacing(event);
                if (newDist >1.1 * oldDist) {
                    handleZoom(true);
                } else if (newDist < oldDist) {
                    handleZoom(false);
                }
                oldDist = newDist;
                break;
        }
    }

    private void handleZoom(boolean isZoomIn) {
        Camera.Parameters params = camera.getParameters();
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            params.setZoom(zoom);
            //there should be set the corresponding clarity
            camera.setParameters(params);
        } else {
            Log.i(TAG, "zoom not supported");
        }
    }

    private void touchFocus(MotionEvent event){

        Log.d(TAG, "touchFocus: 点击，对焦区域");

        Camera.Parameters params = camera.getParameters();
        Camera.Size previewSize = params.getPreviewSize();
        Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, previewSize);

        camera.cancelAutoFocus();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            Log.i(TAG, "focus areas not supported");
        }

        Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f, previewSize);
        if (params.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 800));
            params.setMeteringAreas(meteringAreas);
        } else {
            Log.i(TAG, "metering areas not supported");
        }

        final String currentFocusMode = params.getFocusMode();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        camera.setParameters(params);

        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(currentFocusMode);
                camera.setParameters(params);
            }
        });
    }


    /**
     * listener
     */

    private class VideoCaptureDurationListener implements MediaRecorder.OnInfoListener{
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            if(what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
                Log.d("DurationListener", "Maximum Duration Reached");
                stop();
            }
        }
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

    private Rect calculateTapArea(float x, float y, float coefficient, Camera.Size previewSize) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / previewSize.width - 1000);
        int centerY = (int) (y / previewSize.height - 1000);

        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private  int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }


    private  float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

}
