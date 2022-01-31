package cz.deepvision.iti.is.ui.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import java.util.List;
import java.util.stream.Collectors;

import cz.deepvision.iti.is.OnLoadMoreListener;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.victims.ListViewItem;
import cz.deepvision.iti.is.ui.events.EventsFragment;
import cz.deepvision.iti.is.ui.home.SearchAdapter;
import cz.deepvision.iti.is.ui.places.PlacesFragment;

public class SearchDialog extends DialogFragment {
    private Dialog dialog;
    private List<ListViewItem> items;
    private List<ListViewItem> allItems;
    private int lastVisibleItem, totalItemCount;
    private Fragment fragment;
    private SearchAdapter searchAdapter;



    private int batch = 1;
    private OnLoadMoreListener onLoadMoreListener;


    public SearchDialog(List<ListViewItem> items, Fragment myFragment) {
        this.allItems = items;
        this.items = items;
        this.fragment = myFragment;
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
        return inflater.inflate(R.layout.search_dialog, null);
    }

    private void updateData(String type) {
        List<ListViewItem> collect = allItems.stream().filter(listViewItem -> listViewItem.getType().equals(type)).limit(20L * batch).collect(Collectors.toList());
        searchAdapter.updateItems(collect);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchAdapter = new SearchAdapter(items, fragment);

        RadioGroup allTags = view.findViewById(R.id.tagGroup);
        RadioButton defaultSelect = view.findViewById(R.id.victimTag);

        String type = "entity";
        if (fragment instanceof EventsFragment) {
            defaultSelect = view.findViewById(R.id.eventTag);
            type = "event";
        } else if (fragment instanceof PlacesFragment) {
            defaultSelect = view.findViewById(R.id.placeTag);
            type = "place";
        }

        defaultSelect.setChecked(true);
        updateData(type);



        Button loadMoreItems = view.findViewById(R.id.load_more_places);
        loadMoreItems.setOnClickListener(view1 -> {
            loadMoreItems.setVisibility(View.INVISIBLE);
            batch++;
            changeAdapterData(view, allTags);
        });

        allTags.setOnCheckedChangeListener((radioGroup, i) -> {
            batch = 1;
            loadMoreItems.setVisibility(View.INVISIBLE);
            changeAdapterData(view, allTags);
        });


        RecyclerView container = view.findViewById(R.id.person_list);
        container.addItemDecoration(new DividerItemDecoration(fragment.requireContext(), DividerItemDecoration.VERTICAL));
        container.setHasFixedSize(true);
        container.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        container.setAdapter(searchAdapter);

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


        container.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        container.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final GridLayoutManager gridLayoutManager = (GridLayoutManager) container.getLayoutManager();

                totalItemCount = gridLayoutManager.getItemCount();
                lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();

                if (lastVisibleItem > totalItemCount - 2 && (items.size() > 20)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.showButton();
                    }
                } else if (lastVisibleItem < totalItemCount - 5)
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.hideButton();
                    }
            }
        });
        setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void hideButton() {
                startAnimation(container, View.GONE);
            }

            @Override
            public void showButton() {
                startAnimation(container, View.VISIBLE);
            }

            private void startAnimation(ViewGroup root, int visibility) {
                Transition transition = new Slide(Gravity.BOTTOM);
                transition.setDuration(600);
                transition.addTarget(R.id.load_more_places);

                TransitionManager.beginDelayedTransition(root, transition);
                loadMoreItems.setVisibility(visibility);
            }
        });
    }

    private void changeAdapterData(@NonNull View view, RadioGroup allTags) {
        RadioButton radioButton = view.findViewById(allTags.getCheckedRadioButtonId());
        Log.e("Text", radioButton.getText().toString());
        String type = "entity";
        if (radioButton.getText().toString().equals("Místa")) type = "place";
        if (radioButton.getText().toString().equals("Události")) type = "event";

        updateData(type);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }
}
