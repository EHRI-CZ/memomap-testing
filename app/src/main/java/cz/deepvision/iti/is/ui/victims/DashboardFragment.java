package cz.deepvision.iti.is.ui.victims;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import cz.deepvision.iti.is.models.victims.Person;
import cz.deepvision.iti.is.models.victims.VictimListItem;
import io.realm.Realm;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    //private Realm realm;
    private RecyclerView recyclerView;
    private VictimsAdapter adapter;
    private List<VictimListItem> itemList;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_victims, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        dashboardViewModel.getItems().observe(getViewLifecycleOwner(), new Observer<List<VictimListItem>>() {
            @Override
            public void onChanged(List<VictimListItem> victimListItems) {
                Log.d("IS", "Items changed");
                itemList.addAll(victimListItems);
                adapter.notifyDataSetChanged();
                adapter.setLoaded();
            }
        });
        recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        //itemList = dashboardViewModel.loadData();
        itemList = new ArrayList<>();
        adapter = new VictimsAdapter(recyclerView,itemList,getActivity());

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
                            dashboardViewModel.loadData();
                            adapter.notifyDataSetChanged();
                            adapter.setLoaded();
                        }
                    }, 5000);
                } else {
                    Toast.makeText(getActivity(), "Loading data completed", Toast.LENGTH_SHORT).show();
                }
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