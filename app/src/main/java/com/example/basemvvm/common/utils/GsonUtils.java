package com.example.basemvvm.common.utils;

import com.google.gson.Gson;

public class GsonUtils {

    private static Gson gson;

    public synchronized static Gson getInstance() {
        if (gson == null) {
            synchronized (GsonUtils.class) {
                if (gson == null) gson = new Gson();
            }
        }
        return gson;
    }

    public static String serialize(Object object, Class clazz) {
        return getInstance().toJson(object, clazz);
    }

    public static <T> T deserialize(String string, Class<T> clazz) {
        return getInstance().fromJson(string, clazz);
    }
}
