package cz.deepvision.iti.is.models.markers;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import cz.deepvision.iti.is.graphql.EntitiesGeoLocationGroupWithTransportsQuery.Entity;


public class PositionMarker extends CustomMarker<Entity>
{
    private final LatLng mPosition;
    private List<Entity> mEntity;
    private String mSnippet;
    public final int icon;


    public PositionMarker(double lat, double lng, List<Entity> entityList, String snippet, int iconRes, boolean visible) {
        super(lat,lng,entityList,snippet,iconRes,visible);
        mPosition = new LatLng(lat, lng);
        mEntity = entityList;
        mSnippet = snippet;
        icon = iconRes;
    }

    @Override
    public List<Entity> getmEntity() {
        return mEntity;
    }

    @Override
    public void setmEntity(List<Entity> mEntity) {
        this.mEntity = mEntity;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() { return getWholeTitle(); }

    private String getWholeTitle() {
        return "Moje pozice";
    }

    @Override
    public String getSnippet() { return mSnippet; }

    /**
     * Set the title of the marker
     * @param mEntity Single person entity
     */

    /**
     * Set the description of the marker
     * @param snippet string to be set as snippet
     */
    public void setSnippet(String snippet) {
        mSnippet = snippet;
    }

}
