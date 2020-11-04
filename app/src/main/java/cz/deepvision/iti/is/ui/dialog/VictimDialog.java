package cz.deepvision.iti.is.ui.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.transition.Slide;
import androidx.transition.Transition;

import cz.deepvision.iti.is.OnLoadMoreListener;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.victims.Event;
import cz.deepvision.iti.is.models.victims.Person;
import cz.deepvision.iti.is.ui.victims.DocumentAdapter;
import cz.deepvision.iti.is.ui.victims.VictimsViewModel;
import cz.deepvision.iti.is.util.Requester;

import java.util.List;

import static cz.deepvision.iti.is.util.LayoutGenerator.*;

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
            } else photo.setImageDrawable(ctx.getDrawable(R.drawable.no_portrait_icon));


            addInfo(infoContainer, getEndingChar("Narozen", data) + " " + data.getBorn());
            Event addressDeport = filterEvents("residence_before_deportation", data.getEventList());
            if (addressDeport != null) if (addressDeport.getPlace() != null)
                addInfo(infoContainer, "Poslední bydliště před deportací" + ": " + addressDeport.getPlace());

            if (!isSmallDialog()) {
                Event addressProt = filterEvents("residence_before_deportation", data.getEventList());
                if (addressProt != null) if (addressProt.getPlace() != null)
                    addInfo(infoContainer, "Adresa registrace v protektorátu" + ": " + addressProt.getPlace());
                String fate = data.getFate().equals("murdered") ? "Zavražděn" : "Přežil";
                String deathPlace = data.getDeathPlace() != null ? data.getDeathPlace() : "";
                String sexFate = getEndingChar(fate, data);
                addInfo(infoContainer, sexFate + " " + deathPlace);
            } else {
                String fate = data.getFate().equals("murdered") ? "Zavražděn" : "Přežil";
                addInfo(infoContainer, getEndingChar(fate, data));
            }

            List<Event> transports = getTransports(data.getEventList());
            for (Event transport : transports) {
                if (transport.getName() != null) {
                    String[] parts = transport.getName().split("\\(");
                    addInfo(infoContainer, "Transport " + parts[0] + ",č. " + transport.getTransport_nm() + "(" + parts[1]);
                }
            }
        }
    }

    @Override
    public void updateData(Person data) {
        this.data = data;
    }
}