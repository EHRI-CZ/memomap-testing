package cz.deepvision.iti.is.ui.home;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.Algorithm;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.CDATASection;

import java.util.ArrayList;
import java.util.List;

import cz.deepvision.iti.is.CustomClusterManager;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.graphql.EntitiesGeoLocationGroupQuery;
import cz.deepvision.iti.is.graphql.EntitiesGeoLocationGroupWithTransportsQuery;
import cz.deepvision.iti.is.graphql.EntitiesGeoLocationGroupWithTransportsQuery.EntitiesGeoLocationGroupWithTransport;
import cz.deepvision.iti.is.graphql.EntityDetailQuery;
import cz.deepvision.iti.is.graphql.EventDetailQuery;
import cz.deepvision.iti.is.graphql.EventsGeoLocationGroupQuery;
import cz.deepvision.iti.is.graphql.PlaceDetailQuery;
import cz.deepvision.iti.is.graphql.PlacesGeoLocationGroupQuery;
import cz.deepvision.iti.is.models.Event;
import cz.deepvision.iti.is.models.Place;
import cz.deepvision.iti.is.models.markers.CustomMarker;
import cz.deepvision.iti.is.models.markers.EntityMarker;
import cz.deepvision.iti.is.models.markers.EventMarker;
import cz.deepvision.iti.is.models.markers.PlaceMarker;
import cz.deepvision.iti.is.models.victims.ListViewItem;
import cz.deepvision.iti.is.models.victims.Person;
import cz.deepvision.iti.is.ui.dialog.EventDialog;
import cz.deepvision.iti.is.ui.dialog.ListDialog;
import cz.deepvision.iti.is.ui.dialog.PlaceDialog;
import cz.deepvision.iti.is.ui.dialog.VictimDialog;
import cz.deepvision.iti.is.util.NetworkConnection;

public class HomeViewModel extends AndroidViewModel implements CustomClusterManager.onCameraIdleExtension, LocationListener {

    private LocationManager mLocationManager;
    private MutableLiveData<String> mText;
    private CustomClusterManager<CustomMarker> mClusterManager;

    SupportMapFragment mapFragment = null;
    Fragment mFragment = null;
    private HomeViewModel thisModel = this;
    private GoogleMap mMap;
    private LatLng lastPossition = new LatLng(50.088780, 14.419094);
    private Boolean[] filters = new Boolean[]{true, true, true};
    private boolean loading = false;
    private int year = 0;
    private int month = 0;
    private List<CustomMarker> markerItems = new ArrayList<>();

    public HomeViewModel(@NonNull Application application, cz.deepvision.iti.is.models.Location position) {
        super(application);
        mText = new MutableLiveData<>();
        cz.deepvision.iti.is.models.Location location = position;
        mText.setValue("This is home fragment");
        if (mapFragment == null) {
            GoogleMapOptions options = new GoogleMapOptions();
            options.compassEnabled(false);
            options.tiltGesturesEnabled(false);
            options.rotateGesturesEnabled(false);


            mLocationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);

            mapFragment = SupportMapFragment.newInstance(options);

            mapFragment.getMapAsync(googleMap -> {
                LatLng latLng = new LatLng(50.088780, 14.419094);
                lastPossition = latLng;
                if (location != null) {
                    latLng = new LatLng(location.getLat(), location.getLng());
                    lastPossition = latLng;
                }
                mMap = googleMap;
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplication().getApplicationContext(), R.raw.map_style_dark));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPossition, 18));
                //Cluster
                mClusterManager = new CustomClusterManager<>(mFragment.getActivity(), googleMap);
                mClusterManager.setmOnCameraIdleExtension(thisModel);
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
                ListDialog listDialog = new ListDialog(listViewItems, getmFragment());
                listDialog.show(getMapFragment().getActivity().getSupportFragmentManager(), "dialog_list");
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
                ListDialog listDialog = new ListDialog(listViewItems, getmFragment());
                listDialog.show(getMapFragment().getActivity().getSupportFragmentManager(), "dialog_list");

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


    public void updateMarkers(final GoogleMap googleMap, LatLng location, float radius) {
        markerItems.clear();
        Boolean[] doneLoading = new Boolean[]{false, false, false};

        if (filters[0]) {
            //Entities groups
            NetworkConnection.getInstance().getApolloClient().query(new EntitiesGeoLocationGroupWithTransportsQuery(location.longitude, location.latitude, (int) radius)).enqueue(new ApolloCall.Callback<EntitiesGeoLocationGroupWithTransportsQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<EntitiesGeoLocationGroupWithTransportsQuery.Data> response) {
                    final List<EntitiesGeoLocationGroupWithTransport> entities = response.data().entitiesGeoLocationGroupWithTransports();
                    for (EntitiesGeoLocationGroupWithTransport entity : entities) {
                        boolean visible = entity.entities().stream().anyMatch(entity1 -> isValidDate(entity1.date()));

                        EntityMarker item = new EntityMarker(entity.location().lat(), entity.location().lon(), entity.entities(), null, R.drawable.ic_entity_map, visible);
                        markerItems.add(item);
                    }
                    doneLoading[0] = true;
                    finishLoadingMap(googleMap, doneLoading);
                }


                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());
                }
            });
        }

        if (filters[1]) {
            //Events
            NetworkConnection.getInstance().getApolloClient().query(new EventsGeoLocationGroupQuery(location.longitude, location.latitude, (int) radius)).enqueue(new ApolloCall.Callback<EventsGeoLocationGroupQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<EventsGeoLocationGroupQuery.Data> response) {
                    final List<EventsGeoLocationGroupQuery.EventsGeoLocationGroup> events = response.data().eventsGeoLocationGroup();
                    for (EventsGeoLocationGroupQuery.EventsGeoLocationGroup event : events) {
                        EventMarker item = new EventMarker(event.location().lat(), event.location().lon(), event.events(), null, R.drawable.ic_event_map, true);
                        markerItems.add(item);
                    }
                    doneLoading[1] = true;
                    finishLoadingMap(googleMap, doneLoading);

                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());
                }
            });
        }

        if (filters[2]) {
            //Places
            NetworkConnection.getInstance().getApolloClient().query(new PlacesGeoLocationGroupQuery(location.longitude, location.latitude, (int) radius)).enqueue(new ApolloCall.Callback<PlacesGeoLocationGroupQuery.Data>() {
                @Override
                public void onResponse(@NotNull final Response<PlacesGeoLocationGroupQuery.Data> response) {
                    final List<PlacesGeoLocationGroupQuery.PlacesGeoLocationGroup> places = response.data().placesGeoLocationGroup();
                    for (PlacesGeoLocationGroupQuery.PlacesGeoLocationGroup place : places) {
                        PlaceMarker item = new PlaceMarker(place.location().lat(), place.location().lon(), place.places(), null, R.drawable.ic_place_map, true);
                        markerItems.add(item);
                    }
                    doneLoading[2] = true;
                    finishLoadingMap(googleMap, doneLoading);
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());
                }
            });
        }
    }

    private boolean isValidDate(String date) {
        if (year == 0 && month == 0) return true;
        final String[] dates = date.split("\\.");
        if (Integer.valueOf(dates[2]) < year) return true;
        if (Integer.valueOf(dates[2]) <= year && Integer.valueOf(dates[1]) <= month) return true;
        else return false;

    }

    private void updateMap(GoogleMap googleMap, List<CustomMarker> markerItems) {
        if (mFragment != null && mFragment.getActivity() != null) {
            mFragment.getActivity().runOnUiThread(() -> {
                try {
                    googleMap.clear();
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

    private void finishLoadingMap(GoogleMap googleMap, Boolean[] doneLoading) {
        for (int i = 0; i < filters.length; i++) {
            Log.e("Filters", "position: " + i + " " + filters[i]);
        }
        for (int i = 0; i < doneLoading.length; i++) {
            Log.e("Filters loading", "position: " + i + " " + doneLoading[i]);
        }
        if (filters[0] == doneLoading[0] && filters[1] == doneLoading[1] && filters[2] == doneLoading[2] && !loading) {
            loading = true;
            updateMap(googleMap, markerItems);
        }
    }

    // TODO: posun na mapě
    @Override
    public void onCameraIdle() {
        updatePosition();
    }

    public void updatePosition() {
        Log.d("IS", "CAMERA IDLE");
        float zoom = mMap.getCameraPosition().zoom;
        LatLng current = mMap.getCameraPosition().target;
        float radius = mClusterManager.getRadius(mMap);
        if (!lastPossition.equals(current)) {
            loading = false;
            Log.d("IS", "POSITION CHANGED " + lastPossition + " new " + current);
            updateMarkers(mMap, current, radius);
            lastPossition = current;
        }
    }

    public void updateFilters(Boolean[] filters) {
        this.filters = filters;
        LatLng current = mMap.getCameraPosition().target;
        float radius = mClusterManager.getRadius(mMap);
        updateMarkers(mMap, current, radius);
        lastPossition = current;
    }

    public void updateLocalPosition(Location location) {
        if (mMap != null) {
            Log.d("IS", "User location");
            float zoom = mMap.getCameraPosition().zoom;
            LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
            float radius = mClusterManager.getRadius(mMap);
            if (!lastPossition.equals(current)) {
                loading = false;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPossition, 18));
                Log.d("IS", "POSITION CHANGED " + lastPossition + " new " + current);
                updateMarkers(mMap, current, radius);
                lastPossition = current;
            }
        }
    }

    public void updateDatePosition() {
        if (year != 0 && month != 0) {
            Log.d("IS", "CAMERA IDLE");
            float zoom = mMap.getCameraPosition().zoom;
            LatLng current = mMap.getCameraPosition().target;
            float radius = mClusterManager.getRadius(mMap);
            Log.d("IS", "POSITION CHANGED " + lastPossition + " new " + current);
            updateMarkers(mMap, current, radius);
            lastPossition = current;
        }
    }

    public void updateCurrentPosition(boolean gpsEnabled) {
        boolean isGPSProviderEabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Criteria crta = new Criteria();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            crta.setAccuracy(Criteria.ACCURACY_FINE);
        } else {
            crta.setAccuracy(Criteria.ACCURACY_MEDIUM);
        }
        crta.setPowerRequirement(Criteria.POWER_LOW);
        String provider = mLocationManager.getBestProvider(crta, true);
        if ((isGPSProviderEabled || isNetworkEnabled) && gpsEnabled) {
            if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.requestLocationUpdates(provider, (10 * 60 * 1000), 10, this);
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