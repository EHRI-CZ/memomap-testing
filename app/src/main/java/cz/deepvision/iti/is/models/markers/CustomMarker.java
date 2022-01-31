package cz.deepvision.iti.is.models.markers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.List;

public class CustomMarker<T> implements ClusterItem {
    private final LatLng mPosition;
    private List<T> mEntity;
    private String mSnippet;
    private boolean visible;
    public final int icon;

    public CustomMarker(double lat, double lng, int iconRes, boolean visible) {
        mPosition = new LatLng(lat, lng);
        mEntity = null;
        mSnippet = null;
        icon = iconRes;
        this.visible = visible;
    }

    public CustomMarker(double lat, double lng, List<T> entityList, String snippet, int iconRes, boolean visible) {
        mPosition = new LatLng(lat, lng);
        mEntity = entityList;
        mSnippet = snippet;
        icon = iconRes;
        this.visible = visible;
    }

    public LatLng getmPosition() {
        return mPosition;
    }

    public List<T> getmEntity() {
        return mEntity;
    }

    public void setmEntity(List<T> mEntity) {
        this.mEntity = mEntity;
    }

    public String getmSnippet() {
        return mSnippet;
    }

    public void setmSnippet(String mSnippet) {
        this.mSnippet = mSnippet;
    }

    public int getIcon() {
        return icon;
    }

    public boolean isVisible() {
        return visible;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return null;
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return null;
    }
}
