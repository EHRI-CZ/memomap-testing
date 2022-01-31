package cz.deepvision.iti.is.ui.home;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cz.deepvision.iti.is.BaseApp;
import cz.deepvision.iti.is.CustomClusterManager;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.graphql.EntitiesGeoLocationGroupWithTransportsQuery;
import cz.deepvision.iti.is.graphql.EntitiesGeoLocationGroupWithTransportsQuery.EntitiesGeoLocationGroupWithTransport;
import cz.deepvision.iti.is.graphql.EntityDetailQuery;
import cz.deepvision.iti.is.graphql.EventDetailQuery;
import cz.deepvision.iti.is.graphql.EventsGeoLocationGroupQuery;
import cz.deepvision.iti.is.graphql.FilterAllQuery;
import cz.deepvision.iti.is.graphql.FilterEntitiesEventsQuery;
import cz.deepvision.iti.is.graphql.FilterEntitiesPlacesQuery;
import cz.deepvision.iti.is.graphql.FilterPlacesEventsQuery;
import cz.deepvision.iti.is.graphql.PlaceDetailQuery;
import cz.deepvision.iti.is.graphql.PlacesGeoLocationGroupQuery;
import cz.deepvision.iti.is.models.Event;
import cz.deepvision.iti.is.models.Place;
import cz.deepvision.iti.is.models.markers.CustomMarker;
import cz.deepvision.iti.is.models.markers.EntityMarker;
import cz.deepvision.iti.is.models.markers.EventMarker;
import cz.deepvision.iti.is.models.markers.FilterMarker;
import cz.deepvision.iti.is.models.markers.PlaceMarker;
import cz.deepvision.iti.is.models.markers.PositionMarker;
import cz.deepvision.iti.is.models.victims.ListViewItem;
import cz.deepvision.iti.is.models.victims.Person;
import cz.deepvision.iti.is.ui.dialog.EventDialog;
import cz.deepvision.iti.is.ui.dialog.PlaceDialog;
import cz.deepvision.iti.is.ui.dialog.SearchDialog;
import cz.deepvision.iti.is.ui.dialog.VictimDialog;
import cz.deepvision.iti.is.util.NetworkConnection;

public class HomeViewModel extends AndroidViewModel implements CustomClusterManager.onCameraIdleExtension, LocationListener {

    private LocationManager mLocationManager;
    private final MutableLiveData<String> mText;
    private CustomClusterManager<CustomMarker> mClusterManager;

    SupportMapFragment mapFragment = null;
    Fragment mFragment = null;
    private GoogleMap mMap;
    private boolean loading = false;
    private int year = 1939;
    private int month = 3;
    private List<CustomMarker> markerItems = new ArrayList<>();

    public HomeViewModel(@NonNull Application application, cz.deepvision.iti.is.models.Location position) {
        super(application);
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
        if (mapFragment == null) {
            GoogleMapOptions options = new GoogleMapOptions();
            options.compassEnabled(false);
            options.tiltGesturesEnabled(false);
            options.rotateGesturesEnabled(false);

            mLocationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
            mapFragment = SupportMapFragment.newInstance(options);

            mapFragment.getMapAsync(googleMap -> {
                if (position != null)
                    BaseApp.setGpsLocation(new LatLng(position.getLat(), position.getLng()));

                mMap = googleMap;
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplication().getApplicationContext(), R.raw.map_style_light));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(BaseApp.getGpsLocation(), 18));
                //Cluster
                mClusterManager = new CustomClusterManager<>(mFragment.getActivity(), googleMap);
                mClusterManager.setmOnCameraIdleExtension(this);
                mClusterManager.setOnClusterItemClickListener(onClusterClickListener);
                googleMap.setOnCameraIdleListener(mClusterManager);

            });

        }
    }

    final ClusterManager.OnClusterItemClickListener onClusterClickListener = new ClusterManager.OnClusterItemClickListener() {
        @Override
        public boolean onClusterItemClick(ClusterItem item) {
            if (item != null) {
                if (item instanceof EventMarker && ((EventMarker) item).isVisible()) {
                    previewEvent((EventMarker) item);
                } else if (item instanceof EntityMarker && ((EntityMarker) item).isVisible()) {
                    previewEntity((EntityMarker) item);
                } else if (item instanceof PlaceMarker && ((PlaceMarker) item).isVisible()) {
                    previewPlace((PlaceMarker) item);
                } else if (item instanceof FilterMarker && ((FilterMarker) item).isVisible()) {
                    previewFilter((FilterMarker) item);
                }
            }
            return false;
        }

        private void previewPlace(PlaceMarker selected) {
            NetworkConnection.getInstance().getApolloClient().query(new PlaceDetailQuery(selected.getmEntity().get(0).id())).enqueue(new ApolloCall.Callback<PlaceDetailQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<PlaceDetailQuery.Data> response) {
                    if (response.data() != null && response.data().placeDetail() != null) {
                        PlaceDetailQuery.PlaceDetail responseData = response.data().placeDetail();
                        Place data = new Place(responseData);

                        PlaceDialog placeDialog = new PlaceDialog(getmFragment(), data, true, 2);
                        placeDialog.show(getMapFragment().getActivity().getSupportFragmentManager(), "dialog");

                    }
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());
                }
            });
        }

        private void previewEvent(EventMarker selected) {
            if (selected.getmEvents().size() > 1) {
                List<ListViewItem> listViewItems = new ArrayList<>();
                for (EventsGeoLocationGroupQuery.Event event : selected.getmEvents()) {
                    listViewItems.add(new ListViewItem(event.id(), event.label(), "event"));
                }
                SearchDialog searchDialog = new SearchDialog(listViewItems, getmFragment());
                searchDialog.show(getMapFragment().getActivity().getSupportFragmentManager(), "dialog_list");
            } else {
                NetworkConnection.getInstance().getApolloClient().query(new EventDetailQuery(selected.getmEntity().get(0).id())).enqueue(new ApolloCall.Callback<EventDetailQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<EventDetailQuery.Data> response) {
                        if (response.data() != null && response.data().eventDetail() != null) {
                            EventDetailQuery.EventDetail responseData = response.data().eventDetail();
                            Event data = new Event(responseData);

                            EventDialog eventDialog = new EventDialog(getmFragment(), data, true, 2);
                            eventDialog.show(getMapFragment().getActivity().getSupportFragmentManager(), "dialog");
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.e("IS", e.getMessage());
                    }
                });
            }
        }

        private void previewEntity(EntityMarker selected) {
            if (selected.getmEntity().size() > 1) {
                List<ListViewItem> listViewItems = new ArrayList<>();
                for (EntitiesGeoLocationGroupWithTransportsQuery.Entity entity : selected.getmEntity()) {
                    listViewItems.add(new ListViewItem(entity.id(), entity.label(), "entity"));
                }
                SearchDialog searchDialog = new SearchDialog(listViewItems, getmFragment());
                searchDialog.show(getMapFragment().getActivity().getSupportFragmentManager(), "dialog_list");

            } else {
                NetworkConnection.getInstance().getApolloClient().query(new EntityDetailQuery(selected.getmEntity().get(0).id())).enqueue(new ApolloCall.Callback<EntityDetailQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<EntityDetailQuery.Data> response) {
                        if (response.data() != null && response.data().entityDetail() != null) {
                            EntityDetailQuery.EntityDetail responseData = response.data().entityDetail();
                            Person data = new Person(responseData);

                            VictimDialog victimDialog = new VictimDialog(getmFragment(), data, true, 2);
                            victimDialog.show(getMapFragment().getActivity().getSupportFragmentManager(), "dialog");

                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.e("IS", e.getMessage());
                    }
                });
            }
        }
    };

    private void previewFilter(FilterMarker marker) {
        if (marker.getmEvents().size() > 1) {
            List<ListViewItem> listViewItems = new ArrayList<>();
            for (int i = 0; i < marker.getmEvents().size(); i++) {
                listViewItems.add(new ListViewItem(marker.getmEventIds().get(i), marker.getmEvents().get(i), marker.getType()));
            }
            SearchDialog searchDialog = new SearchDialog(listViewItems, getmFragment());
            searchDialog.show(getMapFragment().getActivity().getSupportFragmentManager(), "dialog_list");

        } else {
            switch (marker.getType()) {
                case "entity": {
                    NetworkConnection.getInstance().getApolloClient().query(new EntityDetailQuery(marker.getmEventIds().get(0))).enqueue(new ApolloCall.Callback<EntityDetailQuery.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<EntityDetailQuery.Data> response) {
                            if (response.data() != null && response.data().entityDetail() != null) {
                                EntityDetailQuery.EntityDetail responseData = response.data().entityDetail();
                                Person data = new Person(responseData);

                                VictimDialog victimDialog = new VictimDialog(getmFragment(), data, true, 2);
                                victimDialog.show(getMapFragment().getActivity().getSupportFragmentManager(), "dialog");

                            }
                        }

                        @Override
                        public void onFailure(@NotNull ApolloException e) {
                            Log.e("IS", e.getMessage());
                        }
                    });
                    break;
                }
                case "event": {
                    NetworkConnection.getInstance().getApolloClient().query(new EventDetailQuery(marker.getmEventIds().get(0))).enqueue(new ApolloCall.Callback<EventDetailQuery.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<EventDetailQuery.Data> response) {
                            if (response.data() != null && response.data().eventDetail() != null) {
                                EventDetailQuery.EventDetail responseData = response.data().eventDetail();
                                Event data = new Event(responseData);

                                EventDialog eventDialog = new EventDialog(getmFragment(), data, true, 2);
                                eventDialog.show(getMapFragment().getActivity().getSupportFragmentManager(), "dialog");
                            }
                        }

                        @Override
                        public void onFailure(@NotNull ApolloException e) {
                            Log.e("IS", e.getMessage());
                        }
                    });
                    break;
                }

                case "place": {
                    NetworkConnection.getInstance().getApolloClient().query(new PlaceDetailQuery(marker.getmEventIds().get(0))).enqueue(new ApolloCall.Callback<PlaceDetailQuery.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<PlaceDetailQuery.Data> response) {
                            if (response.data() != null && response.data().placeDetail() != null) {
                                PlaceDetailQuery.PlaceDetail responseData = response.data().placeDetail();
                                Place data = new Place(responseData);

                                PlaceDialog placeDialog = new PlaceDialog(getmFragment(), data, true, 2);
                                placeDialog.show(getMapFragment().getActivity().getSupportFragmentManager(), "dialog");

                            }
                        }

                        @Override
                        public void onFailure(@NotNull ApolloException e) {
                            Log.e("IS", e.getMessage());
                        }
                    });
                }
                break;
            }
        }
    }


    public LiveData<String> getText() {
        return mText;
    }

    public SupportMapFragment getMapFragment() {
        return mapFragment;
    }

    public void setMapFragment(SupportMapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    public Fragment getmFragment() {
        return mFragment;
    }

    public void setmFragment(Fragment mFragment) {
        this.mFragment = mFragment;
    }


    public void updateMarkers(LatLng location, float radius) {
        markerItems.clear();

        Boolean[] filters = BaseApp.getFilters();
        // Pouze oběti
        if (filters[0] && !filters[1] && !filters[2]) {
            NetworkConnection.getInstance().getApolloClient().query(new EntitiesGeoLocationGroupWithTransportsQuery(location.longitude, location.latitude, (int) radius)).enqueue(new ApolloCall.Callback<EntitiesGeoLocationGroupWithTransportsQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<EntitiesGeoLocationGroupWithTransportsQuery.Data> response) {
                    final List<EntitiesGeoLocationGroupWithTransport> entities = response.data().entitiesGeoLocationGroupWithTransports();
                    for (EntitiesGeoLocationGroupWithTransport entity : entities) {
                        boolean visible = entity.entities().stream().anyMatch(entity1 -> isValidDate(entity1.date()));

                        EntityMarker item = new EntityMarker(entity.location().lat(), entity.location().lon(), entity.entities(), null, R.drawable.ic_mapa_lide_stav1, visible);
                        markerItems.add(item);
                    }
                    finishLoadingMap();
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());
                }
            });
        }// Pouze eventy
        else if (!filters[0] && filters[1] && !filters[2]) {
            NetworkConnection.getInstance().getApolloClient().query(new EventsGeoLocationGroupQuery(location.longitude, location.latitude, (int) radius)).enqueue(new ApolloCall.Callback<EventsGeoLocationGroupQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<EventsGeoLocationGroupQuery.Data> response) {
                    final List<EventsGeoLocationGroupQuery.EventsGeoLocationGroup> events = response.data().eventsGeoLocationGroup();
                    for (EventsGeoLocationGroupQuery.EventsGeoLocationGroup event : events) {
                        EventMarker item = new EventMarker(event.location().lat(), event.location().lon(), event.events(), null, R.drawable.ic_place_map, true);
                        markerItems.add(item);
                    }
                    finishLoadingMap();

                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());
                }
            });

        }// Pouze místa
        else if (!filters[0] && !filters[1] && filters[2]) {
            NetworkConnection.getInstance().getApolloClient().query(new PlacesGeoLocationGroupQuery(location.longitude, location.latitude, (int) radius)).enqueue(new ApolloCall.Callback<PlacesGeoLocationGroupQuery.Data>() {
                @Override
                public void onResponse(@NotNull final Response<PlacesGeoLocationGroupQuery.Data> response) {
                    final List<PlacesGeoLocationGroupQuery.PlacesGeoLocationGroup> places = response.data().placesGeoLocationGroup();
                    for (PlacesGeoLocationGroupQuery.PlacesGeoLocationGroup place : places) {
                        PlaceMarker item = new PlaceMarker(place.location().lat(), place.location().lon(), place.places(), null, R.drawable.ic_event_map, true);
                        markerItems.add(item);
                    }
                    finishLoadingMap();
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());
                }
            });

        }// Pouze oběti a eventy
        else if (filters[0] && filters[1] && !filters[2]) {
            NetworkConnection.getInstance().getApolloClient().query(new FilterEntitiesEventsQuery(location.longitude, location.latitude, (int) radius)).enqueue(new ApolloCall.Callback<FilterEntitiesEventsQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<FilterEntitiesEventsQuery.Data> response) {

                    List<FilterEntitiesEventsQuery.Entity> entities = Objects.requireNonNull(response.data().filterEntitiesEvents()).entities();
                    List<FilterEntitiesEventsQuery.Event> events = Objects.requireNonNull(response.data().filterEntitiesEvents()).events();

                    for (FilterEntitiesEventsQuery.Entity entity : entities) {
                        FilterMarker item = new FilterMarker(entity.location().lat(), entity.location().lon(), entity.labels(), entity.ids(), null, R.drawable.ic_mapa_lide_stav1, true, entity.type());
                        markerItems.add(item);
                    }
                    for (FilterEntitiesEventsQuery.Event event : events) {
                        FilterMarker item = new FilterMarker(event.location().lat(), event.location().lon(), event.labels(), event.ids(), null, R.drawable.ic_event_map, true, event.type());
                        markerItems.add(item);
                    }

                    finishLoadingMap();

                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());
                }
            });

        }// Pouze oběti a místa
        else if (filters[0] && !filters[1] && filters[2]) {
            NetworkConnection.getInstance().getApolloClient().query(new FilterEntitiesPlacesQuery(location.longitude, location.latitude, (int) radius)).enqueue(new ApolloCall.Callback<FilterEntitiesPlacesQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<FilterEntitiesPlacesQuery.Data> response) {

                    List<FilterEntitiesPlacesQuery.Entity> entities = Objects.requireNonNull(response.data().filterEntitiesPlaces()).entities();
                    List<FilterEntitiesPlacesQuery.Place> places = Objects.requireNonNull(response.data().filterEntitiesPlaces()).places();

                    for (FilterEntitiesPlacesQuery.Entity entity : entities) {
                        FilterMarker item = new FilterMarker(entity.location().lat(), entity.location().lon(), entity.labels(), entity.ids(), null, R.drawable.ic_mapa_lide_stav1, true, entity.type());
                        markerItems.add(item);
                    }

                    for (FilterEntitiesPlacesQuery.Place place : places) {
                        FilterMarker item = new FilterMarker(place.location().lat(), place.location().lon(), place.labels(), place.ids(), null, R.drawable.ic_place_map, true, place.type());
                        markerItems.add(item);
                    }

                    finishLoadingMap();

                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());
                }
            });

        } // Pouze místa a eventy
        else if (!filters[0] && filters[1] && filters[2]) {
            NetworkConnection.getInstance().getApolloClient().query(new FilterPlacesEventsQuery(location.longitude, location.latitude, (int) radius)).enqueue(new ApolloCall.Callback<FilterPlacesEventsQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<FilterPlacesEventsQuery.Data> response) {

                    List<FilterPlacesEventsQuery.Event> events = Objects.requireNonNull(response.data().filterPlacesEvents()).events();
                    List<FilterPlacesEventsQuery.Place> places = Objects.requireNonNull(response.data().filterPlacesEvents()).places();

                    for (FilterPlacesEventsQuery.Event event : events) {
                        FilterMarker item = new FilterMarker(event.location().lat(), event.location().lon(), event.labels(), event.ids(), null, R.drawable.ic_event_map, true, event.type());
                        markerItems.add(item);
                    }

                    for (FilterPlacesEventsQuery.Place place : places) {
                        FilterMarker item = new FilterMarker(place.location().lat(), place.location().lon(), place.labels(), place.ids(), null, R.drawable.ic_place_map, true, place.type());
                        markerItems.add(item);
                    }

                    finishLoadingMap();

                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());
                }
            });

        } //Všechno dohromady
        else if (filters[0] && filters[1] && filters[2]) {
            NetworkConnection.getInstance().getApolloClient().query(new FilterAllQuery(location.longitude, location.latitude, (int) radius)).enqueue(new ApolloCall.Callback<FilterAllQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<FilterAllQuery.Data> response) {

                    List<FilterAllQuery.Entity> entities = Objects.requireNonNull(response.data().filterAll()).entities();
                    List<FilterAllQuery.Event> events = Objects.requireNonNull(response.data().filterAll()).events();
                    List<FilterAllQuery.Place> places = Objects.requireNonNull(response.data().filterAll()).places();

                    for (FilterAllQuery.Entity entity : entities) {
                        FilterMarker item = new FilterMarker(entity.location().lat(), entity.location().lon(), entity.labels(), entity.ids(), null, R.drawable.ic_mapa_lide_stav1, true, entity.type());
                        markerItems.add(item);
                    }
                    for (FilterAllQuery.Event event : events) {
                        FilterMarker item = new FilterMarker(event.location().lat(), event.location().lon(), event.labels(), event.ids(), null, R.drawable.ic_event_map, true, event.type());
                        markerItems.add(item);
                    }

                    for (FilterAllQuery.Place place : places) {
                        FilterMarker item = new FilterMarker(place.location().lat(), place.location().lon(), place.labels(), place.ids(), null, R.drawable.ic_place_map, true, place.type());
                        markerItems.add(item);
                    }

                    finishLoadingMap();

                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());
                }
            });
        } else {
            finishLoadingMap();
        }
    }

    private boolean isValidDate(String date) {
        if (!BaseApp.isTimeLapseEnabled()) return true;

        final String[] dates = date.split("\\.");
        if (Integer.parseInt(dates[2]) == year && Integer.parseInt(dates[1]) <= month && Integer.parseInt(dates[1]) >= month - 3)
            return true;
        else return false;

    }

    private void updateMap(List<CustomMarker> markerItems) {
        if (mFragment != null && mFragment.getActivity() != null) {
            mFragment.getActivity().runOnUiThread(() -> {
                try {
                    mMap.clear();
                    mClusterManager.clearItems();
                    mClusterManager.addItems(markerItems);
                    mClusterManager.cluster();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    loading = false;
                }
            });
        }
    }

    private void finishLoadingMap() {
        if (!loading) {
            loading = true;
            markerItems.add(new PositionMarker(BaseApp.getGpsLocation().latitude, BaseApp.getGpsLocation().longitude,
                    null, null,
                    R.drawable.ic_entity_map, true));
            updateMap(markerItems);
        }
    }

    // TODO: posun na mapě
    @Override
    public void onCameraIdle() {
        updatePosition();
    }

    public void updatePosition() {
        Log.d("IS", "CAMERA IDLE");
        LatLng current = mMap.getCameraPosition().target;
        float radius = mClusterManager.getRadius(mMap);
        if (!BaseApp.getGpsLocation().equals(current)) {
            loading = false;
            Log.d("IS", "POSITION CHANGED " + BaseApp.getGpsLocation() + " new " + current);
            updateMarkers(current, radius);
        }
    }

    public void updateFilters(Boolean[] filters) {
        BaseApp.setFilters(filters);
        LatLng current = mMap.getCameraPosition().target;
        float radius = mClusterManager.getRadius(mMap);
        updateMarkers(current, radius);
        BaseApp.setGpsLocation(current);
    }

    public void updateLocalPosition(Location location) {
        if (mMap != null) {
            Log.d("IS", "User location");
            LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
            float radius = mClusterManager.getRadius(mMap);
            if (!BaseApp.getGpsLocation().equals(current)) {
                loading = false;
                BaseApp.setGpsLocation(current);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(BaseApp.getGpsLocation(), 18));
                Log.d("IS", "POSITION CHANGED " + BaseApp.getGpsLocation() + " new " + current);
                updateMarkers(current, radius);
            }
        }
    }

    public void updateDatePosition() {
        if (BaseApp.isTimeLapseEnabled()) {
            Log.d("IS", "CAMERA IDLE");
            LatLng current = mMap.getCameraPosition().target;
            float radius = mClusterManager.getRadius(mMap);
            Log.d("IS", "POSITION CHANGED " + BaseApp.getGpsLocation() + " new " + current);
            updateMarkers(current, radius);
        }
    }

    public void updateCurrentPosition(boolean gpsEnabled) {
        boolean isGPSProviderEabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Criteria crta = new Criteria();
        crta.setAccuracy(Criteria.ACCURACY_FINE);
        crta.setPowerRequirement(Criteria.POWER_LOW);
        String provider = mLocationManager.getBestProvider(crta, true);
        if ((isGPSProviderEabled || isNetworkEnabled) && gpsEnabled) {
            if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.requestLocationUpdates(provider, (1000), 1, this);
        } else if (!gpsEnabled) stopUpdating();
    }

    private void stopUpdating() {
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(@NonNull android.location.Location location) {
        updateLocalPosition(location);
    }


    public void setYear(int year) {
        this.year = year;
        updateDatePosition();
    }

    public void setMonth(int month) {
        this.month = month;
        updateDatePosition();
    }
}