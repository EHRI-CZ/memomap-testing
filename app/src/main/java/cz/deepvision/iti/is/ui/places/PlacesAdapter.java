package cz.deepvision.iti.is.ui.places;

import android.app.Activity;
import android.os.Looper;
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
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.graphql.PlaceDetailQuery;
import cz.deepvision.iti.is.models.Place;
import cz.deepvision.iti.is.models.victims.Person;
import cz.deepvision.iti.is.models.victims.RecordListItem;
import cz.deepvision.iti.is.ui.dialog.PlaceDialog;
import cz.deepvision.iti.is.util.Requester;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean isLoading;
    private Activity activity;
    private List<RecordListItem> items;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private Fragment fragment;

    public PlacesAdapter(RecyclerView recyclerView, List<RecordListItem> items, Activity activity, PlacesFragment placesFragment) {
        this.items = items;
        this.activity = activity;
        fragment = placesFragment;

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
              /*  if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }*/
                if(lastVisibleItem > totalItemCount-4){
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.showButton();
                    }
                }
                if (dy < 0) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.hideButton();
                    }
                }
            }
        });
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }

    public void setLoaded() {
        isLoading = false;
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
            return new PlacesAdapter.ItemViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_loading, parent, false);
            return new PlacesAdapter.LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PlacesAdapter.ItemViewHolder) {
            RecordListItem item = items.get(position);
            PlacesAdapter.ItemViewHolder itemViewHolder = (PlacesAdapter.ItemViewHolder) holder;
            itemViewHolder.setUpPlaceData(item.getKey(), item.getUrl(), item.getLabel());
        } else if (holder instanceof PlacesAdapter.LoadingViewHolder) {
            PlacesAdapter.LoadingViewHolder loadingViewHolder = (PlacesAdapter.LoadingViewHolder) holder;
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
        public Person data;
        private String url;
        private String key;


        ItemViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.textview);
            card = view.findViewById(R.id.row_root_element);
            icon = view.findViewById(R.id.imageView);
            card.setOnClickListener(view1 -> {
                PlaceDialog placeDialog = new PlaceDialog(fragment, false);
                ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://77.236.207.194:8529/_db/ITI_DV/iti").build();
                apolloClient.query(new PlaceDetailQuery(key)).enqueue(new ApolloCall.Callback<PlaceDetailQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<PlaceDetailQuery.Data> response) {
                        Looper.prepare();
                        if (response.data() != null && response.data().placeDetail() != null) {
                            PlaceDetailQuery.PlaceDetail responseData = response.data().placeDetail();
                            Place data = new Place(responseData);
                            fragment.getActivity().runOnUiThread(() -> placeDialog.updateData(data));
                            placeDialog.show(fragment.getActivity().getSupportFragmentManager(), "dialog");
                        }

                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.e("IS", e.getMessage());
                    }
                });
            });
        }

        public void setUpPlaceData(String key, String url, String label) {
            this.key = key;
            this.url = url;
            this.title.setText(label);
            if (url != null) {
                Requester requester = new Requester(fragment.getActivity());
                requester.makeRequestForAdapter(url, icon);
            } else icon.setImageDrawable(activity.getDrawable(R.drawable.no_portrait_icon));

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