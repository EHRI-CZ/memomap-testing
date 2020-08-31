package cz.deepvision.iti.is.ui.places;

import android.os.Bundle;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        final TextView textView = root.findViewById(R.id.text_dashboard);
        placesViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        final Button button = root.findViewById(R.id.load_more_places);
        button.setVisibility(View.GONE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemList.size() <= 20) {
                    itemList.add(null);
                    adapter.notifyItemInserted(itemList.size() - 1);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            itemList.remove(itemList.size() - 1);
                            adapter.notifyItemRemoved(itemList.size());
                            //LOAD DATA
                            placesViewModel.loadData();
                            adapter.notifyDataSetChanged();
                            adapter.setLoaded();
                        }
                    }, 5000);
                }else {
                    placesViewModel.loadData();
                    adapter.notifyDataSetChanged();
                    button.setVisibility(View.GONE);
                }
            }
        });

        placesViewModel.getItems().observe(getViewLifecycleOwner(), new Observer<List<RecordListItem>>() {
            @Override
            public void onChanged(List<RecordListItem> victimListItems) {
                Log.d("IS", "Items changed");
                itemList.addAll(victimListItems);
                adapter.notifyDataSetChanged();
                adapter.setLoaded();
            }
        });
        recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        //itemList = dashboardViewModel.loadData();
        itemList = new ArrayList<>();
        adapter = new PlacesAdapter(recyclerView, itemList, getActivity(), this);

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (itemList.size() <= 20) {
                    itemList.add(null);
                    adapter.notifyItemInserted(itemList.size() - 1);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            itemList.remove(itemList.size() - 1);
                            adapter.notifyItemRemoved(itemList.size());
                            //LOAD DATA
                            placesViewModel.loadData();
                            adapter.notifyDataSetChanged();
                            adapter.setLoaded();
                        }
                    }, 5000);
                } else {
                    button.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), "Loading data completed", Toast.LENGTH_SHORT).show();
                }
                button.setVisibility(View.VISIBLE);
            }

            @Override
            public void hideButton() {
                startAnimation(container,View.GONE);
            }

            @Override
            public void showButton() {
                startAnimation(container,View.VISIBLE);
            }

            private void startAnimation(ViewGroup root,int visibility) {
                AutoTransition transition = new AutoTransition();
                transition.setDuration(200);
                transition.setInterpolator(new AccelerateDecelerateInterpolator());
                TransitionManager.beginDelayedTransition(root, transition);
                button.setVisibility(visibility);

            }
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        //realm = Realm.getDefaultInstance();

        //recyclerView.setAdapter(new RealmVictimsRecyclerViewAdapter(realm.where(Person.class).findAllAsync()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onStop() {
        super.onStop();
        //realm.close();
    }
}