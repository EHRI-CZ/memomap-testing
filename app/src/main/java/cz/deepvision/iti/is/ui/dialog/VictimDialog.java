package cz.deepvision.iti.is.ui.dialog;

import static cz.deepvision.iti.is.util.LayoutGenerator.addInfo;
import static cz.deepvision.iti.is.util.LayoutGenerator.addTransportInfo;
import static cz.deepvision.iti.is.util.LayoutGenerator.filterEvents;
import static cz.deepvision.iti.is.util.LayoutGenerator.getEndingChar;
import static cz.deepvision.iti.is.util.LayoutGenerator.getTransports;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.victims.Event;
import cz.deepvision.iti.is.models.victims.Person;
import cz.deepvision.iti.is.ui.victims.DocumentAdapter;
import cz.deepvision.iti.is.util.Requester;

public class VictimDialog extends DefaultDialog implements DefaultDialog.Updater<Person> {
    private Person data;

    public VictimDialog(@NonNull Fragment inputFragment, Person data, boolean small, int style) {
        super(inputFragment, small, style);
        this.data = data;
        fragment = inputFragment;
    }

    public VictimDialog(@NonNull Fragment inputFragment, Person data, int style) {
        super(inputFragment, false, style);
        fragment = inputFragment;
        this.data = data;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onStart() {
        super.onStart();
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
            name.setText(data.getName());
            if (name.getText().toString().isEmpty())
                name.setVisibility(View.GONE);
            String imageUrl = "";
            if (isSmallDialog()) {
                firstIcon.setOnClickListener(icon -> {
                    icon.animate().setDuration(200).rotationBy(360).alpha(0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            dismiss();
                            VictimDialog victimDialog = new VictimDialog(fragment, data, false, 2);
                            victimDialog.show(fragment.getActivity().getSupportFragmentManager(), "dialog_fullscreen");
                        }
                    });
                });
                if (data.getPreview() != null) imageUrl = data.getPreview();
            } else {
                if (data.getDocumentList().size() > 0) {
                    DocumentAdapter documentAdapter = new DocumentAdapter(data.getDocumentList(), fragment);
                    documentContainer.setHasFixedSize(true);
                    documentContainer.setAdapter(documentAdapter);
                } else documentContainer.setVisibility(View.GONE);
                if (data.getFull() != null) imageUrl = data.getFull();
                if (data.getLocation() == null) {
                    firstIcon.setEnabled(false);
                    firstIcon.setImageDrawable(ctx.getDrawable(R.drawable.ic_iti_menu_map_grayed));
                } else {
                    firstIcon.setOnClickListener(icon -> {
                        icon.animate().setDuration(200).rotationBy(360).alpha(0).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                showDataOnMap(data.getLocation());
                                dismiss();
                            }
                        });
                    });
                }
                String finalImageUrl = imageUrl;
                photo.setOnClickListener(view -> {
                    BigImageDialog bigImageDialog = new BigImageDialog(fragment, finalImageUrl);
                    bigImageDialog.show(fragment.getActivity().getSupportFragmentManager(), "dialog_fullscreen");
                });

            }
            secondIcon.setOnClickListener(v -> {
                if (data.getLocation() != null) updateDataOnMap(data.getLocation());
            });

            if (data.getLocation() == null) {
                secondIcon.setEnabled(false);
                secondIcon.setImageDrawable(ctx.getDrawable(R.drawable.ic_iti_navigate_grayed));
            }

            if (!imageUrl.equals("")) {
                Requester requester = new Requester(getActivity(), this);
                requester.makeRequest(imageUrl);
            } else photo.setImageDrawable(ctx.getDrawable(R.drawable.ic_baseline_person));


            addInfo(infoContainer, getEndingChar("Narozen", data) + " " + data.getBorn() + "\n");

            Event addressProt = filterEvents("residence_registration", data.getEventList());
            if (addressProt != null) if (addressProt.getPlace() != null)
                addInfo(infoContainer, "Adresa registrace v protektorátu" + ": " + addressProt.getPlace() + "\n");

            if (!isSmallDialog()) {
                Event addressDeport = filterEvents("residence_before_deportation", data.getEventList());
                if (addressDeport != null) if (addressDeport.getPlace() != null)
                    addInfo(infoContainer, "Poslední bydliště před deportací" + ": " + addressDeport.getPlace() + "\n");
            }

            List<Event> transports = getTransports(data.getEventList());

            List<Event> collect = transports.stream().sorted(Comparator.comparingInt(event -> Integer.parseInt(event.getTransport_nm()))).collect(Collectors.toList());
            for (int i = 0; i < collect.size(); i++) {
                if (collect.get(i).getName() != null) {
                    String[] parts = collect.get(i).getName().split("\\(");
                    addTransportInfo(infoContainer, "Transport " + parts[0] + ",č. " + collect.get(i).getTransport_nm() + "(" + parts[1], i == collect.size() - 1);
                }
            }
            String fate = data.getFate().equals("murdered") ? "Zavražděn" : "Přežil";
            String deathPlace = data.getDeathPlace() != null ? data.getDeathPlace() : "";
            String sexFate = getEndingChar(fate, data);


            if (isSmallDialog())
                addInfo(infoContainer, getEndingChar(fate, data));
            else
                addInfo(infoContainer, sexFate + " - " + deathPlace);
        }
    }

    @Override
    public void updateData(Person data) {
        this.data = data;
    }
}
