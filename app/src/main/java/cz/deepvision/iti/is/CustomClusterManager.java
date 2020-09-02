package cz.deepvision.iti.is;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
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
        this.setRenderer(new CustomRenderer(context,map,this));
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
            markerOptions.icon(bitmapDescriptorFromVector(cnt,item.icon));
        }

        @Override
        protected void onClusterItemUpdated(@NonNull CustomMarker item, @NonNull Marker marker) {
            super.onClusterItemUpdated(item, marker);
            try {
                marker.setIcon(bitmapDescriptorFromVector(cnt,item.icon));
            }catch (Exception e){
                Log.e("IS",e.getMessage());
            }

        }

        private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
            int resize = 2;
            if(vectorDrawableResourceId == R.drawable.ic_entities_map){
                resize = 3;
            }
            Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth()/resize, vectorDrawable.getIntrinsicHeight()/resize);
            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth()/resize, vectorDrawable.getIntrinsicHeight()/resize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }
    }
}
