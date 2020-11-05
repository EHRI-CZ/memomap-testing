package cz.deepvision.iti.is.ui.events;

import android.graphics.Paint;
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

import cz.deepvision.iti.is.OnLoadMoreListener;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.victims.RecordListItem;

import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {

    private EventsViewModel eventsViewModel;
    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private List<RecordListItem> itemList;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        eventsViewModel = ViewModelProviders.of(this).get(EventsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_events, container, false);
//        final TextView textView = root.findViewById(R.id.text_dashboard);
//        eventsViewModel.getItems().observe(getViewLifecycleOwner(), new Observer<List<RecordListItem>>() {
//            @Override
//            public void onChanged(List<RecordListItem> eventListItems) {
//                Log.d("IS", "Items changed");
//                itemList.addAll(eventListItems);
//                adapter.notifyDataSetChanged();
//                if (itemList.size() > 23)
//                    Toast.makeText(getContext(), "Data byla načtena", Toast.LENGTH_SHORT).show();
//                else
//                    Toast.makeText(getContext(), "Žádná další data k dispozici", Toast.LENGTH_SHORT).show();
//            }
//        });

        final Button button = root.findViewById(R.id.load_more_places);
        button.setVisibility(View.GONE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventsViewModel.loadData();
                adapter.notifyDataSetChanged();
                button.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Data byla načtena", Toast.LENGTH_SHORT).show();
            }
        });

        eventsViewModel.getItems().observe(getViewLifecycleOwner(), new Observer<List<RecordListItem>>() {
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
        adapter = new EventsAdapter(recyclerView, itemList, getActivity(), this);

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
//        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
//        Drawable horizontalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.divider);
//        horizontalDecoration.setDrawable(horizontalDivider);
//        recyclerView.addItemDecoration(horizontalDecoration);
    }

    public Paint getPaint(int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        return paint;
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
