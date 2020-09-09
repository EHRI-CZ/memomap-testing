package cz.deepvision.iti.is;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.solver.widgets.Rectangle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.w3c.dom.Text;

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
        //Toast.makeText(mapFragment.getContext(),"Current zoom:"+zoom+" location:"+current + " radius:"+radius,Toast.LENGTH_LONG).show();
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
                if (marker.getTag() != null)
                    marker.setIcon(bitmapDescriptorFromVector(cnt, item));
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
            double resize = 1.5;

            Drawable iconDrawable = ContextCompat.getDrawable(context, item.getIcon());
            iconDrawable.setBounds(50, 50, (int) (iconDrawable.getIntrinsicWidth() / resize), (int) (iconDrawable.getIntrinsicHeight() / resize));
            Bitmap icon = Bitmap.createBitmap((int) (iconDrawable.getIntrinsicWidth() / resize), (int) (iconDrawable.getIntrinsicHeight() / resize), Bitmap.Config.ARGB_8888);

            Bitmap bmp = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bmp);

            Drawable bubbleDrawable = ContextCompat.getDrawable(ctx, R.drawable.ic_baseline_chat_bubble_96);
            bubbleDrawable.setBounds(0, 0, bubbleDrawable.getIntrinsicWidth() / 2, bubbleDrawable.getIntrinsicHeight() / 2);
            Bitmap bubble = Bitmap.createBitmap(bubbleDrawable.getIntrinsicWidth() / 2, bubbleDrawable.getIntrinsicHeight() / 2, Bitmap.Config.ARGB_8888);

            canvas.translate(0,iconDrawable.getIntrinsicHeight());
            iconDrawable.draw(canvas);

            if (item.getmEntity().size() > 1) {
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setTextSize(35);
                canvas.translate(iconDrawable.getIntrinsicWidth()/2, -bubbleDrawable.getIntrinsicHeight());
                bubbleDrawable.draw(canvas);
                canvas.translate(80, 90);

                canvas.drawText(String.valueOf(item.getmEntity().size()), 0, 0, paint);
            }
            return BitmapDescriptorFactory.fromBitmap(bmp);
        }

        private void drawBubble(Canvas canvas, int size) {

            /*Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            canvas.drawBitmap(bitmap,0,0,paint);
            String text = String.valueOf(size);

            Paint textPaint = new Paint();
            textPaint.setTextSize(30);
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setColor(Color.BLACK);

            Rect r = new Rect();
            textPaint.getTextBounds(text, 0, text.length(), r);

            r = canvas.getClipBounds();
            RectF bounds = new RectF(r);
            bounds.right = textPaint.measureText(text, 0, text.length());
            bounds.left += (r.width() - bounds.right) / 2.0f;
            canvas.drawText(text, bounds.left, 25, textPaint);*/

        }
    }
}
