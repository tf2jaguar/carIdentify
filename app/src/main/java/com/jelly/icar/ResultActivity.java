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
            Log.e("ResultActivity", "path invalid!");
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
                Log.i("ResultActivity", "refresh bd token and retry.");
                bdMsg = classifier.predictByBD(imgPath);
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
        // 使用矩阵旋转90°
        Bitmap bitmap = roteBitmap(imgPath);
        imageView.setImageBitmap(bitmap);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.max(heightRatio, widthRatio);
        }
        return inSampleSize;
    }

    public Bitmap roteBitmap(Bitmap origin, int angle) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的bitmap
        return Bitmap.createBitmap(origin, 0, 0,
                origin.getWidth(), origin.getHeight(), matrix, true);
    }

    public Bitmap roteBitmap(String imgPath) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        bitmap = BitmapFactory.decodeFile(imgPath, options);
        options.inSampleSize = calculateInSampleSize(options, 1120, 1120);
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(imgPath, options);
        return roteBitmap(bitmap, 90);
    }

}
