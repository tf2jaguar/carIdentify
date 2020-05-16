package com.jelly.icar;

import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ScanActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Camera mCamera;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    private Camera.PictureCallback mPictureCallback = (data, camera) -> {
        File tempFile = new File(getCacheDir(), "temp.png");
        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(data);
            fos.close();

            Intent intent = new Intent(ScanActivity.this, ResultActivity.class);
            intent.putExtra("picPath", tempFile.getAbsolutePath());
            startActivity(intent);
            ScanActivity.this.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mPreview = findViewById(R.id.preview);
        mHolder = mPreview.getHolder();
        mHolder.addCallback(this);
        // 点击页面 立即聚焦 进行下一步
        mPreview.setOnClickListener(v -> capture(null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = getCamera();
            if (mHolder != null) {
                setStartPreview(mCamera, mHolder);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    public void capture(View view) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setPreviewSize(800, 400);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.autoFocus((success, camera) -> {
            if (success) {
                mCamera.takePicture(null, null, mPictureCallback);
            }
        });
    }

    /**
     * 获取camera对象
     */
    private Camera getCamera() {
        Camera camera;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            camera = null;
            e.printStackTrace();
        }
        return camera;
    }

    /**
     * 开始预览相机内容
     */
    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            //将系统camera预览角度调整，横屏 转换为 竖屏
            camera.setDisplayOrientation(90);
            camera.startPreview();

            // 1.5秒后聚焦、拍照 进行下一步
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    capture(null);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            // 相机回调 置空
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mCamera, mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        setStartPreview(mCamera, mHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }
}
