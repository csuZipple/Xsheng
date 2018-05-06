package zippler.cn.xs.activity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import zippler.cn.xs.R;
import zippler.cn.xs.util.Camera2Config;
import zippler.cn.xs.util.Camera2Util;

/**
 * To record videos and set surface preview
 */
public class RecordActivity extends BaseActivity implements TextureView.SurfaceTextureListener {

    private TextureView preview;
    private ImageView back;
    private ImageView exposure;
    private ImageView reverse;
    private TextView nextStep;
    private ImageView recordBtn;

    //control camera
    private CameraManager cameraManager;
    private String cameraFront;
    private String cameraBack;
    private boolean isCameraFront = false;
    private boolean isLightOn = false;
    private Size mPreviewSize;
    private Size mCaptureSize;
    private ImageReader imageReader;//but it will be a video...how to deal with it ?
    private CameraDevice cameraDevice;

    private ImageReader mImageReader;
    private CaptureRequest.Builder mPreviewBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mPreviewSession;

    //handler
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;

    //config class
    private CameraDeviceStateCallback stateCallback;
    private CameraCharacteristics cameraCharacteristics;

    //record
    private static final int MAX_RECORD_TIME = Camera2Config.RECORD_MAX_TIME;//最大录制时长,默认15S
    private static final int MIN_RECORD_TIME = Camera2Config.RECORD_MIN_TIME;//最小录制时长，默认2S
    private boolean isRecorded = false;//是否正在录制视频
    private boolean isStop = false;//是否停止过了MediaRecorder
    private int currentTime;
    private MediaRecorder mMediaRecorder;

    private String videoSavePath;
    //constant
    private int width;//width of TextureView
    private int height;//height of TextureView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initViews();

        initTexture();
        //set next step enabled
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.record_btn:
                //record here has some bugs ....
                if (!isRecorded){
                    prepareMediaRecorder();
                    startButtonAnima();
                    isRecorded = true; //如果按下后经过500毫秒则会修改当前状态为长按状态，标记为正在录制中
                    startMediaRecorder();//开始录制
                }else{
                    stopButtonAnima();
                    isRecorded = false;
                    stopMediaRecorder();
                }

                break;
            default:
                toast("default clicked in preview camera");
                break;
        }
    }

    private void initViews(){
        preview = findViewById(R.id.preview);
        back = findViewById(R.id.back);
        exposure = findViewById(R.id.exposure);
        reverse = findViewById(R.id.camera_id);
        nextStep = findViewById(R.id.next_step);
        recordBtn = findViewById(R.id.record_btn);
        //add two finger scaling
        registerListeners();
        addOnTouchListener();
    }

    private void registerListeners() {
        recordBtn.setOnClickListener(this);
        back.setOnClickListener(this);
        nextStep.setOnClickListener(this);
        exposure.setOnClickListener(this);
        reverse.setOnClickListener(this);
    }

    /**
     * set touch listener for views
     */
    private void addOnTouchListener(){
        preview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                //两指缩放
                changeZoom(event);
                return true;
            }

        });
    }

    private void initTexture(){
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
        preview.setSurfaceTextureListener(this);
    }

    /**
     * start preview
     */
    private void startPreview(){
        if (null == cameraDevice || !preview.isAvailable() || null == mPreviewSize) {
            return;
        }

        SurfaceTexture mSurfaceTexture = preview.getSurfaceTexture();//获取TextureView的SurfaceTexture，作为预览输出载体

        if (mSurfaceTexture == null) {
            return;
        }

        try {
            closePreviewSession();
            mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());//设置TextureView的缓冲区大小
            mPreviewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);//创建CaptureRequestBuilder，TEMPLATE_PREVIEW比表示预览请求
            Surface mSurface = new Surface(mSurfaceTexture);//获取Surface显示预览数据
            mPreviewBuilder.addTarget(mSurface);//设置Surface作为预览数据的显示界面

            //默认预览不开启闪光灯
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);

            //创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            cameraDevice.createCaptureSession(Arrays.asList(mSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        //创建捕获请求
                        mCaptureRequest = mPreviewBuilder.build();
                        mPreviewSession = session;
                        //不停的发送获取图像请求，完成连续预览
                        mPreviewSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("preview", "捕获的异常" + e.toString());
        }
    }

    //clean session
    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    /**
     * init camera size
     * @param width preview size
     * @param height preview size
     */
    private void setupCamera(int width,int height){
         cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            assert cameraManager != null;
            cameraBack = cameraManager.getCameraIdList()[0];
            cameraFront = cameraManager.getCameraIdList()[1];

            if (isCameraFront){
                cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraFront);
            }else{
                cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraBack);
            }

            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            mPreviewSize = Camera2Util.getMinPreSize(map.getOutputSizes(SurfaceTexture.class), width, height, Camera2Config.PREVIEW_MAX_HEIGHT);
            mCaptureSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth());
                }
            });


            configureTransform(width, height);

            setupImageReader();
            //MediaRecorder用于录像所需
            mMediaRecorder = new MediaRecorder();

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupImageReader(){
        mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(),
                ImageFormat.JPEG, 2);

        //add listener here..
        //but we do not need it .
    }

    private void configureTransform(int width, int height) {
        if (null == preview || null == mPreviewSize) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, width, height);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) height / mPreviewSize.getHeight(),
                    (float) width / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        preview.setTransform(matrix);
    }

    /**
     * Open the camera at the designated position
     * @param id position,1 for front and 0 for back
     */
    private void openCamera(String id){
//获取摄像头的管理者CameraManager
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        //检查权限
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //打开相机，第一个参数指示打开哪个摄像头，第二个参数stateCallback为相机的状态回调接口，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            manager.openCamera(id, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startPreview();

            if (null != preview) {
                configureTransform(preview.getWidth(), preview.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    /**
     * ********************************   about record videos  *******************************************
     */
    private void setUpMediaRecorder() {
        try {
            Log.e("mediaRecorder", "setUpMediaRecorder");
            mMediaRecorder.reset();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            // 这里有点投机取巧的方式，不过证明方法也是不错的
            // 录制出来10S的视频，大概1.2M，清晰度不错，而且避免了因为手动设置参数导致无法录制的情况
            // 手机一般都有这个格式CamcorderProfile.QUALITY_480P,因为单单录制480P的视频还是很大的，所以我们在手动根据预览尺寸配置一下videoBitRate,值越高越大
            // QUALITY_QVGA清晰度一般，不过视频很小，一般10S才几百K
            // 判断有没有这个手机有没有这个参数
            if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                profile.videoBitRate = mPreviewSize.getWidth() * mPreviewSize.getHeight();
                mMediaRecorder.setProfile(profile);
                mMediaRecorder.setPreviewDisplay(new Surface(preview.getSurfaceTexture()));
            } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
                profile.videoBitRate = mPreviewSize.getWidth() * mPreviewSize.getHeight();

                mMediaRecorder.setProfile(profile);
                mMediaRecorder.setPreviewDisplay(new Surface(preview.getSurfaceTexture()));
            } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QVGA)) {
                mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA));
                mMediaRecorder.setPreviewDisplay(new Surface(preview.getSurfaceTexture()));
            } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_CIF)) {
                mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_CIF));
                mMediaRecorder.setPreviewDisplay(new Surface(preview.getSurfaceTexture()));
            } else {
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mMediaRecorder.setVideoEncodingBitRate(10000000);
                mMediaRecorder.setVideoFrameRate(30);
                mMediaRecorder.setVideoEncodingBitRate(2500000);
                mMediaRecorder.setVideoFrameRate(20);
                mMediaRecorder.setVideoSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            }

            //判断有没有配置过视频地址了
            Camera2Util.createSavePath(Camera2Config.PATH_SAVE_VIDEO);//判断有没有这个文件夹，没有的话需要创建
            @SuppressLint("SimpleDateFormat")
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            videoSavePath = Camera2Config.PATH_SAVE_VIDEO + "Xsheng_VIDEO_" + timeStamp + ".mp4";
            mMediaRecorder.setOutputFile(videoSavePath);

            //判断是不是前置摄像头,是的话需要旋转对应的角度
            if (isCameraFront) {
                mMediaRecorder.setOrientationHint(270);
            } else {
                mMediaRecorder.setOrientationHint(90);
            }
            mMediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //预览录像
    private void prepareMediaRecorder() {
        if (null == cameraDevice || !preview.isAvailable() || null == mPreviewSize) {
            return;
        }

        try {
            closePreviewSession();
            Log.e("prepare record video", "prepareMediaRecorder");
            setUpMediaRecorder();

            SurfaceTexture texture = preview.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            //判断预览之前有没有开闪光灯
            if (isLightOn) {
                mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            } else {
                mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            }

            //保持当前的缩放比例
            mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);

            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        //创建捕获请求
                        mCaptureRequest = mPreviewBuilder.build();
                        mPreviewSession = session;
                        //设置反复捕获数据的请求，这样预览界面就会一直有数据显示
                        mPreviewSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("dasdasdasdas", "捕获的异常2" + e.toString());
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                   toast("failed");
                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //开始录像
    private void startMediaRecorder() {
        Log.e("daasddasd", "startMediaRecorder");
        // Start recording
        try {
            mMediaRecorder.start();
            //开始计时，判断是否已经超过录制时间了
            mCameraHandler.postDelayed(recordRunnable, 0);
            isStop = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            currentTime++;
//            //开始显示进度条
//            mProgressView.setVisibility(View.VISIBLE);
//            mProgressView.setIsStart(true);
//            //显示时间
//            tvBalanceTime.setVisibility(View.VISIBLE);
//            tvBalanceTime.setText(MAX_RECORD_TIME - currentTime + "s");

            //如果超过最大录制时长则自动结束
            if (currentTime > MAX_RECORD_TIME) {
                stopMediaRecorder();
            } else {
                mCameraHandler.postDelayed(this, 1000);
            }
        }
    };

    //停止录像
    private void stopMediaRecorder() {
        if (TextUtils.isEmpty(videoSavePath)) {
            return;
        }
        try {
            //结束ProgressView
//            mProgressView.setIsStart(false);
            mCameraHandler.removeCallbacks(recordRunnable);
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            isStop = true;

            //判断录制时常是不是小于指定秒数
            if (currentTime <= MIN_RECORD_TIME) {
                Toast.makeText(this, "录制时间过短", Toast.LENGTH_LONG).show();
            } else {
                //正常录制结束，跳转到完成页，这里你也可以自己处理
                if (Camera2Config.ACTIVITY_AFTER_CAPTURE != null) {
                    Intent intent = new Intent(this, Camera2Config.ACTIVITY_AFTER_CAPTURE);
                    intent.putExtra(Camera2Config.INTENT_PATH_SAVE_VIDEO, videoSavePath);
                    startActivity(intent);
                }
            }

            //录制完成后重置录制界面
            showResetCameraLayout();

        } catch (Exception e) {
            //这里抛出的异常是由于MediaRecorder开关时间过于短暂导致，直接按照录制时间短处理
            Toast.makeText(this, "录制时间过短", Toast.LENGTH_LONG).show();
            showResetCameraLayout();
        }

        currentTime = 0;
    }


    /**
     * **********************************************切换摄像头**************************************
     */
    public void switchCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }

        if (isCameraFront) {
            isCameraFront = false;
            setupCamera(width, height);
            openCamera(cameraBack);
        } else {
            isCameraFront = true;
            setupCamera(width, height);
            openCamera(cameraFront);
        }
    }

    /**
     * ***************************************打开和关闭闪光灯****************************************
     */
    public void openLight() {
        if (isLightOn) {
            exposure.setSelected(false);
            isLightOn = false;
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_OFF);
        } else {
            exposure.setSelected(true);
            isLightOn = true;
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_TORCH);
        }

        try {
            if (mPreviewSession != null)
                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mCameraHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showResetCameraLayout() {
        resetCamera();
//        mProgressView.setVisibility(View.INVISIBLE);
//        tvBalanceTime.setVisibility(View.GONE);
        //重置ProgressView
//        mProgressView.reset();
    }

    private void resetCamera() {
        if (TextUtils.isEmpty(cameraBack)) {
            return;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
        }

        setupCamera(width, height);
        openCamera(cameraBack);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.width = width;
        this.height = height;

        setupCamera(width, height);//set camera config
        openCamera(cameraBack);//open camera
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        configureTransform(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    class CameraDeviceStateCallback extends CameraDevice.StateCallback{

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "onOpened: camera opened");
            cameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    }


    /**
     * *********************************放大或者缩小**********************************
     */
    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float finger_spacing;
    int zoom_level = 0;
    Rect zoom;

    public void changeZoom(MotionEvent event) {
        try {
            //活动区域宽度和作物区域宽度之比和活动区域高度和作物区域高度之比的最大比率
            float maxZoom = (cameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)) * 10;
            Rect m = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

            int action = event.getAction();
            float current_finger_spacing;
            //判断当前屏幕的手指数
            if (event.getPointerCount() > 1) {
                //计算两个触摸点的距离
                current_finger_spacing = getFingerSpacing(event);

                if (finger_spacing != 0) {
                    if (current_finger_spacing > finger_spacing && maxZoom > zoom_level) {
                        zoom_level++;

                    } else if (current_finger_spacing < finger_spacing && zoom_level > 1) {
                        zoom_level--;
                    }

                    int minW = (int) (m.width() / maxZoom);
                    int minH = (int) (m.height() / maxZoom);
                    int difW = m.width() - minW;
                    int difH = m.height() - minH;
                    int cropW = difW / 100 * (int) zoom_level;
                    int cropH = difH / 100 * (int) zoom_level;
                    cropW -= cropW & 3;
                    cropH -= cropH & 3;
                    zoom = new Rect(cropW, cropH, m.width() - cropW, m.height() - cropH);
                    mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
                }
                finger_spacing = current_finger_spacing;
            } else {
                if (action == MotionEvent.ACTION_UP) {
                    //single touch logic,可做点击聚焦操作
                }
            }

            try {
                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
                            }
                        },
                        null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException("can not access camera.", e);
        }
    }

    //计算两个触摸点的距离
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    //开始按下按钮动画
    public void startButtonAnima() {
        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(recordBtn, "scaleX", 1f, 1.3f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(recordBtn, "scaleY", 1f, 1.3f);

        animatorSet.setDuration(100);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(scaleX).with(scaleY);//两个动画同时开始
        animatorSet.start();
    }

    //停止按下按钮动画
    public void stopButtonAnima() {
        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(recordBtn, "scaleX", 1.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(recordBtn, "scaleY", 1.3f, 1f);

        animatorSet.setDuration(100);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(scaleX).with(scaleY);//两个动画同时开始
        animatorSet.start();
    }
}
