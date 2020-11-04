package cz.deepvision.iti.is.ui.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.material.button.MaterialButton;

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
import cz.deepvision.iti.is.util.NetworkConnection;

import org.jetbrains.annotations.NotNull;

import java.util.List;

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
        private Button showInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            personName = itemView.findViewById(R.id.txt_info);
            showInfo = itemView.findViewById(R.id.txt_show_detail);

        }

        public void bind(final ListViewItem element) {
            personName.setText(element.getLabel());
            showInfo.setOnClickListener(view -> {
                if(element.getType().equals("entity")){
                    NetworkConnection.getInstance().getApolloClient().query(new EntityDetailQuery(element.getKey())).enqueue(new ApolloCall.Callback<EntityDetailQuery.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<EntityDetailQuery.Data> response) {
                            if (response.data() != null && response.data().entityDetail() != null) {
                                EntityDetailQuery.EntityDetail responseData = response.data().entityDetail();
                                Person data = new Person(responseData);

                                VictimDialog victimDialog = new VictimDialog(fragment,data, true,2);
                                victimDialog.show(fragment.getActivity().getSupportFragmentManager(), "dialog_fullscreen");
                            }
                        }

                        @Override
                        public void onFailure(@NotNull ApolloException e) {
                            Log.e("IS", e.getMessage());
                        }
                    });
                }else if(element.getType().equals("place")){
                    NetworkConnection.getInstance().getApolloClient().query(new PlaceDetailQuery(element.getKey())).enqueue(new ApolloCall.Callback<PlaceDetailQuery.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<PlaceDetailQuery.Data> response) {
                            if (response.data() != null && response.data().placeDetail() != null) {
                                PlaceDetailQuery.PlaceDetail responseData = response.data().placeDetail();
                                Place data = new Place(responseData);

                                PlaceDialog placeDialog = new PlaceDialog(fragment,data,true,2);
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
                    NetworkConnection.getInstance().getApolloClient().query(new EventDetailQuery(element.getKey())).enqueue(new ApolloCall.Callback<EventDetailQuery.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<EventDetailQuery.Data> response) {
                            if (response.data() != null && response.data().eventDetail() != null) {

                                EventDetailQuery.EventDetail responseData = response.data().eventDetail();
                                Event data = new Event(responseData);

                                EventDialog eventDialog = new EventDialog(fragment, data,true,2);
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
