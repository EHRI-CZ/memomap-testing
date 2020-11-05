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
import cz.deepvision.iti.is.graphql.EntitiesGeoListLimitedQuery;
import cz.deepvision.iti.is.models.victims.RecordListItem;
import cz.deepvision.iti.is.util.NetworkConnection;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VictimsViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<List<RecordListItem>> mItems;
    private int mOffset;
    private int number = 1;

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
        LatLng location = new LatLng(50.088780, 14.419094);
        int radius = 150;
        if(mOffset>0) radius += radius;

        // TODO : Zobrazení vícero osob, data takhle přijdou, takže chyba buď GRAPHQL, nebo ITI
        // TODO : Limit nefunguje s větším radiusem přijde záznamů více
        NetworkConnection.getInstance().getApolloClient().query(new EntitiesGeoListLimitedQuery(location.longitude,location.latitude,(int)radius,mOffset,24))
                .enqueue(new ApolloCall.Callback<EntitiesGeoListLimitedQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull final Response<EntitiesGeoListLimitedQuery.Data> response) {
                        List<RecordListItem> items = new ArrayList<>();
                        for (EntitiesGeoListLimitedQuery.EntitiesGeoListLimited dbItem:response.data().entitiesGeoListLimited()) {
                            RecordListItem item = new RecordListItem();
//                            item.setLabel(dbItem.entity_label());
                            item.setLabel(number + ":" +dbItem.entity_label());
                            item.setKey(dbItem.id());
                            item.setUrl(dbItem.preview());
                            items.add(item);
                            number++;
                        }
                        mItems.postValue(items);
                        mOffset+=response.data().entitiesGeoListLimited().size();
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.e("IS",e.getMessage());
                    }
                });
    }
}