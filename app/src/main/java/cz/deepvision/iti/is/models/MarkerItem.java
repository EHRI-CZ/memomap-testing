package cz.deepvision.iti.is.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MarkerItem implements ClusterItem

    {
        private final LatLng mPosition;
        private String mTitle;
        private String mSnippet;
        public final int icon;

    public MarkerItem(double lat, double lng, int iconRes) {
        mPosition = new LatLng(lat, lng);
        mTitle = null;
        mSnippet = null;
        icon = iconRes;
    }

    public MarkerItem(double lat, double lng, String title, String snippet, int iconRes) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
        icon = iconRes;
    }

        @Override
        public LatLng getPosition() {
        return mPosition;
    }

        @Override
        public String getTitle() { return mTitle; }

        @Override
        public String getSnippet() { return mSnippet; }

        /**
         * Set the title of the marker
         * @param title string to be set as title
         */
        public void setTitle(String title) {
        mTitle = title;
    }

        /**
         * Set the description of the marker
         * @param snippet string to be set as snippet
         */
        public void setSnippet(String snippet) {
        mSnippet = snippet;
    }
}
