package cz.deepvision.iti.is.ui.places;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cz.deepvision.iti.is.graphql.PlacesGeoListLimitedQuery;
import cz.deepvision.iti.is.models.victims.RecordListItem;

public class PlacesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    private MutableLiveData<List<RecordListItem>> mItems;
    private int mOffset;
    private static int number = 1;

    public PlacesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is places fragment");

        mItems = new MutableLiveData<>();
        mOffset = 0;
        loadData();
    }

    public LiveData<String> getText() {
        return mText;
    }

    public MutableLiveData<List<RecordListItem>> getItems() {
        return mItems;
    }

    public void loadData(){
        ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://77.236.207.194:8529/_db/ITI_DV/iti").build();
        LatLng location = new LatLng(50.089397, 14.416994);
        int radius = 150;
        if(mOffset>0) radius = 150 * (mOffset/24);
        if(mOffset ==0) {
            number = 1;
        }
        apolloClient.query(new PlacesGeoListLimitedQuery(location.longitude,location.latitude,(int)radius,mOffset,24))
                .enqueue(new ApolloCall.Callback<PlacesGeoListLimitedQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull final Response<PlacesGeoListLimitedQuery.Data> response) {
                        List<RecordListItem> items = new ArrayList<>();
                        for (PlacesGeoListLimitedQuery.PlacesGeoListLimited dbItem:response.data().placesGeoListLimited()) {
                            RecordListItem item = new RecordListItem();
                            item.setLabel(number + ":" +dbItem.place_label());
                            item.setKey(dbItem.id());
                            item.setUrl(dbItem.preview());
                            items.add(item);
                            number++;
                        }
                        mItems.postValue(items);
                        mOffset+=24;
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.e("IS",e.getMessage());
                    }
                });
    }
}