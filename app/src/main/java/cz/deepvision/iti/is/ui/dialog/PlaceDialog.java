package cz.deepvision.iti.is.ui.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.Place;
import cz.deepvision.iti.is.ui.victims.DocumentAdapter;
import cz.deepvision.iti.is.util.Requester;

import static cz.deepvision.iti.is.util.LayoutGenerator.addInfo;

public class PlaceDialog extends DefaultDialog implements DefaultDialog.Updater<Place> {
    private Place data;


    public PlaceDialog(@NonNull Fragment inputFragment, Place data, boolean small, int style) {
        super(inputFragment, small, style);
        this.fragment = inputFragment;
        this.data = data;
    }

    public PlaceDialog(@NonNull Fragment inputFragment, Place data, int style) {
        super(inputFragment, false, style);
        this.fragment = inputFragment;
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
            name.setText(data.getLabel());
            String imageUrl = "";
            if (isSmallDialog()) {
                firstIcon.setOnClickListener(icon -> {
                    icon.animate().setDuration(200).rotationBy(360).alpha(0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            dismiss();
                            PlaceDialog placeDialog = new PlaceDialog(fragment, data, false, 2);
                            placeDialog.show(fragment.getActivity().getSupportFragmentManager(), "dialog_fullscreen");
                        }
                    });
                });
                if (data.getPreview() != null) imageUrl = data.getPreview();
                addInfo(infoContainer, data.getDescription().substring(0, data.getDescription().length() / 2) + "...");
            } else {
                if (data.getDocumentList().size() > 0) {
                    DocumentAdapter documentAdapter = new DocumentAdapter(data.getDocumentList(), fragment);
                    documentContainer.setHasFixedSize(true);
                    documentContainer.setAdapter(documentAdapter);
                } else documentContainer.setVisibility(View.GONE);
                if (data.getFull() != null) imageUrl = data.getFull();
                addInfo(infoContainer, data.getDescription());
                changeLayoutConstrains();

                if (data.getLocation() == null) {
                    firstIcon.setEnabled(false);
                    firstIcon.setImageDrawable(ctx.getDrawable(R.drawable.ic_iti_menu_map_grayed));
                }else{
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
            } else
                photo.setImageDrawable(getActivity().getDrawable(R.drawable.ic_baseline_home_96));


        }
    }

    private void changeLayoutConstrains() {
        ConstraintLayout.LayoutParams photoParams = (ConstraintLayout.LayoutParams) photo.getLayoutParams();
        photoParams.rightToRight = root.getId();
        photoParams.leftToLeft = root.getId();
        photoParams.leftMargin = 0;
        photoParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        photoParams.height = 400;
        photo.setLayoutParams(photoParams);

        ConstraintLayout.LayoutParams textParams = (ConstraintLayout.LayoutParams) infoContainer.getLayoutParams();
        textParams.leftToLeft = root.getId();
        textParams.rightToRight = root.getId();
        textParams.topToBottom = photo.getId();
        textParams.topToTop = -1;
        textParams.topMargin = 8;
        textParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        textParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        infoContainer.setLayoutParams(textParams);

        ConstraintLayout.LayoutParams documentParams = (ConstraintLayout.LayoutParams) documentContainer.getLayoutParams();
        documentParams.topToBottom = infoContainer.getId();
        documentContainer.setLayoutParams(documentParams);


    }

    @Override
    public void updateData(Place data) {
        this.data = data;
    }

}
