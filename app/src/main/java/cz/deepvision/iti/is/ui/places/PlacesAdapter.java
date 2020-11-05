package cz.deepvision.iti.is.ui.places;

import android.app.Activity;
import android.os.Handler;
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
import cz.deepvision.iti.is.OnShowAnotherElement;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.graphql.EntityDetailQuery;
import cz.deepvision.iti.is.graphql.PlaceDetailQuery;
import cz.deepvision.iti.is.models.Place;
import cz.deepvision.iti.is.models.victims.Person;
import cz.deepvision.iti.is.models.victims.RecordListItem;
import cz.deepvision.iti.is.ui.dialog.PlaceDialog;
import cz.deepvision.iti.is.ui.dialog.VictimDialog;
import cz.deepvision.iti.is.util.NetworkConnection;
import cz.deepvision.iti.is.util.Requester;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener onLoadMoreListener;
    private Activity activity;
    private List<RecordListItem> items;
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

                if (lastVisibleItem > totalItemCount - 2) {
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
            itemViewHolder.setUpPlaceData(item.getKey(), item.getUrl(), item.getLabel(),position);
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
        public Place data;
        private String url;
        private String key;
        private int position;
        private PlaceDialog placeDialog;


        ItemViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.textview);
            card = view.findViewById(R.id.row_root_element);
            icon = view.findViewById(R.id.imageView);
            card.setOnClickListener(view1 -> {
                NetworkConnection.getInstance().getApolloClient().query(new PlaceDetailQuery(key)).enqueue(new ApolloCall.Callback<PlaceDetailQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<PlaceDetailQuery.Data> response) {
                        if (response.data() != null && response.data().placeDetail() != null) {
                            PlaceDetailQuery.PlaceDetail responseData = response.data().placeDetail();
                            Place data = new Place(responseData);

                            placeDialog = new PlaceDialog(fragment, data,false,2);
                            fragment.getActivity().runOnUiThread(() -> placeDialog.updateData(data));

                            placeDialog.setOnShowAnotherElement(onShowAnotherElement);
                            placeDialog.show(fragment.getChildFragmentManager(), "dialog");
                        }

                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.e("IS", e.getMessage());
                    }
                });
            });
        }
        private OnShowAnotherElement onShowAnotherElement = new OnShowAnotherElement() {
            @Override
            public void showNext() {
                if (position + 1 < items.size())
                    showAnotherElement(items.get(++position).getKey(),-1);
            }

            @Override
            public void showPrevious() {
                if (position - 1 >= 0)
                    showAnotherElement(items.get(--position).getKey(),1);
            }
        };

        public void showAnotherElement(String key,int style) {

            NetworkConnection.getInstance().getApolloClient().query(new PlaceDetailQuery(key)).enqueue(new ApolloCall.Callback<PlaceDetailQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<PlaceDetailQuery.Data> response) {
                    PlaceDetailQuery.PlaceDetail responseData = response.data().placeDetail();
                    data = new Place(responseData);
                    PlaceDialog dialog = new PlaceDialog(fragment, data,style);

                    dialog.show(fragment.getChildFragmentManager(), VictimDialog.class.getName());
                    dialog.setOnShowAnotherElement(onShowAnotherElement);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> {
                        placeDialog.dismiss();
                        placeDialog = dialog;
                    },500);
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    Log.e("IS", e.getMessage());

                }
            });
        }
        public void setUpPlaceData(String key, String url, String label, int position) {
            this.key = key;
            this.url = url;
            this.title.setText(label);
            this.position = position;
            if (url != null) {
                Requester requester = new Requester(fragment.getActivity());
                requester.makeRequestForAdapter(url, icon);
            } else icon.setImageDrawable(activity.getDrawable(R.drawable.ic_baseline_home_96));
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