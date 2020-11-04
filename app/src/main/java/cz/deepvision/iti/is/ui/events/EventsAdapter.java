package cz.deepvision.iti.is.ui.events;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import cz.deepvision.iti.is.OnLoadMoreListener;
import cz.deepvision.iti.is.OnShowAnotherElement;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.graphql.EventDetailQuery;
import cz.deepvision.iti.is.models.Event;
import cz.deepvision.iti.is.models.victims.RecordListItem;
import cz.deepvision.iti.is.ui.dialog.EventDialog;
import cz.deepvision.iti.is.ui.places.PlacesAdapter;
import cz.deepvision.iti.is.util.NetworkConnection;
import cz.deepvision.iti.is.util.Requester;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener onLoadMoreListener;
    private Activity activity;
    private List<RecordListItem> items;
    private int lastVisibleItem, totalItemCount;
    private Fragment fragment;

    public EventsAdapter(RecyclerView recyclerView, List<RecordListItem> items, Activity activity, EventsFragment eventsFragment) {
        this.items = items;
        this.activity = activity;
        fragment = eventsFragment;

        final GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = gridLayoutManager.getItemCount();
                lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();

                if (lastVisibleItem > totalItemCount - 2) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.showButton();
                    }
                } else if (totalItemCount < 23) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.showButton();
                    }
                } else if (lastVisibleItem < totalItemCount - 5)
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.hideButton();
                    }
            }
        });
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }


    @Override
    public int getItemViewType(int position) {
        return items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.fragment_list_item, parent, false);
            return new EventsAdapter.ItemViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_loading, parent, false);
            return new EventsAdapter.LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EventsAdapter.ItemViewHolder) {
            RecordListItem item = items.get(position);
            EventsAdapter.ItemViewHolder itemViewHolder = (EventsAdapter.ItemViewHolder) holder;
            itemViewHolder.setUpEventData(item.getKey(), item.getUrl(), item.getLabel(), position);
        } else if (holder instanceof EventsAdapter.LoadingViewHolder) {
            EventsAdapter.LoadingViewHolder loadingViewHolder = (EventsAdapter.LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout card;
        TextView title;
        ImageView icon;
        public Event data;
        private String url;
        private String key;
        private int position;
        private EventDialog eventDialog;

        ItemViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.textview);
            card = view.findViewById(R.id.row_root_element);
            icon = view.findViewById(R.id.imageView);
            card.setOnClickListener(view1 -> {
                eventDialog = new EventDialog(fragment, data, false,2);
                eventDialog.setOnShowAnotherElement(new OnShowAnotherElement() {
                    @Override
                    public void showNext() {
                        if (position + 1 <= items.size())
                            showAnotherElement(items.get(position++).getKey());
                    }

                    @Override
                    public void showPrevious() {
                        if (position - 1 != 0)
                            showAnotherElement(items.get(position--).getKey());
                    }
                });
                NetworkConnection.getInstance().getApolloClient().query(new EventDetailQuery(key)).enqueue(new ApolloCall.Callback<EventDetailQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<EventDetailQuery.Data> response) {
                        if (response.data() != null && response.data().eventDetail() != null) {
                            EventDetailQuery.EventDetail responseData = response.data().eventDetail();
                            data = new Event(responseData);
                            fragment.getActivity().runOnUiThread(() -> eventDialog.updateData(data));
                            eventDialog.show(fragment.getChildFragmentManager(), "dialog_fullscreen");
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.e("IS", e.getMessage());
                    }
                });
            });
        }

        public void setUpEventData(String key, String url, String label, int position) {
            this.key = key;
            this.url = url;
            this.title.setText(label);
            this.position = position;
            if (url != null) {
                String substring = url.substring(1);
                Requester requester = new Requester(fragment.getActivity());
                requester.makeRequestForAdapter(substring, icon);
            } else icon.setImageDrawable(activity.getDrawable(R.drawable.no_portrait_icon));
        }

        public void showAnotherElement(String key) {
            NetworkConnection.getInstance().getApolloClient().query(new EventDetailQuery(key)).enqueue(new ApolloCall.Callback<EventDetailQuery.Data>() {

                @Override
                public void onResponse(@NotNull Response<EventDetailQuery.Data> response) {
                    EventDetailQuery.EventDetail responseData = response.data().eventDetail();
                    data = new Event(responseData);
                    eventDialog.updateData(data);
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());

                }
            });
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
        }
    }
}