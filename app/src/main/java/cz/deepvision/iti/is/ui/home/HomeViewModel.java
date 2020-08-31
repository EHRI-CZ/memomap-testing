package cz.deepvision.iti.is.ui.home;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cz.deepvision.iti.is.CustomClusterManager;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.graphql.EntitiesGeoLocationGroupQuery;
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
import cz.deepvision.iti.is.ui.dialog.PlaceDialog;
import cz.deepvision.iti.is.ui.dialog.VictimDialog;

public class HomeViewModel extends AndroidViewModel implements CustomClusterManager.onCameraIdleExtension, LocationListener {

    private MutableLiveData<String> mText;
    private MutableLiveData<double[]> location;
    private CustomClusterManager<CustomMarker> mClusterManager;

    SupportMapFragment mapFragment = null;
    Fragment mFragment = null;
    private HomeViewModel thisModel = this;
    private GoogleMap mMap;
    private LatLng lastPossition = new LatLng(50.088780, 14.419094);
    private boolean isGPSEnabled = false;
    private LocationManager mLocationManager = null;
    private List<CustomMarker> markerItems;
    private Boolean[] filters = new Boolean[]{true, true, true};

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mText = new MutableLiveData<>();
        location = new MutableLiveData<>();
        mText.setValue("This is home fragment");
        if (mapFragment == null) {
            GoogleMapOptions options = new GoogleMapOptions();
            options.compassEnabled(false);
            options.tiltGesturesEnabled(false);
            options.rotateGesturesEnabled(false);

            mLocationManager = (LocationManager) application.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            mapFragment = SupportMapFragment.newInstance(options);

            /*if (isGPSEnabled){
                if (ActivityCompat.checkSelfPermission(application.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(application.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                            0, this);
                    return;
                }
            }*/

        }

        mapFragment.getMapAsync(googleMap -> {
            LatLng latLng = new LatLng(50.088780, 14.419094);
            lastPossition = latLng;
//            googleMap.addMarker(new MarkerOptions().position(latLng).title("ITI"));
            if (location.getValue() != null) {
                latLng = new LatLng(location.getValue()[0], location.getValue()[1]);
                lastPossition = latLng;
            }
            mMap = googleMap;
            googleMap.addMarker(new MarkerOptions().position(latLng).title("ITI"));
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplication().getApplicationContext(), R.raw.map_style_dark));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPossition, 18));
            //Cluster
            mClusterManager = new CustomClusterManager<>(mFragment.getActivity(), googleMap);
            mClusterManager.setmOnCameraIdleExtension(thisModel);
            mClusterManager.setOnClusterItemClickListener(onClusterClickListener);
            googleMap.setOnCameraIdleListener(mClusterManager);
            updateMarkers(googleMap, latLng, getRadius(googleMap));
/*mClusterManager.getMarkerCollection().setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
@Override
public View getInfoWindow(Marker marker) {
final LayoutInflater inflater = LayoutInflater.from(mFragment.getActivity());
final View view = inflater.inflate(R.layout.custom_info_window, null);
final TextView textView = view.findViewById(R.id.textViewTitle);
String text = (marker.getTitle() != null) ? marker.getTitle() : "Cluster Item";
textView.setText(text);
return view;
}

@Override
public View getInfoContents(Marker marker) {
return null;
}
});
mClusterManager.getMarkerCollection().setOnInfoWindowClickListener(marker ->
Toast.makeText(ClusteringDemoActivity.this,
"Info window clicked.",
Toast.LENGTH_SHORT).show());
*/
        });
    }

    final ClusterManager.OnClusterItemClickListener onClusterClickListener = new ClusterManager.OnClusterItemClickListener() {
        @Override
        public boolean onClusterItemClick(ClusterItem item) {
            CustomMarker selected = null;
            for (CustomMarker markerItem : markerItems) {
                if (markerItem.getTitle().equals(item.getTitle())) selected = markerItem;
            }
            if (selected != null) {
                if (selected instanceof EventMarker) {
                    previewEvent((EventMarker) selected);
                } else if (selected instanceof EntityMarker) {
                    previewEntity((EntityMarker) selected);
                } else {
                    previewPlace((PlaceMarker) selected);
                }
            }
            return false;
        }

        private void previewPlace(PlaceMarker selected) {
            PlaceDialog placeDialog = new PlaceDialog(getmFragment(), true);
            ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://77.236.207.194:8529/_db/ITI_DV/iti").build();
            apolloClient.query(new PlaceDetailQuery(selected.getmEntity().get(0).id())).enqueue(new ApolloCall.Callback<PlaceDetailQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<PlaceDetailQuery.Data> response) {
                    Looper.prepare();
                    if (response.data() != null && response.data().placeDetail() != null) {
                        PlaceDetailQuery.PlaceDetail responseData = response.data().placeDetail();
                        Place data = new Place(responseData);
                        getMapFragment().getActivity().runOnUiThread(() -> placeDialog.updateData(data));
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
                final BottomSheetDialog builder = new BottomSheetDialog(getmFragment().requireContext());
                View root = LayoutInflater.from(getmFragment().requireContext()).inflate(R.layout.custom_person_list, null);

                List<ListViewItem> listViewItems = new ArrayList<>();
                for (EventsGeoLocationGroupQuery.Event event : selected.getmEvents()) {
                    listViewItems.add(new ListViewItem(event.id(),event.label(),"event"));
                }
                LisViewAdapter homePersonAdapter = new LisViewAdapter(listViewItems, getmFragment());
                RecyclerView container = root.findViewById(R.id.person_list);
                container.addItemDecoration(new DividerItemDecoration(getmFragment().getActivity(), DividerItemDecoration.VERTICAL));

                container.setHasFixedSize(true);
                container.setLayoutManager(new LinearLayoutManager(getmFragment().getContext()));

                container.setAdapter(homePersonAdapter);
                builder.setContentView(root);

                if (!builder.isShowing()) builder.show();

                Button button = root.findViewById(R.id.btn_close_list);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        builder.dismiss();
                    }
                });
            } else {
                EventDialog eventDialog = new EventDialog(getmFragment(), true);
                ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://77.236.207.194:8529/_db/ITI_DV/iti").build();
                apolloClient.query(new EventDetailQuery(selected.getmEntity().get(0).id())).enqueue(new ApolloCall.Callback<EventDetailQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<EventDetailQuery.Data> response) {
                        Looper.prepare();
                        if (response.data() != null && response.data().eventDetail() != null) {
                            EventDetailQuery.EventDetail responseData = response.data().eventDetail();
                            Event data = new Event(responseData);
                            getMapFragment().getActivity().runOnUiThread(() -> eventDialog.updateData(data));
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
                final BottomSheetDialog builder = new BottomSheetDialog(getmFragment().requireContext());
                View root = LayoutInflater.from(getmFragment().requireContext()).inflate(R.layout.custom_person_list, null);

                List<ListViewItem> listViewItems = new ArrayList<>();
                for (EntitiesGeoLocationGroupQuery.Entity entity : selected.getmEntity()) {
                    listViewItems.add(new ListViewItem(entity.id(),entity.label(),"entity"));
                    
                }
                LisViewAdapter homePersonAdapter = new LisViewAdapter(listViewItems, getmFragment());
                RecyclerView container = root.findViewById(R.id.person_list);
                container.addItemDecoration(new DividerItemDecoration(getmFragment().getActivity(), DividerItemDecoration.VERTICAL));

                container.setHasFixedSize(true);
                container.setLayoutManager(new LinearLayoutManager(getmFragment().getContext()));

                container.setAdapter(homePersonAdapter);
                builder.setContentView(root);

                if (!builder.isShowing()) builder.show();

                Button button = root.findViewById(R.id.btn_close_list);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        builder.dismiss();
                    }
                });
            } else {
                VictimDialog victimDialog = new VictimDialog(getmFragment(), true);
                ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://77.236.207.194:8529/_db/ITI_DV/iti").build();
                apolloClient.query(new EntityDetailQuery(selected.getmEntity().get(0).id())).enqueue(new ApolloCall.Callback<EntityDetailQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<EntityDetailQuery.Data> response) {
                        Looper.prepare();
                        if (response.data() != null && response.data().entityDetail() != null) {
                            EntityDetailQuery.EntityDetail responseData = response.data().entityDetail();
                            Person data = new Person(responseData);
                            getMapFragment().getActivity().runOnUiThread(() -> victimDialog.updateData(data));
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

    public MutableLiveData<double[]> getLocation() {
        return location;
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

    /*private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth() / 2, vectorDrawable.getIntrinsicHeight() / 2);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth() / 2, vectorDrawable.getIntrinsicHeight() / 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }*/

    public void updateMarkers(final GoogleMap googleMap, LatLng location, float radius) {
        ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://77.236.207.194:8529/_db/ITI_DV/iti").build();
        markerItems = new ArrayList<>();

        if (filters[0]) {
            //Entities groups
            apolloClient.query(new EntitiesGeoLocationGroupQuery(location.longitude, location.latitude, (int) radius)).enqueue(new ApolloCall.Callback<EntitiesGeoLocationGroupQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<EntitiesGeoLocationGroupQuery.Data> response) {
                    final List<EntitiesGeoLocationGroupQuery.EntitiesGeoLocationGroup> entities = response.data().entitiesGeoLocationGroup();
                    //final BitmapDescriptor icon = bitmapDescriptorFromVector(mFragment.getContext(),R.drawable.ic_entity_map);
                    //final BitmapDescriptor iconGroup = bitmapDescriptorFromVector(mFragment.getContext(),R.drawable.ic_entities_map);//BitmapDescriptorFactory.fromResource(R.drawable.ic_entities_map);
                    for (EntitiesGeoLocationGroupQuery.EntitiesGeoLocationGroup entity : entities) {

                        //LatLng latLng = new LatLng(entity.location().lat(), entity.location().lon());
                        EntityMarker item = null;
                        if (entity.groupCount() == 1) {
                            item = new EntityMarker(entity.location().lat(), entity.location().lon(), entity.entities(), null, R.drawable.ic_entity_map);
                        } else {
                            item = new EntityMarker(entity.location().lat(), entity.location().lon(), entity.entities(), null, R.drawable.ic_entities_map);
                        }

                        markerItems.add(item);
                    }
                    mFragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            googleMap.clear();
                            mClusterManager.clearItems();
                            mClusterManager.addItems(markerItems);
                            mClusterManager.cluster();
                        }
                    });

                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());
                }
            });
        }

        if (filters[1]) {
            //Events
            apolloClient.query(new EventsGeoLocationGroupQuery(location.longitude, location.latitude, (int) radius)).enqueue(new ApolloCall.Callback<EventsGeoLocationGroupQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<EventsGeoLocationGroupQuery.Data> response) {
                    final List<EventsGeoLocationGroupQuery.EventsGeoLocationGroup> events = response.data().eventsGeoLocationGroup();
                    mFragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            googleMap.clear();
                            for (EventsGeoLocationGroupQuery.EventsGeoLocationGroup event : events) {
                                EventMarker item = new EventMarker(event.location().lat(), event.location().lon(), event.events(), null, R.drawable.ic_event_map);
                                markerItems.add(item);

                            }
                        }
                    });
                    mFragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            googleMap.clear();
                            mClusterManager.clearItems();
                            mClusterManager.addItems(markerItems);
                            mClusterManager.cluster();
                        }
                    });
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());
                }
            });
        }

        if (filters[2]) {
            //Places
            apolloClient.query(new PlacesGeoLocationGroupQuery(location.longitude, location.latitude, (int) radius)).enqueue(new ApolloCall.Callback<PlacesGeoLocationGroupQuery.Data>() {
                @Override
                public void onResponse(@NotNull final Response<PlacesGeoLocationGroupQuery.Data> response) {
                    final List<PlacesGeoLocationGroupQuery.PlacesGeoLocationGroup> places = response.data().placesGeoLocationGroup();
                    mFragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            googleMap.clear();
                            for (PlacesGeoLocationGroupQuery.PlacesGeoLocationGroup place : places) {
                                PlaceMarker item = new PlaceMarker(place.location().lat(), place.location().lon(), place.places(), null, R.drawable.ic_place_map);
                                markerItems.add(item);
                            }
                        }
                    });

                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());
                }
            });
        }
    }

    public void setUpMapUpdateListener() {
        if (isGPSEnabled)
            if (ActivityCompat.checkSelfPermission(getApplication().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplication().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
    }

    // TODO: posun na mapě
    @Override
    public void onCameraIdle() {
        updatePosition();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
//        updateLocalPosition(location);
    }


    public void updatePosition() {
        Log.d("IS", "CAMERA IDLE");
        float zoom = mMap.getCameraPosition().zoom;
        LatLng current = mMap.getCameraPosition().target;
        float radius = getRadius(mMap);
        if (!lastPossition.equals(current)) {
            Log.d("IS", "POSITION CHANGED " + lastPossition + " new " + current);
            updateMarkers(mMap, current, radius);
            lastPossition = current;
        }
    }

    public void updateFilters(Boolean[] filters) {
        this.filters = filters;
        LatLng current = mMap.getCameraPosition().target;
        float radius = getRadius(mMap);
        updateMarkers(mMap, current, radius);
        lastPossition = current;
    }


//    private void updateLocalPosition(Location location) {
//        Log.d("IS", "CAMERA IDLE");
//        float zoom = mMap.getCameraPosition().zoom;
//        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
//        float radius = getRadius(mMap);
//        if (!lastPossition.equals(current)) {
//            Log.d("IS", "POSITION CHANGED " + lastPossition + " new " + current);
//            updateMarkers(mMap, current, radius);
//            lastPossition = current;
//        }
//    }

    public void updateLocalPositionFromData(double[] location) {
        lastPossition = new LatLng(location[0], location[1]);
        float radius = getRadius(mMap);
        LatLng current = new LatLng(location[0], location[1]);
        updateMarkers(mMap, current, radius);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location[0], location[1])));
    }

    public void updateLocalPositionFromData() {
        if (location.getValue() != null) {
            lastPossition = new LatLng(location.getValue()[0], location.getValue()[1]);
            float radius = getRadius(mMap);
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPossition, 18));
//            updateMarkers(mMap, lastPossition, radius);
        }
    }

    public GoogleMap getmMap() {
        return mMap;
    }
}