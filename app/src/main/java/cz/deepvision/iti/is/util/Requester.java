package cz.deepvision.iti.is.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import cz.deepvision.iti.is.ui.dialog.DefaultDialog;
import okhttp3.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Requester {
    private Activity activity;
    private DefaultDialog dialog;

    public Requester(Activity activity, DefaultDialog dialog) {
        this.activity = activity;
        this.dialog = dialog;
    }

    public Requester(Activity activity) {
        this.activity = activity;
    }

    public void makeRequest(String imageUrl) {
        if (loadImageFromStorage(imageUrl) != null)
            activity.runOnUiThread(() -> dialog.update(loadImageFromStorage(imageUrl)));
         else {
            Request request = new Request.Builder().url(imageUrl).build();
            NetworkConnection.getInstance().getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (response.isSuccessful()) {
                        ResponseBody in = response.body();
                        InputStream inputStream = in.byteStream();
                        // convert inputstram to bufferinoutstream
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                        Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);
                        saveToInternalStorage(bitmap, imageUrl);
                        activity.runOnUiThread(() -> dialog.update(bitmap));
                        throw new IOException("Unexpected code " + response);
                    }
                }
            });
        }
    }

    public void makeRequestForAdapter(String imageUrl, ImageView photo) {
        if (loadImageFromStorage(imageUrl) != null)
            activity.runOnUiThread(() -> photo.setImageBitmap(loadImageFromStorage(imageUrl)));
         else {
            Request request = new Request.Builder().url(imageUrl).build();
            NetworkConnection.getInstance().getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (response.isSuccessful()) {
                        ResponseBody in = response.body();
                        InputStream inputStream = in.byteStream();
                        // convert inputstram to bufferinoutstream
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                        Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);
                        saveToInternalStorage(bitmap, imageUrl);
                        activity.runOnUiThread(() -> photo.setImageBitmap(bitmap));
                    }
                }
            });
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage, String url) {
        ContextWrapper cw = new ContextWrapper(activity.getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        url = url.replace('/','_');
        File mypath = new File(directory, url);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(fos != null)
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private Bitmap loadImageFromStorage(String image) {
        try {
            ContextWrapper cw = new ContextWrapper(activity.getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            image = image.replace('/','_');
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File f = new File(directory.getPath(), image);
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface UpdatePhoto {
        public void update(Bitmap bmp);
    }
}
