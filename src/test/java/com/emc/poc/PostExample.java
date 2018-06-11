package com.emc.poc;

import java.io.IOException;
import java.util.Date;

import okhttp3.*;

public class PostExample {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static OkHttpClient client = new OkHttpClient();

    public static String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .addHeader("Accept-Encoding", "gzip,deflate")
                .addHeader("Authorization", "Basic cm9vdDplYXN5b3Bz")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static void main(String[] args) throws IOException {
        int i = 0;
        String json = "insert into v_test1 content {\n" +
                "  \"name\": \"test"+i+"\",\n" +
                "  \"IPS_String\": \"99.13.31.0, 99.13.31.1, 99.13.31.2, 99.13.31.3, 99.13.31.4\",\n" +
                "  \"IPS_EmbeddedList\": [\"99.13.31.0\", \"99.13.31.1\", \"99.13.31.2\", \"99.13.31.3\", \"99.13.31.4\"],\n" +
                "  \"businessId\": \"ac4643ea7481916003bfcf41aa48ac3a\",\n" +
                "  \"_category\": \"ttt\",\n" +
                "  \"memo\": \"aaa\",\n" +
                "  \"instanceId\": " + i + ",\n" +
                "  \"_object_id\": \"BUSINESS\",\n" +
                "  \"creator\": \"easyops\",\n" +
                "  \"ctime\": \""+i+"\",\n" +
                "  \"org\": 1009,\n" +
                "  \"_id\": {\n" +
                "    \"$oid\": \"5a4deb0ede74fe6f9d2b7392\"\n" +
                "  },\n" +
                "  \"_ts\": 1515055886,\n" +
                "  \"_version\": 1,\n" +
                "  \"__SYNC__BATCH__\": 1515787201\n" +
                "}";
        long startTime = System.currentTimeMillis();
        post("http://139.219.102.105:2480/command/test1/sql", json);
        long endTime = System.currentTimeMillis();
        System.out.println(endTime-startTime);
    }
}
