package cz.deepvision.iti.is.ui.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import cz.deepvision.iti.is.R;

public class AboutFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AboutViewModel aboutViewModel = ViewModelProviders.of(this).get(AboutViewModel.class);
        View root = inflater.inflate(R.layout.fragment_about, container, false);

        return root;
    }
}