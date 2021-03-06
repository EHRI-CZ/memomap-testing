package cz.deepvision.iti.is;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import cz.deepvision.iti.is.models.markers.CustomMarker;


public class CustomClusterManager<M> extends ClusterManager {
    private GoogleMap map;
    private onCameraIdleExtension mOnCameraIdleExtension;
    private Context ctx;

    public CustomClusterManager(Context context, GoogleMap map) {
        super(context, map);
        ctx = context;
        this.map = map;
        this.setRenderer(new CustomRenderer(context, map, this));
    }

    @Override
    public void onCameraIdle() {
        super.onCameraIdle();
        mOnCameraIdleExtension.onCameraIdle();
    }

    public interface onCameraIdleExtension {
        void onCameraIdle();
    }

    public void setmOnCameraIdleExtension(onCameraIdleExtension mOnCameraIdleExtension) {
        this.mOnCameraIdleExtension = mOnCameraIdleExtension;
    }

    public float getRadius(GoogleMap googleMap) {
        VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();

        LatLng farRight = visibleRegion.farRight;
        LatLng farLeft = visibleRegion.farLeft;
        LatLng nearRight = visibleRegion.nearRight;
        LatLng nearLeft = visibleRegion.nearLeft;

        float[] distanceWidth = new float[2];
        Location.distanceBetween((farRight.latitude + nearRight.latitude) / 2, (farRight.longitude + nearRight.longitude) / 2, (farLeft.latitude + nearLeft.latitude) / 2, (farLeft.longitude + nearLeft.longitude) / 2, distanceWidth);


        float[] distanceHeight = new float[2];
        Location.distanceBetween((farRight.latitude + nearRight.latitude) / 2, (farRight.longitude + nearRight.longitude) / 2, (farLeft.latitude + nearLeft.latitude) / 2, (farLeft.longitude + nearLeft.longitude) / 2, distanceHeight);

        float distance;

        if (distanceWidth[0] > distanceHeight[0]) {
            distance = distanceWidth[0];
        } else {
            distance = distanceHeight[0];
        }
        return distance;
    }


    private class CustomRenderer extends DefaultClusterRenderer<CustomMarker> {
        Context cnt;

        public CustomRenderer(Context context, GoogleMap map, ClusterManager clusterManager) {
            super(context, map, clusterManager);
            cnt = context;
        }

        @Override
        protected void onBeforeClusterItemRendered(@NonNull CustomMarker item, @NonNull MarkerOptions markerOptions) {
            super.onBeforeClusterItemRendered(item, markerOptions);
            markerOptions.icon(bitmapDescriptorFromVector(cnt, item));
        }

        @Override
        protected void onClusterItemUpdated(@NonNull CustomMarker item, @NonNull Marker marker) {
            super.onClusterItemUpdated(item, marker);
            try {
                if (marker.getTag() != null) marker.setIcon(bitmapDescriptorFromVector(cnt, item));
            } catch (Exception e) {
                Log.e("IS", e.getMessage());
            }
        }

        @Override
        protected void onClusterUpdated(@NonNull Cluster<CustomMarker> cluster, @NonNull Marker marker) {
            try {
                super.onClusterUpdated(cluster, marker);
            } catch (Exception e) {
                Log.e("IS", e.getMessage());
            }
        }

        private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes CustomMarker item) {
            double resize = 1.75;

            Drawable iconDrawable = ContextCompat.getDrawable(context, item.getIcon());
            iconDrawable.setBounds(0, 0, (int) (iconDrawable.getIntrinsicWidth() / resize), (int) (iconDrawable.getIntrinsicHeight() / resize));

            Bitmap bmp = Bitmap.createBitmap((int) (iconDrawable.getIntrinsicWidth() / resize), (int) (iconDrawable.getIntrinsicHeight() / resize), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);

            //TODO : dopsat filter na datum a ??as
            if (!item.isVisible()) {
                ColorMatrix cm = new ColorMatrix();
                cm.setSaturation(0);
                Paint paint = new Paint();
                ColorFilter filter = new ColorMatrixColorFilter(cm);
                paint.setColorFilter(filter);

                iconDrawable.setColorFilter(filter);
            }
            iconDrawable.draw(canvas);

            if (item.getmEntity() != null && item.getmEntity().size() > 1) {
                Paint whitePaint = new Paint();

                Rect clipBounds = canvas.getClipBounds();
                int cHeight = clipBounds.height();
                int cWidth = clipBounds.width();

                String text = String.valueOf(item.getmEntity().size());
                whitePaint.setColor(Color.WHITE);
                whitePaint.setTextSize(25);
                whitePaint.setTextAlign(Paint.Align.LEFT);
                whitePaint.getTextBounds(text, 0, text.length(), clipBounds);
                float x = cWidth / 2f - clipBounds.width() / 2f - clipBounds.left;
                float y = cHeight / 1.2f;
                canvas.drawText(text, x, y, whitePaint);
            }

            return BitmapDescriptorFactory.fromBitmap(bmp);
        }
    }
}
