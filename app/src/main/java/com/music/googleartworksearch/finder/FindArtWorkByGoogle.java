package com.music.googleartworksearch.finder;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;

import com.music.googleartworksearch.utils.DebugLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.core.util.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FindArtWorkByGoogle {

    public static FinderCloseableRequest find(Context context, String artistName, String albumName, Consumer<List<String>> callback) {
        String finalKeyword = "Album " + artistName + " " + albumName;
        return find(context, finalKeyword, callback);
    }

    public static FinderCloseableRequest find(Context context, String keyword, Consumer<List<String>> callback) {
        String finalUrl = String.format("https://www.google.com/search?site=imghp&tbm=isch&source=hp&q=%s&tbs=isz:l", Uri.encode(keyword));
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();
        DebugLog.logd("Url: " + finalUrl);

        TimeUnit timeUnit = TimeUnit.SECONDS;
        ConnectionPool connectionPool = new ConnectionPool(5, 6L, timeUnit);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(3L, timeUnit)
                .readTimeout(6_000L, TimeUnit.MILLISECONDS)
                .writeTimeout(6_000L, TimeUnit.MILLISECONDS)
                .connectionPool(connectionPool);
        builder.networkInterceptors().add(new UserAgentInterceptor(WebSettings.getDefaultUserAgent(context)));
        OkHttpClient okHttpClient = builder.build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                DebugLog.loge(e);
                if (!call.isCanceled()) {
                    sendEmptyCallback(callback);
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (call.isCanceled()) {
                    return;
                }
                if (!response.isSuccessful()) {
                    sendEmptyCallback(callback);
                    return;
                }
                ResponseBody responseBody = response.body();
                String htmlString = responseBody != null ? responseBody.string() : null;
                try {
                    doGetArtWorks(htmlString, callback);
                } catch (Exception e) {
                    DebugLog.loge(e);
                    sendEmptyCallback(callback);
                }
            }
        });
        return () -> {
            if (!call.isCanceled()) {
                DebugLog.logd("Cancel request with keyword  " + keyword);
                call.cancel();
            }
        };
    }

    private static void doGetArtWorks(String htmlString, Consumer<List<String>> callback) {
        List<String> listImageFromHtml = getListImageFromHtml(htmlString);
        if (listImageFromHtml.isEmpty()) {
            sendEmptyCallback(callback);
        } else {
            if (callback != null) {
                callback.accept(listImageFromHtml);
            }
        }
    }

    public static List<String> getListImageFromHtml(String str) {
        ArrayList<String> arrayList = new ArrayList<>();
        Matcher matcher = Pattern.compile("(src=\"http)(.*?)(\")").matcher(str);
        matcher.find();
        while (matcher.find()) {
            if (arrayList.isEmpty()) {
                arrayList.add("https://www.tohsoft.images/none");
            }
            String group = matcher.group();
            if (group.length() > 20 && !group.contains("?url=")) {
                String url = group.substring(5, group.length() - 1);
                if (url.endsWith(".svg")) {
                    continue;
                }
                arrayList.add(url);
            }
        }
        return arrayList;
    }

    private static void sendEmptyCallback(Consumer<List<String>> callback) {
        if (callback != null) {
            callback.accept(Collections.emptyList());
        }
    }
}
