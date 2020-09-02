package cz.deepvision.iti.is.ui.dialog;

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

    public PlaceDialog() {
    }

    public PlaceDialog(@NonNull Fragment fragment, boolean small) {
        super(fragment.requireContext(), small);
        this.fragment = fragment;
    }

    public PlaceDialog(@NonNull Context context, boolean small, Place data, Fragment fragment) {
        super(context, small);
        this.fragment = fragment;
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
            getName().setText(data.getLabel());
            if (isSmallDialog()) {
                getFirstIcon().setOnClickListener(view -> {
                    dismiss();
                    PlaceDialog placeDialog = new PlaceDialog(getCtx(), false, data, fragment);
                    placeDialog.show(fragment.getActivity().getSupportFragmentManager(), "dialog_fullscreen");
                });
                if (data.getPreview() != null) imageUrl = data.getPreview();
                addInfo(getInfoContainer(), data.getDescription().substring(0, data.getDescription().length() / 2) + "...");
            } else {
                if (data.getDocumentList().size() > 0) {
                    DocumentAdapter documentAdapter = new DocumentAdapter(data.getDocumentList(), fragment);
                    getDocumentContainer().setHasFixedSize(true);
                    getDocumentContainer().setAdapter(documentAdapter);
                } else getDocumentContainer().setVisibility(View.GONE);
                if (data.getFull() != null) imageUrl = data.getFull();
                addInfo(getInfoContainer(), data.getDescription());
                changeLayoutConstrains();
                getFirstIcon().setOnClickListener(v -> {
                    if (data.getLocation() != null) showDataOnMap(data.getLocation());
                });
                if (data.getLocation() == null) {
                    getFirstIcon().setEnabled(false);
                    getFirstIcon().setImageDrawable(ctx.getDrawable(R.drawable.ic_iti_menu_map_grayed));
                }
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

        }
    }

    private void changeLayoutConstrains() {
        ConstraintLayout.LayoutParams photoParams = (ConstraintLayout.LayoutParams) getPhoto().getLayoutParams();
        photoParams.rightToRight = getRoot().getId();
        photoParams.leftToLeft = getRoot().getId();
        photoParams.leftMargin = 0;
        photoParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        photoParams.height = 400;
        getPhoto().setLayoutParams(photoParams);

        ConstraintLayout.LayoutParams textParams = (ConstraintLayout.LayoutParams) getInfoContainer().getLayoutParams();
        textParams.leftToLeft = getRoot().getId();
        textParams.rightToRight = getRoot().getId();
        textParams.topToBottom = getPhoto().getId();
        textParams.topToTop = -1;
        textParams.topMargin = 8;
        textParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        textParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getInfoContainer().setLayoutParams(textParams);

        ConstraintLayout.LayoutParams documentParams = (ConstraintLayout.LayoutParams) getDocumentContainer().getLayoutParams();
        documentParams.topToBottom = getInfoContainer().getId();
        getDocumentContainer().setLayoutParams(documentParams);


    }

    @Override
    public void updateData(Place data) {
        this.data = data;
    }
}
