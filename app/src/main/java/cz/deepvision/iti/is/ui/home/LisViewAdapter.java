package cz.deepvision.iti.is.ui.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.graphql.EntityDetailQuery;
import cz.deepvision.iti.is.graphql.EventDetailQuery;
import cz.deepvision.iti.is.graphql.PlaceDetailQuery;
import cz.deepvision.iti.is.models.Event;
import cz.deepvision.iti.is.models.Place;
import cz.deepvision.iti.is.models.victims.ListViewItem;
import cz.deepvision.iti.is.models.victims.Person;
import cz.deepvision.iti.is.ui.dialog.EventDialog;
import cz.deepvision.iti.is.ui.dialog.PlaceDialog;
import cz.deepvision.iti.is.ui.dialog.VictimDialog;

public class LisViewAdapter extends RecyclerView.Adapter<LisViewAdapter.ViewHolder> {
    private List<ListViewItem> items;
    private Context ctx;
    private Fragment fragment;


    public LisViewAdapter(List<ListViewItem> items, Fragment fragment) {
        this.items = items;
        this.ctx = fragment.requireContext();
        this.fragment = fragment;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_person_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public class ViewHolder<E> extends RecyclerView.ViewHolder {
        private TextView personName;
        private TextView showInfo;
        private ListViewItem data;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            personName = itemView.findViewById(R.id.txt_info);
            showInfo = itemView.findViewById(R.id.txt_show_detail);

        }

        public void bind(final ListViewItem element) {
            personName.setText(element.getLabel());
            showInfo.setOnClickListener(view -> {
                if(element.getType().equals("entity")){
                    VictimDialog victimDialog = new VictimDialog(fragment, true);
                    ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://77.236.207.194:8529/_db/ITI_DV/iti").build();
                    apolloClient.query(new EntityDetailQuery(element.getKey())).enqueue(new ApolloCall.Callback<EntityDetailQuery.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<EntityDetailQuery.Data> response) {
                            if (response.data() != null && response.data().entityDetail() != null) {
                                EntityDetailQuery.EntityDetail responseData = response.data().entityDetail();
                                fragment.getActivity().runOnUiThread(() -> victimDialog.updateData(new Person(responseData)));
                                victimDialog.show(fragment.getActivity().getSupportFragmentManager(), "dialog_fullscreen");
                            }
                        }

                        @Override
                        public void onFailure(@NotNull ApolloException e) {
                            Log.e("IS", e.getMessage());
                        }
                    });
                }else if(element.getType().equals("place")){
                    PlaceDialog placeDialog = new PlaceDialog(fragment, true);
                    ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://77.236.207.194:8529/_db/ITI_DV/iti").build();
                    apolloClient.query(new PlaceDetailQuery(element.getKey())).enqueue(new ApolloCall.Callback<PlaceDetailQuery.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<PlaceDetailQuery.Data> response) {
                            if (response.data() != null && response.data().placeDetail() != null) {
                                PlaceDetailQuery.PlaceDetail responseData = response.data().placeDetail();
                                fragment.getActivity().runOnUiThread(() -> placeDialog.updateData(new Place(responseData)));
                                placeDialog.show(fragment.getActivity().getSupportFragmentManager(), "dialog_fullscreen");
                            }
                        }

                        @Override
                        public void onFailure(@NotNull ApolloException e) {
                            Log.e("IS", e.getMessage());
                        }
                    });
                }
                else{
                    EventDialog eventDialog = new EventDialog(fragment, true);
                    ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://77.236.207.194:8529/_db/ITI_DV/iti").build();
                    apolloClient.query(new EventDetailQuery(element.getKey())).enqueue(new ApolloCall.Callback<EventDetailQuery.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<EventDetailQuery.Data> response) {
                            if (response.data() != null && response.data().eventDetail() != null) {
                                EventDetailQuery.EventDetail responseData = response.data().eventDetail();
                                fragment.getActivity().runOnUiThread(() -> eventDialog.updateData(new Event(responseData)));
                                eventDialog.show(fragment.getActivity().getSupportFragmentManager(), "dialog_fullscreen");
                            }
                        }

                        @Override
                        public void onFailure(@NotNull ApolloException e) {
                            Log.e("IS", e.getMessage());
                        }
                    });
                }
            });
        }
    }
}
