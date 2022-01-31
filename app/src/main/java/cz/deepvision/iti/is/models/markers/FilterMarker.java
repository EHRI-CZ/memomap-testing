package cz.deepvision.iti.is.models.markers;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import cz.deepvision.iti.is.graphql.EventsGeoLocationGroupQuery;

public class FilterMarker extends CustomMarker<String>
{
    private final LatLng mPosition;
    private List<String> mEvents;
    private List<String> mEventIds;
    private String type;
    private String mSnippet;
    public final int icon;

    public FilterMarker(double lat, double lng, List<String> entityList,List<String> entityIds, String snippet, int iconRes, boolean visible,String type) {
        super(lat,lng,entityList,snippet,iconRes,visible);
        mPosition = new LatLng(lat, lng);
        mEvents = entityList;
        mEventIds = entityIds;
        this.type = type;
        mSnippet = snippet;
        icon = iconRes;
    }

    public List<String> getmEvents() {
        return mEvents;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() { return getWholeTitle(getmEvents()); }

    private String getWholeTitle(List<String> getmEvents) {
        String title = "";
        if(getmEvents != null)
        {
            for (String event : getmEvents) {
                title += " ";
                title += event;
            }
        }
        return title;
    }

    @Override
    public String getSnippet() { return mSnippet; }

    /**
     * Set the title of the marker
     * @param mEvents Single person entity
     */
    public void setmEvents(List<String> mEvents) {
        this.mEvents = mEvents;
    }

    /**
     * Set the description of the marker
     * @param snippet string to be set as snippet
     */
    public void setSnippet(String snippet) {
        mSnippet = snippet;
    }

    public List<String> getmEventIds() {
        return mEventIds;
    }

    public void setmEventIds(List<String> mEventIds) {
        this.mEventIds = mEventIds;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
