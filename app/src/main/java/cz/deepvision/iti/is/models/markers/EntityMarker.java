package cz.deepvision.iti.is.models.markers;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import static cz.deepvision.iti.is.graphql.EntitiesGeoLocationGroupQuery.Entity;

public class EntityMarker  extends CustomMarker<Entity>
    {
        private final LatLng mPosition;
        private List<Entity> mEntity;
        private String mSnippet;
        public final int icon;

//    public EntityMarker(double lat, double lng, int iconRes) {
//        super(lat,lng,iconRes);
//        mPosition = new LatLng(lat, lng);
//        mEntity = null;
//        mSnippet = null;
//        icon = iconRes;
//    }


    public EntityMarker(double lat, double lng, List<Entity> entityList, String snippet, int iconRes) {
        super(lat,lng,entityList,snippet,iconRes);
        mPosition = new LatLng(lat, lng);
        mEntity = entityList;
        mSnippet = snippet;
        icon = iconRes;
    }

        public List<Entity> getmEntity() {
            return mEntity;
        }

        @Override
        public LatLng getPosition() {
        return mPosition;
    }

        @Override
        public String getTitle() { return getWholeTitle(getmEntity()); }

        private String getWholeTitle(List<Entity> getmEntity) {
        String title = "";
            for (Entity entity : getmEntity) {
                title += " ";
                title += entity.label();
            }
            return title;
        }

        @Override
        public String getSnippet() { return mSnippet; }

        /**
         * Set the title of the marker
         * @param mEntity Single person entity
         */
        public void setmEntity(List<Entity> mEntity) {
            this.mEntity = mEntity;
        }

        /**
         * Set the description of the marker
         * @param snippet string to be set as snippet
         */
        public void setSnippet(String snippet) {
        mSnippet = snippet;
    }

}
