package com.yandex.minavegador.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by user on 31/07/2016.
 */
public class Utils {

    @WorkerThread
    @Nullable
    public static Bitmap getFavicon(Context context, @NonNull String url) {
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        if (host == null) {
            return null;
        }
        String faviconUrl = "http://favicon.yandex.net/favicon/" + host;
        InputStream inputStream = null;
        try {
            URL u = new URL(faviconUrl);
            URLConnection dc = u.openConnection();
            dc.setConnectTimeout(5000);
            dc.setReadTimeout(5000);
            inputStream = dc.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(dc.getInputStream());
            final int w = context.getResources()
                    .getDimensionPixelSize(android.R.dimen.app_icon_size);
            return Bitmap.createScaledBitmap(bitmap, w, w, true);
        } catch (Exception e) {
            return null;
        } finally {
            closeSilently(inputStream);
        }
    }

    private static void closeSilently(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (IOException e) {
            }
        }
    }

    public static String getData(String url) throws IOException {
        BufferedReader inputStream;

        URL jsonUrl = new URL(url);
        URLConnection dc = jsonUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);

        inputStream = new BufferedReader(new InputStreamReader(
                dc.getInputStream()));

        return inputStream.readLine();
    }
}
