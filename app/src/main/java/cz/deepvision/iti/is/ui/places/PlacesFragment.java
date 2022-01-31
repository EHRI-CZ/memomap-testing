package cz.deepvision.iti.is.ui.places;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import java.util.ArrayList;
import java.util.List;

import cz.deepvision.iti.is.OnLoadMoreListener;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.victims.RecordListItem;

public class PlacesFragment extends Fragment {
    private PlacesViewModel placesViewModel;
    private RecyclerView recyclerView;
    private PlacesAdapter adapter;
    private List<RecordListItem> itemList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        placesViewModel =
                ViewModelProviders.of(this).get(PlacesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_places, container, false);

        final Button button = root.findViewById(R.id.load_more_places);
        button.setVisibility(View.GONE);

        button.setOnClickListener(view -> {
                placesViewModel.loadData();
                adapter.notifyDataSetChanged();
                button.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Data byla načtena", Toast.LENGTH_SHORT).show();
        });

        placesViewModel.getItems().observe(getViewLifecycleOwner(), new Observer<List<RecordListItem>>() {
            @Override
            public void onChanged(List<RecordListItem> victimListItems) {
                Log.d("IS", "Items changed");
                itemList.addAll(victimListItems);
                adapter.notifyDataSetChanged();
            }
        });
        recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        itemList = new ArrayList<>();
        adapter = new PlacesAdapter(recyclerView, itemList, getActivity(), this);

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
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
                button.setVisibility(visibility);
            }
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}