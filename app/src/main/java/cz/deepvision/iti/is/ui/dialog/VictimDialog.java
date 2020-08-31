package cz.deepvision.iti.is.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.graphql.EntityDetailQuery;
import cz.deepvision.iti.is.models.victims.Event;
import cz.deepvision.iti.is.models.victims.Person;
import cz.deepvision.iti.is.ui.victims.DocumentAdapter;
import cz.deepvision.iti.is.util.Requester;

import static cz.deepvision.iti.is.util.LayoutGenerator.addInfo;
import static cz.deepvision.iti.is.util.LayoutGenerator.filterEvents;
import static cz.deepvision.iti.is.util.LayoutGenerator.getEndingChar;
import static cz.deepvision.iti.is.util.LayoutGenerator.getTransports;

public class VictimDialog extends DefaultDialog implements DefaultDialog.Updater<Person>, Requester.UpdatePhoto {
    private Person data;

    public VictimDialog() {
    }

    public VictimDialog(@NonNull Fragment inputFragment, boolean small) {
        super(inputFragment.requireContext(), small);
        fragment = inputFragment;
    }

    public VictimDialog(@NonNull Context context, boolean small, Person data, Fragment inputFragment) {
        super(context, small);
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
            String imageUrl = "";
            if (isSmallDialog()) {
                getFirstIcon().setOnClickListener(view -> {
                    dismiss();
                    VictimDialog victimDialog = new VictimDialog(getCtx(), false, data, fragment);
                    victimDialog.show(fragment.getActivity().getSupportFragmentManager(), "dialog_fullscreen");
                });
                if (data.getPreview() != null) imageUrl = data.getPreview();
            } else {
                if (data.getDocumentList().size() > 0) {
                    DocumentAdapter documentAdapter = new DocumentAdapter(data.getDocumentList(), fragment);
                    getDocumentContainer().setHasFixedSize(true);
                    getDocumentContainer().setAdapter(documentAdapter);
                } else getDocumentContainer().setVisibility(View.GONE);
                if (data.getFull() != null) imageUrl = data.getFull();
                getFirstIcon().setOnClickListener(v -> {
                    if (data.getLocation() != null) showDataOnMap(data.getLocation());
                });
                if (data.getLocation() == null) {
                    getFirstIcon().setEnabled(false);
                    getFirstIcon().setImageDrawable(ctx.getDrawable(R.drawable.ic_iti_menu_map_grayed));
                }
                getPhoto().setOnClickListener(view -> {
                    if (isImageFitToScreen) {
                        isImageFitToScreen = false;
                        hideUI(View.VISIBLE);
                        getPhoto().setLayoutParams(new ConstraintLayout.LayoutParams(300, 300));
                        ConstraintLayout.LayoutParams photoParams = (ConstraintLayout.LayoutParams) getPhoto().getLayoutParams();
                        photoParams.topToBottom = getName().getId();
                        photoParams.topMargin = 16;
                        getPhoto().setLayoutParams(photoParams);
                    } else {
                        isImageFitToScreen = true;
                        hideUI(View.INVISIBLE);
                        getPhoto().setLayoutParams(new ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                        getPhoto().setScaleType(ImageView.ScaleType.FIT_CENTER);
                    }

                    AutoTransition transition = new AutoTransition();
                    transition.setDuration(150);
                    transition.setInterpolator(new AccelerateDecelerateInterpolator());
                    TransitionManager.beginDelayedTransition(getRoot(), transition);
                });
            }
            getSecondIcon().setOnClickListener(v -> {
                if (data.getLocation() != null) updateDataOnMap(data.getLocation());
            });

            if (data.getLocation() == null) {
                getSecondIcon().setEnabled(false);
                getSecondIcon().setImageDrawable(ctx.getDrawable(R.drawable.ic_iti_navigate_grayed));
            }

            if (!imageUrl.equals("")) {
                Requester requester = new Requester(getActivity(), this);
                requester.makeRequest(imageUrl);
            }

            getName().setText(data.getName());
            addInfo(getInfoContainer(), getEndingChar("Narozen", data) + " " + data.getBorn());
            Event addressDeport = filterEvents("residence_before_deportation", data.getEventList());
            if (addressDeport != null) if (addressDeport.getPlace() != null)
                addInfo(getInfoContainer(), "Poslední bydliště před deportací" + ": " + addressDeport.getPlace());

            if (!isSmallDialog()) {
                Event addressProt = filterEvents("residence_before_deportation", data.getEventList());
                if (addressProt != null) if (addressProt.getPlace() != null)
                    addInfo(getInfoContainer(), "Adresa registrace v protektorátu" + ": " + addressProt.getPlace());
            }

            if (data.getDeath() != null) {
                String fate = data.getFate().equals("murdered") ? "Zavražděn " : "Přežil";
                String deathPlace = data.getDeathPlace() != null ? data.getDeathPlace() : "";
                addInfo(getInfoContainer(), getEndingChar(fate + " " + deathPlace, data));
            }
            List<Event> transports = getTransports(data.getEventList());
            for (Event transport : transports) {
                if (transport.getName() != null) {
                    String[] parts = transport.getName().split("\\(");
                    addInfo(getInfoContainer(), "Transport " + parts[0] + ",č. " + transport.getTransport_nm() + "(" + parts[1]);
                }
            }
        }
    }


    @Override
    public void updateData(Person data) {
        this.data = data;
    }

}
