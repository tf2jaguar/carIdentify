package com.jelly.icar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    // 申请权限的requestCode
    private static final int PERMISSION_REQUEST_CODE = 0x00000012;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * 检查权限，开始扫描
     */
    public void scan(View view) {
        if (checkPermission()) {
            startActivity(new Intent(this, ScanActivity.class));
        }
    }

    /**
     * 调用相机前先检查权限。
     */
    private boolean checkPermission() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.CAMERA);
        int hasInternetPermission = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.INTERNET);

        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED &&
                hasInternetPermission == PackageManager.PERMISSION_GRANTED) {
            //有权限，清空展示文字，调起相机拍照。
            return true;
        } else {
            //没有权限，申请权限。
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.INTERNET}, PERMISSION_REQUEST_CODE);
        }
        return false;
    }

    /**
     * 处理权限申请的回调。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //允许权限，有调起相机扫描
                scan(null);
            } else {
                //拒绝权限，弹出提示框。
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_LONG).show();
            }
        }
    }
}
