package com.jelly.icar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.jelly.icar.constants.Constants;
import com.jelly.icar.util.Classifier;
import com.jelly.icar.util.Utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ResultActivity extends AppCompatActivity {

    private Classifier classifier;
    private TextView verifyResult;
    private ImageView imageView;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imageView = findViewById(R.id.pic);
        verifyResult = findViewById(R.id.pic_res);

        String path = getIntent().getStringExtra("picPath");
        if (path == null || "".equals(path.replaceAll(" ", ""))) {
            Log.e("", "获取传递信息失败");
            return;
        }

        //加载配置文件
        Utils.loadConfig(this, "carConfig");
        classifier = new Classifier(Utils.assetFilePath(this, Constants.MODEL_PATH));

        // 验证
        verify(path);

        //展示pic
        showVerifyPic(path);
    }

    private Bitmap roteBitmap(String imgPath) {
        Bitmap bitmap = null;
        try {
            FileInputStream fis = new FileInputStream(imgPath);
            bitmap = BitmapFactory.decodeStream(fis);
            // 设置一个矩阵，旋转九十度
            Matrix matrix = new Matrix();
            matrix.setRotate(90);
            // 将原来的图片的bitmap旋转九十度
            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 交由 识别器 识别
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void verify(String imgPath) {
        new Thread(() -> {
            String msg;
            String modelMsg = classifier.predictByModel(roteBitmap(imgPath));
            String bdMsg = classifier.predictByBD(imgPath);

            if (bdMsg == null) {
                Utils.refreshBToken();
                Log.i(null, "refresh bd token.");
            }

            msg = modelMsg + " " + bdMsg;
            showVerifyResult(msg);
        }).start();
    }

    /**
     * 显示识别结果
     */
    private void showVerifyResult(final String result) {
        runOnUiThread(() -> verifyResult.setText(result));
    }

    /**
     * 显示图片
     */
    private void showVerifyPic(final String imgPath) {
        // 方法一：最终显示图片为旋转90°的
//        Bitmap bitmap = BitmapFactory.decodeFile(path);
//        imageView.setImageBitmap(bitmap);

        // 方法二：使用矩阵旋转90°
        Bitmap bitmap = roteBitmap(imgPath);
        imageView.setImageBitmap(bitmap);
    }
}
