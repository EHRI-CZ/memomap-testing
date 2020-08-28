package cz.deepvision.iti.is.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cz.deepvision.iti.is.models.victims.Document;
import cz.deepvision.iti.is.util.Requester;

import static cz.deepvision.iti.is.util.LayoutGenerator.addInfo;

public class DocumentDialog extends DefaultDialog implements DefaultDialog.Updater<Document> {
    private Document data;

    public DocumentDialog() {
    }

    public DocumentDialog(@NonNull Fragment inputFragment,Document data) {
        super(inputFragment.requireContext(), false);
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
            Requester requester = new Requester(fragment.getActivity());
            requester.makeRequestForAdapter(data.getFullImage(),getPhoto());
            getFirstIcon().setVisibility(View.GONE);
            getSecondIcon().setVisibility(View.GONE);
            getName().setText(data.getName());
            addInfo(getInfoContainer(),"Oběti šoa, jejichž se dokument týká:" + data.getName());
        }

    }


    @Override
    public void updateData(Document data) {
        this.data = data;
        updateUI();
    }
}
