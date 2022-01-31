package cz.deepvision.iti.is.ui.dialog;

import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import cz.deepvision.iti.is.util.Requester;

public class BigImageDialog extends DefaultDialog implements DefaultDialog.Updater<Image> {

    private final String imageUrl;

    public BigImageDialog(@NonNull Fragment inputFragment,String imageUrl) {
        super(inputFragment, false);
        this.imageUrl = imageUrl;
        fragment = inputFragment;

    }

    @Override
    public void updateData(Image data) {

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
        updateUI(view);
    }

    private void updateUI(View view) {
        if (!imageUrl.isEmpty()) {
            firstIcon.setVisibility(View.GONE);
            secondIcon.setVisibility(View.GONE);
            name.setVisibility(View.GONE);

            ConstraintLayout.LayoutParams infoIconParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
            photo.setLayoutParams(infoIconParams);

            Requester requester = new Requester(fragment.getActivity());
            requester.makeRequestForAdapter(imageUrl, photo);
        }
    }

}
