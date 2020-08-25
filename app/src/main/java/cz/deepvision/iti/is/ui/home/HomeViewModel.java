package cz.deepvision.iti.is.ui.home;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.clustering.ClusterManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cz.deepvision.iti.is.CustomClusterManager;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.graphql.EntitiesGeoLocationGroupQuery;
import cz.deepvision.iti.is.models.MarkerItem;

public class HomeViewModel extends AndroidViewModel implements CustomClusterManager.onCameraIdleExtension {

    private MutableLiveData<String> mText;
    private CustomClusterManager<MarkerItem> mClusterManager;

    SupportMapFragment mapFragment = null;
    Fragment mFragment = null;
    private HomeViewModel thisModel = this;
    private GoogleMap mMap;
    private LatLng lastPossition = null;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
        if (mapFragment == null) {
            GoogleMapOptions options = new GoogleMapOptions();
            options.compassEnabled(false);
            options.tiltGesturesEnabled(false);
            options.rotateGesturesEnabled(false);

            mapFragment = SupportMapFragment.newInstance(options);

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    LatLng latLng = new LatLng(50.088780, 14.419094);
                    lastPossition = latLng;
                    mMap = googleMap;
                    googleMap.addMarker(new MarkerOptions().position(latLng)
                            .title("ITI"));
                    googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplication().getApplicationContext(), R.raw.map_style_dark));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));
                    //Cluster
                    mClusterManager = new CustomClusterManager<>(mFragment.getActivity(), googleMap);
                    mClusterManager.setmOnCameraIdleExtension(thisModel);
                    googleMap.setOnCameraIdleListener(mClusterManager);
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
                }
            });
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

    public float getRadius(GoogleMap googleMap){
        VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();

        LatLng farRight = visibleRegion.farRight;
        LatLng farLeft = visibleRegion.farLeft;
        LatLng nearRight = visibleRegion.nearRight;
        LatLng nearLeft = visibleRegion.nearLeft;

        float[] distanceWidth = new float[2];
        Location.distanceBetween(
                (farRight.latitude+nearRight.latitude)/2,
                (farRight.longitude+nearRight.longitude)/2,
                (farLeft.latitude+nearLeft.latitude)/2,
                (farLeft.longitude+nearLeft.longitude)/2,
                distanceWidth
        );


        float[] distanceHeight = new float[2];
        Location.distanceBetween(
                (farRight.latitude+nearRight.latitude)/2,
                (farRight.longitude+nearRight.longitude)/2,
                (farLeft.latitude+nearLeft.latitude)/2,
                (farLeft.longitude+nearLeft.longitude)/2,
                distanceHeight
        );

        float distance;

        if (distanceWidth[0]>distanceHeight[0]){
            distance = distanceWidth[0];
        } else {
            distance = distanceHeight[0];
        }
        return distance;
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth()/2, vectorDrawable.getIntrinsicHeight()/2);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth()/2, vectorDrawable.getIntrinsicHeight()/2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void updateMarkers(final GoogleMap googleMap, LatLng location, float radius){
        ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://77.236.207.194:8529/_db/ITI_DV/iti").build();

        //Places
        /*apolloClient.query(new PlacesGeolocationQuery(location.longitude,location.latitude,(int)radius))
                .enqueue(new ApolloCall.Callback<PlacesGeolocationQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull final Response<PlacesGeolocationQuery.Data> response) {
                        final List<PlacesGeolocationQuery.PlacesGeolocation> places = response.data().placesGeolocation();
                        final BitmapDescriptor icon = bitmapDescriptorFromVector(mFragment.getContext(),R.drawable.ic_place_map);
                        mFragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                googleMap.clear();
                                for (PlacesGeolocationQuery.PlacesGeolocation place:places) {
                                    LatLng latLng = new LatLng(place.location().lat(), place.location().lon());
                                    googleMap.addMarker(new MarkerOptions().position(latLng)
                                            .title(place.preferred_labels().cs()).icon(icon));
                                }
                            }
                        });

                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.e("IS",e.getMessage());
                    }
                });
         */
        //Entities
        /*
        apolloClient.query(new EntitiesGeoLocationQuery(location.longitude,location.latitude,(int)radius))
                .enqueue(new ApolloCall.Callback<EntitiesGeoLocationQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<EntitiesGeoLocationQuery.Data> response) {
                        final List<EntitiesGeoLocationQuery.EntitiesGeoLocation> entities = response.data().entitiesGeoLocation();
                        final BitmapDescriptor icon = bitmapDescriptorFromVector(mFragment.getContext(),R.drawable.ic_entity_map);
                        mFragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                googleMap.clear();
                                for (EntitiesGeoLocationQuery.EntitiesGeoLocation entity:entities) {
                                    LatLng latLng = new LatLng(entity.location().lat(), entity.location().lon());
                                    googleMap.addMarker(new MarkerOptions().position(latLng)
                                            .title(entity.entity_label()).icon(icon));
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.e("IS",e.getMessage());
                    }
                });
         */
        //Entities groups
        apolloClient.query(new EntitiesGeoLocationGroupQuery(location.longitude,location.latitude,(int)radius))
                .enqueue(new ApolloCall.Callback<EntitiesGeoLocationGroupQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<EntitiesGeoLocationGroupQuery.Data> response) {
                        final List<EntitiesGeoLocationGroupQuery.EntitiesGeoLocationGroup> entities = response.data().entitiesGeoLocationGroup();
                        //final BitmapDescriptor icon = bitmapDescriptorFromVector(mFragment.getContext(),R.drawable.ic_entity_map);
                        //final BitmapDescriptor iconGroup = bitmapDescriptorFromVector(mFragment.getContext(),R.drawable.ic_entities_map);//BitmapDescriptorFactory.fromResource(R.drawable.ic_entities_map);
                        final List<MarkerItem> markerItems = new ArrayList<>();
                        for (EntitiesGeoLocationGroupQuery.EntitiesGeoLocationGroup entity:entities) {

                            //LatLng latLng = new LatLng(entity.location().lat(), entity.location().lon());
                            MarkerItem item = null;
                            if(entity.groupCount()==1){
                                item = new MarkerItem(entity.location().lat(),entity.location().lon(),entity.entity_label(),null,R.drawable.ic_entity_map);
                            }else{
                                item = new MarkerItem(entity.location().lat(),entity.location().lon(),entity.entity_label(),null,R.drawable.ic_entities_map);
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
                        Log.e("IS",e.getMessage());
                    }
                });
    }

    @Override
    public void onCameraIdle() {
        Log.d("IS","CAMERA IDLE");
        float zoom = mMap.getCameraPosition().zoom;
        LatLng current = mMap.getCameraPosition().target;
        float radius = getRadius(mMap);
        if(!lastPossition.equals(current)){
            Log.d("IS","POSITION CHANGED "+lastPossition+" new "+current);
            updateMarkers(mMap,current,radius);
            lastPossition = current;
        }
    }
}