package com.topanimestream.utilities;

import android.accounts.NetworkErrorException;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.topanimestream.App;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class ODataUtils {

    public static <T> void GetEntity(String url, final Class<T> classType, final Callback<T> callback)
    {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Token 900156f4-43e6-475d-ba36-127cea10ac16")
                .build();

        OkHttpClient client = App.getHttpClient();

        client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        Gson gson = new Gson();
                        T result = gson.fromJson(response.body().string(), classType);
                        callback.onSuccess(result);
                        return;
                    }

                }
                catch (Exception e)
                {
                    callback.onFailure(e);
                }
                callback.onFailure(new NetworkErrorException("Failed to fetch the data."));
            }
        });
    }

    public static <T> void GetEntityList(String url, final Class<T> classType, final Callback<ArrayList<T>> callback)
    {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Token 900156f4-43e6-475d-ba36-127cea10ac16")
                .build();

        OkHttpClient client = App.getHttpClient();

        client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        Gson gson = new Gson();
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray jsonArray = json.getJSONArray("value");
                        ArrayList<T> genericList = new ArrayList<T>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            genericList.add(gson.fromJson(jsonArray.get(i).toString(), classType));
                        }
                        callback.onSuccess(genericList);
                        return;
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }
        });
    }

    public interface Callback<T>
    {
        void onSuccess(T entity);
        void onFailure(Exception e);
    }

}
