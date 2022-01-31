package cz.deepvision.iti.is.models.markers;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import cz.deepvision.iti.is.graphql.PlacesGeoLocationGroupQuery.Place;

public class PlaceMarker extends CustomMarker<Place> {

    private final LatLng mPosition;
    private List<Place> mEntity;
    private String mSnippet;
    public final int icon;


    public PlaceMarker(double lat, double lng, List<Place> entityList, String snippet, int iconRes, boolean visible) {
        super(lat, lng, iconRes, visible);
        mPosition = new LatLng(lat, lng);
        mEntity = entityList;
        mSnippet = snippet;
        icon = iconRes;
    }

    public List<Place> getmEntity() {
        return mEntity;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return getWholeTitle(getmEntity());
    }

    private String getWholeTitle(List<Place> getmEntity) {
        String title = "";
        if (getmEntity != null) {
            for (Place entity : getmEntity) {
                title += " ";
                title += entity.label();
            }
        }
        return title;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    /**
     * Set the title of the marker
     *
     * @param mEntity Single person entity
     */
    public void setmEntity(List<Place> mEntity) {
        this.mEntity = mEntity;
    }

    /**
     * Set the description of the marker
     *
     * @param snippet string to be set as snippet
     */
    public void setSnippet(String snippet) {
        mSnippet = snippet;
    }

}
