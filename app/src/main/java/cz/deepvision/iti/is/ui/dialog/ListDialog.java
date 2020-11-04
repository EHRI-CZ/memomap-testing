package cz.deepvision.iti.is.ui.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.victims.ListViewItem;
import cz.deepvision.iti.is.ui.home.LisViewAdapter;

public class ListDialog extends DialogFragment {
    private Dialog dialog;
    private List<ListViewItem> items;
    private Fragment fragment;

    public ListDialog(List<ListViewItem> items, Fragment myFragment) {
        this.items = items;
        this.fragment =myFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_MyTheme_BottomSheetDialogSlideUpDown);
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.custom_person_list, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LisViewAdapter lisViewAdapter = new LisViewAdapter(items, fragment);

        RecyclerView container = view.findViewById(R.id.person_list);
        container.addItemDecoration(new DividerItemDecoration(fragment.requireContext(), DividerItemDecoration.VERTICAL));
        container.setHasFixedSize(true);
        container.setLayoutManager(new LinearLayoutManager(fragment.requireContext()));
        container.setAdapter(lisViewAdapter);

        ImageView closeButton = view.findViewById(R.id.close_list);
        closeButton.setOnClickListener(v -> {
            v.animate().setDuration(200).scaleXBy(1).scaleYBy(1).rotationBy(360).alpha(0).setListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dismiss();

                }
            });
        });
    }
}
