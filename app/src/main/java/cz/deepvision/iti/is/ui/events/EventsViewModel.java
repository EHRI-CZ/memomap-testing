package cz.deepvision.iti.is.ui.events;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.maps.model.LatLng;
import cz.deepvision.iti.is.graphql.EventsGeoListLimitedQuery;
import cz.deepvision.iti.is.models.victims.RecordListItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EventsViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<List<RecordListItem>> mItems;
    private int mOffset;
    private static int number = 1;

    public EventsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is events fragment");

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

    public void loadData() {
        ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://77.236.207.194:8529/_db/ITI_DV/iti").build();
        LatLng location = new LatLng(50.088780, 14.419094);
        final int[] radius = {150};
        if (mOffset > 0) radius[0] = 150 * (mOffset / 24);
        if (mOffset == 0) {
            number = 1;
        }
        // TODO : Zobrazení vícero osob, data takhle přijdou, takže chyba buď GRAPHQL, nebo ITI
        // TODO : Limit nefunguje s větším radiusem přijde záznamů více
        apolloClient.query(new EventsGeoListLimitedQuery(location.longitude, location.latitude, (int) radius[0], mOffset, 24)).enqueue(new ApolloCall.Callback<EventsGeoListLimitedQuery.Data>() {
            @Override
            public void onResponse(@NotNull final Response<EventsGeoListLimitedQuery.Data> response) {
                List<RecordListItem> items = new ArrayList<>();
                if (response.data() != null) {
                    /*if (response.data().eventsGeoListLimited() != null) {
                        if (response.data().eventsGeoListLimited().size() == 0) {
                            radius[0] += 150;
                            loadData();
                        }
                    }*/
                    for (EventsGeoListLimitedQuery.EventsGeoListLimited dbItem : response.data().eventsGeoListLimited()) {
                        RecordListItem item = new RecordListItem();
                        item.setLabel(number + ":" + dbItem.event_label());
                        item.setKey(dbItem.id());
                        item.setUrl(dbItem.preview());
                        items.add(item);
                        number++;
                    }
                    mItems.postValue(items);
                    mOffset += 24;
                }
            }
                @Override public void onFailure (@NotNull ApolloException e){
                    Log.e("IS", e.getMessage());
                }
            });
        }
    }