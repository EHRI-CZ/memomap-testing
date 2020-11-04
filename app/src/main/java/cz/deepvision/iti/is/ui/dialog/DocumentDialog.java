package cz.deepvision.iti.is.ui.dialog;

import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
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
import cz.deepvision.iti.is.models.victims.Document;
import cz.deepvision.iti.is.util.Requester;

import static cz.deepvision.iti.is.util.LayoutGenerator.addInfo;

public class DocumentDialog extends DefaultDialog implements DefaultDialog.Updater<Document> {
    private Document data;


    public DocumentDialog(@NonNull Fragment inputFragment,Document data) {
        super(inputFragment, false);
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
            addInfo(infoContainer, "Oběti šoa, jejichž se dokument týká:" + data.getName());
            Requester requester = new Requester(fragment.getActivity());
            requester.makeRequestForAdapter(data.getFullImage(), photo);
        }
    }

    @Override
    public void updateData(Document data) {
        this.data = data;
    }
}
