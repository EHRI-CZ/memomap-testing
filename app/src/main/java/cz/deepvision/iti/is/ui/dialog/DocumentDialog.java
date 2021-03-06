package cz.deepvision.iti.is.ui.dialog;

import static cz.deepvision.iti.is.util.LayoutGenerator.addInfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cz.deepvision.iti.is.models.victims.Document;
import cz.deepvision.iti.is.util.Requester;

public class DocumentDialog extends DefaultDialog implements DefaultDialog.Updater<Document> {
    private Document data;


    public DocumentDialog(@NonNull Fragment inputFragment, Document data) {
        super(inputFragment, false);
        this.data = data;
        fragment = inputFragment;
    }

    public DocumentDialog(@NonNull Fragment inputFragment, Document data, int style) {
        super(inputFragment, false, style);
        this.data = data;
        fragment = inputFragment;
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
            firstIcon.setVisibility(View.GONE);
            secondIcon.setVisibility(View.GONE);
            name.setText(data.getName());
            if (data.getName() != null)
                addInfo(infoContainer, "Oběti šoa, jejichž se dokument týká:" + data.getName());

            photo.setOnClickListener(view -> {
                BigImageDialog bigImageDialog = new BigImageDialog(fragment, data.getFullImage());
                bigImageDialog.show(fragment.getActivity().getSupportFragmentManager(), "dialog_fullscreen");
            });
            Requester requester = new Requester(fragment.getActivity());
            requester.makeRequestForAdapter(data.getFullImage(), photo);
        }
    }


    @Override
    public void updateData(Document data) {
        this.data = data;
    }
}
