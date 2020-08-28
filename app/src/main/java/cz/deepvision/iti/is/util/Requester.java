package cz.deepvision.iti.is.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cz.deepvision.iti.is.ui.dialog.DefaultDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

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

        Request request = new Request.Builder().url(imageUrl).build();
        getUnsafeOkHttpClient().newCall(request).enqueue(new Callback() {
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
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.update(bitmap);
                        }
                    });
                    throw new IOException("Unexpected code " + response);
                }
            }
        });
    }
    public void makeRequestForAdapter(String imageUrl, ImageView photo) {

        Request request = new Request.Builder().url(imageUrl).build();
        getUnsafeOkHttpClient().newCall(request).enqueue(new Callback() {
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
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            photo.setImageBitmap(bitmap);
                        }
                    });
                    throw new IOException("Unexpected code " + response);
                }
            }
        });
    }


    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }).build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public interface UpdatePhoto {
        public void update(Bitmap bmp);
    }
}
