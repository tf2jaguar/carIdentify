package com.jelly.icar.constants;

public class Constants {

    /**
     * 从配置读取
     */
    public static String MODEL_PATH = "";
    public static String BD_AI_CLIENT_ID = "";
    public static String BD_AI_CLIENT_SECRET = "";
    public static String BD_AI_TOKEN = "";
    public static String BD_AI_URL = "";

    /**
     * 以下固定
     */
    public static String BD_AI_URI = "https://aip.baidubce.com/rest/2.0/image-classify/v1/car?access_token=";
    public static String BD_AI_REFRESH_TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=";

    public static String[] IMAGENET_CLASSES = new String[]{
            "None", "MPV", "SUV", "sedan", "hatchback", "minibus", "fastback", "estate", "pickup",
            "hardtop convertible", "sports", "crossover", "convertible"
    };
}

