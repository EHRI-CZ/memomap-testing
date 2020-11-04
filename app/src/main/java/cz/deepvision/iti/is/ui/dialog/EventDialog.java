package cz.deepvision.iti.is.ui.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.graphql.EntityDetailQuery;
import cz.deepvision.iti.is.models.Event;
import cz.deepvision.iti.is.models.victims.Person;
import cz.deepvision.iti.is.ui.victims.DocumentAdapter;
import cz.deepvision.iti.is.util.LayoutGenerator;
import cz.deepvision.iti.is.util.NetworkConnection;
import cz.deepvision.iti.is.util.Requester;

import static cz.deepvision.iti.is.util.LayoutGenerator.*;

public class EventDialog extends DefaultDialog implements DefaultDialog.Updater<Event> {
    private Event data;
    private Person personData;


    public EventDialog(@NonNull Fragment inputFragment, Event data, boolean small, int style) {
        super(inputFragment, small, style);
        this.data = data;
        fragment = inputFragment;
    }

    public EventDialog(@NonNull Fragment inputFragment, Event data) {
        super(inputFragment, false);
        fragment = inputFragment;
        this.data = data;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateUI();
    }

    private void updateUI() {
        if (data != null) {
            name.setText(data.getEntity());
            String imageUrl = "";

            if (isSmallDialog()) {
                firstIcon.setOnClickListener(icon -> {
                    icon.animate().setDuration(200).rotationBy(360).alpha(0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            dismiss();
                            EventDialog eventDialog = new EventDialog(fragment, data, false, 2);
                            eventDialog.show(fragment.getActivity().getSupportFragmentManager(), "dialog_fullscreen");
                        }
                    });
                });
            } else {
                if (data.getDocumentList().size() > 0) {
                    DocumentAdapter documentAdapter = new DocumentAdapter(data.getDocumentList(), fragment);
                    documentContainer.setHasFixedSize(true);
                    documentContainer.setAdapter(documentAdapter);
                } else documentContainer.setVisibility(View.GONE);
                firstIcon.setOnClickListener(v -> {
                    if (data.getLocation() != null) showDataOnMap(data.getLocation());

                });
                if (data.getLocation() == null) {
                    firstIcon.setEnabled(false);
                    firstIcon.setImageDrawable(ctx.getDrawable(R.drawable.ic_iti_menu_map_grayed));
                } else {
                    firstIcon.setOnClickListener(icon -> {
                        icon.animate().setDuration(200).rotationBy(360).alpha(0).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                dismiss();
                                showDataOnMap(data.getLocation());
                            }
                        });
                    });
                }
                secondIcon.setOnClickListener(v -> {
                    if (data.getLocation() != null) updateDataOnMap(data.getLocation());

                });
                if (data.getLocation() == null) {
                    secondIcon.setEnabled(false);
                    secondIcon.setImageDrawable(ctx.getDrawable(R.drawable.ic_iti_navigate_grayed));
                }

                addInfo(infoContainer, data.getLabel());
                addInfo(infoContainer, data.getDate());
                addInfo(infoContainer, data.getPlace());


                NetworkConnection.getInstance().getApolloClient().query(new EntityDetailQuery(data.getEntityID())).enqueue(new ApolloCall.Callback<EntityDetailQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<EntityDetailQuery.Data> response) {
                        if (response.data() != null && response.data().entityDetail() != null) {

                            EntityDetailQuery.EntityDetail responseData = response.data().entityDetail();
                            personData = new Person(responseData);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.e("IS", e.getMessage());
                    }
                });
                name.setOnClickListener(v -> {
                    VictimDialog victimDialog = new VictimDialog(fragment, personData, false, 0);
                    fragment.getActivity().runOnUiThread(() -> victimDialog.updateData(personData));

                    victimDialog.setOnShowAnotherElement(null);
                    victimDialog.show(fragment.getChildFragmentManager(), VictimDialog.class.getName());
                });
//                infoContainer.setVisibility(View.GONE);
//            if (!imageUrl.equals("")) {
//                Requester requester = new Requester(getActivity(), this);
//                requester.makeRequest(imageUrl);
//            } else getPhoto().setImageDrawable(getActivity().getDrawable(R.drawable.ic_baseline_home_96));
            }
        }

    }

    @Override
    public void updateData(Event data) {
        this.data = data;
    }
}
