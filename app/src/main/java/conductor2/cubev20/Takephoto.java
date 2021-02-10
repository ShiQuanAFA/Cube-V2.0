package conductor2.cubev20;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class Takephoto extends AppCompatActivity {

    private final String TAG = Takephoto.this.getClass().getSimpleName();
    private Camera camera;
    private boolean isPreview = false;
    private Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takephoto);
        // 形成预览界面
        SurfaceView mSurfaceView = findViewById(R.id.surface_view);
        final SurfaceHolder mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(mSurfaceCallback);
        // 抓取坐标点颜色按钮设置
        Button button_catch;
        button_catch = findViewById(R.id.button_catch);
        button_catch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Bitmap bmp_rotated = bmpRotation(bmp, 90);
                getBmpColors(bmp_rotated);
                Takephoto.this.finish();
            }
        });
        // 形成预览标记层
        SurfaceView surfaceView_mark = findViewById(R.id.surface_view_mark);
        surfaceView_mark.setZOrderOnTop(true);
        SurfaceHolder surfaceHolder_mark = surfaceView_mark.getHolder();
        surfaceHolder_mark.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder_mark.addCallback(markSurfaceCallback);
    }

    public SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(final SurfaceHolder holder) {
            try{
                // 定义相机对象，设置参数
                camera = Camera.open(0);
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewFormat(ImageFormat.NV21);
                List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
                Camera.Size optionSize = getOptimalPreviewSize(sizeList, holder.getSurfaceFrame().width(), holder.getSurfaceFrame().height());
                parameters.setPreviewSize(optionSize.width, optionSize.height);
                parameters.setPreviewFrameRate(25);
                parameters.setJpegQuality(85);
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(parameters);
                setCameraDisplayOrientation(Takephoto.this, 0, camera);
                // 在holder中预览图象
                camera.setPreviewDisplay(holder);
                camera.setPreviewCallback(new Camera.PreviewCallback(){
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera){
                        // 实时暂存每一帧的预览图像
                        Camera.Size size = camera.getParameters().getPreviewSize();
                        try {
                            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 85, stream);
                            bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                            stream.close();
                        } catch (Exception ex) {
                                Log.e("Sys", "Error:" + ex.getMessage());
                        }
                    }
                });
                camera.startPreview();
                isPreview = true;
            }catch (IOException e){
                Log.e(TAG, e.toString());
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if(camera != null) {
                if(isPreview){
                    holder.removeCallback(this);
                    camera.setPreviewCallback(null);
                    camera.stopPreview();
                    camera.lock();
                    camera.release();
                    camera = null;
                    isPreview = false;
                }
            }
        }
    };

    public SurfaceHolder.Callback markSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // 在标记层绘制标记点
            Paint paint_mark = new Paint();
            paint_mark.setColor(getResources().getColor(R.color.colorPrimary));
            paint_mark.setAntiAlias(true);
            paint_mark.setStyle(Paint.Style.FILL);
            Canvas canvas_mark = holder.lockCanvas();
            int part_width = holder.getSurfaceFrame().width() / 4;
            int part_height = holder.getSurfaceFrame().height() / 4;
            for (int x = 0; x <= 2; x++){
                for (int y = 0; y <= 2; y++){
                    canvas_mark.drawCircle((x + 1) * part_width, (y + 1) * part_height, 10, paint_mark);
                }
            }
            holder.unlockCanvasAndPost(canvas_mark);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera){
     Camera.CameraInfo info = new Camera.CameraInfo();
     Camera.getCameraInfo(cameraId, info);
     int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
     int degrees = 0;
     switch (rotation){
         case Surface.ROTATION_0:
             degrees = 0;
             break;
         case Surface.ROTATION_90:
             degrees = 90;
             break;
         case Surface.ROTATION_180:
             degrees = 180;
             break;
         case Surface.ROTATION_270:
             degrees = 270;
             break;
     }
     int result;
     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
         result = (info.orientation + degrees) % 360;
         result = (360 - result) % 360;
     }else {
         result = (info.orientation - degrees + 360) % 360;
     }
     camera.setDisplayOrientation(result);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
    }

    public Bitmap bmpRotation(Bitmap img, int degrees){
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        int width = img.getWidth();
        int height = img.getHeight();
        return Bitmap.createBitmap(img, 0, 0, width, height, matrix, true);
    }

    public void getBmpColors(Bitmap bmp){
        final Data app = (Data)getApplication();
        int num_pictures = app.getNUM_PICTURES();
        int part_width = bmp.getWidth() / 4;
        int part_height = bmp.getHeight() / 4;
        int pixel;
        for (int x = 0; x <= 2; x++){
            for (int y = 0; y <= 2; y++){
                pixel = bmp.getPixel((x + 1) * part_width, (y + 1) * part_height);
                app.setPIC(num_pictures, x, y, 0, Color.red(pixel));
                app.setPIC(num_pictures, x, y, 1, Color.green(pixel));
                app.setPIC(num_pictures, x, y, 2, Color.blue(pixel));
            }
        }
        app.setNUM_PICTURES(num_pictures + 1);
    }

}
