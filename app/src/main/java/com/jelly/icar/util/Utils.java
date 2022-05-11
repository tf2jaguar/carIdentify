package com.jelly.icar.util;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.jelly.icar.constants.Constants;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Utils {

    public static void refreshBToken() {
        String res = OkHttpUtils.getInstance().doGet(Constants.BD_AI_REFRESH_TOKEN_URL);
        Constants.BD_AI_TOKEN = JSON.parseObject(res).getString("access_token");
        Constants.BD_AI_URL = Constants.BD_AI_URI + Constants.BD_AI_TOKEN;
    }

    public static byte[] readFileToBytes(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        } else {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length())) {
                BufferedInputStream in;
                in = new BufferedInputStream(new FileInputStream(file));
                short bufSize = 1024;
                byte[] buffer = new byte[bufSize];
                int len1;
                while (-1 != (len1 = in.read(buffer, 0, bufSize))) {
                    bos.write(buffer, 0, len1);
                }

                return bos.toByteArray();
            }
        }
    }

    public static String assetFilePath(Context context, String assetName) {
        File file = new File(context.getFilesDir(), assetName);

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e("Utils", "train error process asset " + assetName + " to file path");
        }
        return null;
    }

    public static Properties readProperties(Context c, String propertiesPath) {
        Properties props = new Properties();
        try {
            InputStream in = c.getAssets().open(propertiesPath);
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    public static void loadConfig(Context context, String configPath) {
        Properties property = readProperties(context, configPath);
        String modelPath = property.getProperty("MODEL_PATH");
        String bdAiClientId = property.getProperty("BD_AI_CLIENT_ID");
        String bdAiClientSecret = property.getProperty("BD_AI_CLIENT_SECRET");
        String bdAiToken = property.getProperty("BD_AI_TOKEN");

        if (isBlank(modelPath) || isBlank(bdAiClientId) || isBlank(bdAiClientSecret)) {
            throw new IllegalArgumentException("读取配置文件错误");
        }
        Constants.BD_AI_CLIENT_ID = bdAiClientId;
        Constants.BD_AI_CLIENT_SECRET = bdAiClientSecret;
        Constants.BD_AI_TOKEN = bdAiToken;
        Constants.MODEL_PATH = modelPath;
        Constants.BD_AI_URL = Constants.BD_AI_URI + bdAiToken;
        Constants.BD_AI_REFRESH_TOKEN_URL += (bdAiClientId + "&client_secret=" + bdAiClientSecret);
    }

    private static boolean isBlank(String str) {
        return str == null || "".equals(str.replaceAll(" ", ""));
    }

}
