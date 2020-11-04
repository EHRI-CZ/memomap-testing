package cz.deepvision.iti.is.ui.victims;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
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

public class VictimsFragment extends Fragment {

    private VictimsViewModel victimsViewModel;
    private RecyclerView recyclerView;
    private VictimsAdapter adapter;
    private List<RecordListItem> itemList;
    private Button loadMoreRecords;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        victimsViewModel = ViewModelProviders.of(this).get(VictimsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_victims, container, false);
//        final TextView textView = root.findViewById(R.id.text_dashboard);
//        victimsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        loadMoreRecords = root.findViewById(R.id.load_more_places);
        loadMoreRecords.setVisibility(View.GONE);
        loadMoreRecords.setOnClickListener(view -> {
            if (itemList.size() <= 20) {
                itemList.add(null);
                adapter.notifyItemInserted(itemList.size() - 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        itemList.remove(itemList.size() - 1);
//                        adapter.notifyItemRemoved(itemList.size());
                        //LOAD DATA
                        victimsViewModel.loadData();
                        adapter.notifyDataSetChanged();
                    }
                }, 5000);
            } else {
                victimsViewModel.loadData();
                startAnimation(container, View.GONE);
                adapter.notifyDataSetChanged();

            }
        });

        victimsViewModel.getItems().observe(getViewLifecycleOwner(), new Observer<List<RecordListItem>>() {
            @Override
            public void onChanged(List<RecordListItem> victimListItems) {
                Log.d("IS", "Items changed");
                itemList.addAll(victimListItems);
                adapter.notifyDataSetChanged();
                if (itemList.size() > 24)
                    Toast.makeText(getContext(), "Data byla načtena", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        itemList = new ArrayList<>();
        adapter = new VictimsAdapter(recyclerView, itemList, getActivity(), this);

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void hideButton() {
                if (loadMoreRecords.getVisibility() == View.VISIBLE)
                    startAnimation(container, View.GONE);
            }

            @Override
            public void showButton() {
                startAnimation(container, View.VISIBLE);
            }

        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
//        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(getContext(),
//                DividerItemDecoration.VERTICAL);
//        Drawable horizontalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.space_divider);
//        horizontalDecoration.setDrawable(horizontalDivider);
//        recyclerView.addItemDecoration(horizontalDecoration);
    }


    private void startAnimation(ViewGroup root, int visibility) {
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(700);
        transition.addTarget(R.id.load_more_places);

        TransitionManager.beginDelayedTransition(root, transition);
        loadMoreRecords.setVisibility(visibility);
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}