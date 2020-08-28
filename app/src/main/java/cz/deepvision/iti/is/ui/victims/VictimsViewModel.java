package cz.deepvision.iti.is.ui.victims;

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

import cz.deepvision.iti.is.graphql.EntitiesGeoListLimitedQuery;
import cz.deepvision.iti.is.models.victims.RecordListItem;

public class VictimsViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<List<RecordListItem>> mItems;
    private int mOffset;

    public VictimsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is victims fragment");

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
        LatLng location = new LatLng(50.088780, 14.419094);
        int radius = 150;
        // TODO : Zobrazení vícero osob, data takhle přijdou, takže chyba buď GRAPHQL, nebo ITI
        apolloClient.query(new EntitiesGeoListLimitedQuery(location.longitude,location.latitude,(int)radius,mOffset,24))
                .enqueue(new ApolloCall.Callback<EntitiesGeoListLimitedQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull final Response<EntitiesGeoListLimitedQuery.Data> response) {
                        List<RecordListItem> items = new ArrayList<>();
                        for (EntitiesGeoListLimitedQuery.EntitiesGeoListLimited dbItem:response.data().entitiesGeoListLimited()) {
                            RecordListItem item = new RecordListItem();
                            item.setLabel(dbItem.entity_label());
                            item.setKey(dbItem.id());
                            item.setUrl(dbItem.preview());
                            items.add(item);
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