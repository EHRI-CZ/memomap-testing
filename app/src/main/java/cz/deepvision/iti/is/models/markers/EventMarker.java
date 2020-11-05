package cz.deepvision.iti.is.models.markers;

import com.google.android.gms.maps.model.LatLng;
import cz.deepvision.iti.is.graphql.EventsGeoLocationGroupQuery;

import java.util.List;

public class EventMarker extends CustomMarker<EventsGeoLocationGroupQuery.Event>
{
    private final LatLng mPosition;
    private List<EventsGeoLocationGroupQuery.Event> mEvents;
    private String mSnippet;
    public final int icon;

//    public EventMarker(double lat, double lng, int iconRes) {
//        super(lat,lng,iconRes);
//        mPosition = new LatLng(lat, lng);
//        mEvents = null;
//        mSnippet = null;
//        icon = iconRes;
//    }


    public EventMarker(double lat, double lng, List<EventsGeoLocationGroupQuery.Event> entityList, String snippet, int iconRes, boolean visible) {
        super(lat,lng,entityList,snippet,iconRes,visible);
        mPosition = new LatLng(lat, lng);
        mEvents = entityList;
        mSnippet = snippet;
        icon = iconRes;
    }

    public List<EventsGeoLocationGroupQuery.Event> getmEvents() {
        return mEvents;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() { return getWholeTitle(getmEvents()); }

    private String getWholeTitle(List<EventsGeoLocationGroupQuery.Event> getmEvents) {
        String title = "";
        for (EventsGeoLocationGroupQuery.Event event : getmEvents) {
            title += " ";
            title += event.label();
        }
        return title;
    }

    @Override
    public String getSnippet() { return mSnippet; }

    /**
     * Set the title of the marker
     * @param mEvents Single person entity
     */
    public void setmEvents(List<EventsGeoLocationGroupQuery.Event> mEvents) {
        this.mEvents = mEvents;
    }

    /**
     * Set the description of the marker
     * @param snippet string to be set as snippet
     */
    public void setSnippet(String snippet) {
        mSnippet = snippet;
    }

}
