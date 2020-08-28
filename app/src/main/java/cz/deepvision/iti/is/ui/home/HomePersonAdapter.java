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
import cz.deepvision.iti.is.models.victims.Person;
import cz.deepvision.iti.is.ui.dialog.VictimDialog;

import static cz.deepvision.iti.is.graphql.EntitiesGeoLocationGroupQuery.Entity;

public class HomePersonAdapter extends RecyclerView.Adapter<HomePersonAdapter.PersonViewHolder> {
    private List<Entity> items;
    private Context ctx;
    private Fragment fragment;

    public HomePersonAdapter(List<Entity> item, Fragment fragment) {
        setHasStableIds(true);
        this.fragment = fragment;
        this.ctx = fragment.requireContext();
        this.items = item;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PersonViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_person_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
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

    public class PersonViewHolder extends RecyclerView.ViewHolder {
        private TextView personName;
        private TextView showInfo;
        private Person data;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            personName = itemView.findViewById(R.id.txt_info);
            showInfo = itemView.findViewById(R.id.txt_show_detail);

        }

        public void bind(final Entity element) {
            personName.setText(element.label());
            showInfo.setOnClickListener(view -> {
                VictimDialog victimDialog = new VictimDialog(fragment, true);
                ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://77.236.207.194:8529/_db/ITI_DV/iti").build();
                apolloClient.query(new EntityDetailQuery(element.id())).enqueue(new ApolloCall.Callback<EntityDetailQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<EntityDetailQuery.Data> response) {
                        if (response.data() != null && response.data().entityDetail() != null) {
                            EntityDetailQuery.EntityDetail responseData = response.data().entityDetail();
                            data = new Person(responseData);
                            fragment.getActivity().runOnUiThread(() -> victimDialog.updateData(data));
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.e("IS", e.getMessage());
                    }
                });
                victimDialog.show(fragment.getActivity().getSupportFragmentManager(),"dialog_fullscreen");
                    //TODO : get query to show more detailed info
                });
        }
    }
}
