package cz.deepvision.iti.is.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.Event;
import cz.deepvision.iti.is.ui.victims.DocumentAdapter;

public class EventDialog extends DefaultDialog implements DefaultDialog.Updater<Event> {
    private Event data;

    public EventDialog() {
    }

    public EventDialog(@NonNull Fragment inputFragment, boolean small) {
        super(inputFragment.requireContext(), small);
        fragment = inputFragment;
    }

    public EventDialog(@NonNull Context context, boolean small, Event data, Fragment inputFragment) {
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
            if (isSmallDialog()) {
                getFirstIcon().setOnClickListener(view -> {
                    dismiss();
                    EventDialog eventDialog = new EventDialog(getCtx(), false, data, fragment);
                    eventDialog.show(fragment.getActivity().getSupportFragmentManager(), "dialog_fullscreen");
                });
            } else {
                if (data.getDocumentList().size() > 0) {
                    DocumentAdapter documentAdapter = new DocumentAdapter(data.getDocumentList(), fragment);
                    getDocumentContainer().setHasFixedSize(true);
                    getDocumentContainer().setAdapter(documentAdapter);
                } else getDocumentContainer().setVisibility(View.GONE);
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
            getInfoContainer().setVisibility(View.GONE);
            getName().setText(data.getLabel());
            getPhoto().setVisibility(View.GONE);
        }
    }

    @Override
    public void updateData(Event data) {
        this.data = data;
    }
}
