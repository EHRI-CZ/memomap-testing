package cz.deepvision.iti.is.ui.events;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.deepvision.iti.is.OnLoadMoreListener;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.victims.RecordListItem;

import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {

    private EventsViewModel eventsViewModel;
    //private Realm realm;
    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private List<RecordListItem> itemList;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        eventsViewModel = ViewModelProviders.of(this).get(EventsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_events, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        eventsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
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
                            eventsViewModel.loadData();
                            adapter.notifyDataSetChanged();
                            adapter.setLoaded();
                        }
                    }, 5000);
                }else {
                    eventsViewModel.loadData();
                    adapter.notifyDataSetChanged();
                    button.setVisibility(View.GONE);
                }
            }
        });

        eventsViewModel.getItems().observe(getViewLifecycleOwner(), new Observer<List<RecordListItem>>() {
            @Override
            public void onChanged(List<RecordListItem> victimListItems) {
                Log.d("IS", "Items changed");
                itemList.addAll(victimListItems);
                adapter.notifyDataSetChanged();
                adapter.setLoaded();
                if(itemList.size() > 23)
                    Toast.makeText(getContext(), "Data byla načtena", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        //itemList = dashboardViewModel.loadData();
        itemList = new ArrayList<>();
        adapter = new EventsAdapter(recyclerView, itemList, getActivity(), this);

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
                            eventsViewModel.loadData();
                            adapter.notifyDataSetChanged();
                            adapter.setLoaded();
                        }
                    }, 5000);
                }
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
//        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL));
//        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onStop() {
        super.onStop();
        //realm.close();
    }
}
