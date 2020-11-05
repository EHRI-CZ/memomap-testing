package cz.deepvision.iti.is.ui.victims;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import cz.deepvision.iti.is.OnLoadMoreListener;
import cz.deepvision.iti.is.OnShowAnotherElement;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.graphql.EntityDetailQuery;
import cz.deepvision.iti.is.models.victims.Person;
import cz.deepvision.iti.is.models.victims.RecordListItem;
import cz.deepvision.iti.is.ui.dialog.VictimDialog;
import cz.deepvision.iti.is.util.NetworkConnection;
import cz.deepvision.iti.is.util.Requester;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.ConsoleHandler;

public class VictimsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener onLoadMoreListener;
    private Activity activity;
    private List<RecordListItem> items;
    private int lastVisibleItem, totalItemCount;
    private Fragment fragment;

    public VictimsAdapter(RecyclerView recyclerView, List<RecordListItem> items, Activity activity, VictimsFragment victimsFragment) {
        this.items = items;
        this.activity = activity;
        fragment = victimsFragment;

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
            return new ItemViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            RecordListItem item = items.get(position);
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.setUpPersonData(item.getKey(), item.getUrl(), item.getLabel(), position);
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
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
        private int position;
        private VictimDialog victimDialog;

        ItemViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.textview);
            card = view.findViewById(R.id.row_root_element);
            icon = view.findViewById(R.id.imageView);
            card.setOnClickListener(view1 -> {

                NetworkConnection.getInstance().getApolloClient().query(new EntityDetailQuery(key)).enqueue(new ApolloCall.Callback<EntityDetailQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<EntityDetailQuery.Data> response) {
                        if (response.data() != null && response.data().entityDetail() != null) {
                            EntityDetailQuery.EntityDetail responseData = response.data().entityDetail();
                            data = new Person(responseData);

                            victimDialog = new VictimDialog(fragment, data, false, 0);
                            fragment.getActivity().runOnUiThread(() -> victimDialog.updateData(data));

                            victimDialog.setOnShowAnotherElement(onShowAnotherElement);
                            victimDialog.show(fragment.getChildFragmentManager(), VictimDialog.class.getName());

                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.e("IS", e.getMessage());
                    }
                });
                card.animate().setDuration(200).alpha(0.8f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        card.setAlpha(1f);
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

        public void setUpPersonData(String key, String url, String label, int position) {
            this.key = key;
            this.url = url;
            this.title.setText(label);
            this.position = position;
            if (url != null) {
                Requester requester = new Requester(fragment.getActivity());
                requester.makeRequestForAdapter(url, icon);
            } else icon.setImageDrawable(activity.getDrawable(R.drawable.no_portrait_icon));
        }

        public void showAnotherElement(String key,int style) {

            NetworkConnection.getInstance().getApolloClient().query(new EntityDetailQuery(key)).enqueue(new ApolloCall.Callback<EntityDetailQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<EntityDetailQuery.Data> response) {
                    EntityDetailQuery.EntityDetail responseData = response.data().entityDetail();
                    data = new Person(responseData);
                    VictimDialog dialog = new VictimDialog(fragment, data,style);

                    dialog.show(fragment.getChildFragmentManager(), VictimDialog.class.getName());
                    dialog.setOnShowAnotherElement(onShowAnotherElement);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> {
                        victimDialog.dismiss();
                        victimDialog = dialog;
                    },500);
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