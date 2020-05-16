package com.jelly.icar.util;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jelly.icar.constants.Constants;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


public class Classifier {

    private Module model;
    private float[] mean = {0.485f, 0.456f, 0.406f};
    private float[] std = {0.229f, 0.224f, 0.225f};

    public Classifier(String modelPath) {
        model = Module.load(modelPath);
    }

    public void setMeanAndStd(float[] mean, float[] std) {
        this.mean = mean;
        this.std = std;
    }

    public Tensor preprocess(Bitmap bitmap, int size) {
        bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
        return TensorImageUtils.bitmapToFloat32Tensor(bitmap, this.mean, this.std);
    }

    public int argMax(float[] inputs) {
        int maxIndex = -1;
        float maxvalue = 0.0f;

        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] > maxvalue) {
                maxIndex = i;
                maxvalue = inputs[i];
            }
        }
        return maxIndex;
    }

    public String predictByModel(Bitmap bitmap) {
        Tensor tensor = preprocess(bitmap, 224);

        IValue inputs = IValue.from(tensor);
        Tensor outputs = model.forward(inputs).toTensor();
        float[] scores = outputs.getDataAsFloatArray();

        int classIndex = argMax(scores);

        return Constants.IMAGENET_CLASSES[classIndex];
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String predictByBD(String path) {
        Map<String, String> params = new HashMap<>(2);

        byte[] bytes;
        String imgParam = "";
        try {
            bytes = Utils.readFileToBytes(path);
            imgParam = URLEncoder.encode(Base64.getEncoder().encodeToString(bytes), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        params.put("image", imgParam);
        params.put("top_num", "3");

        JSONObject res = JSON.parseObject(OkHttpUtils.getInstance()
                .doPostForm(Constants.BD_AI_URL, params));
        if (res.getString("error_msg") != null) {
            Log.e("BD_ERROR", res.getString("error_msg"));
            return null;
        }
        JSONObject result = res.getJSONArray("result").getJSONObject(0);

        String year = result.getString("year");
        String name = result.getString("name");
        return year + "-" + name;
    }

}

