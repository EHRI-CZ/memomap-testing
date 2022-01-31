package cz.deepvision.iti.is;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cz.deepvision.iti.is.graphql.SearchByFullTextQuery;
import cz.deepvision.iti.is.models.DataGenerator;
import cz.deepvision.iti.is.models.victims.ListViewItem;
import cz.deepvision.iti.is.ui.dialog.SearchDialog;
import cz.deepvision.iti.is.ui.events.EventsFragment;
import cz.deepvision.iti.is.ui.home.HomeFragment;
import cz.deepvision.iti.is.ui.places.PlacesFragment;
import cz.deepvision.iti.is.ui.victims.VictimsFragment;
import cz.deepvision.iti.is.util.NetworkConnection;
import io.realm.BuildConfig;

public class MainActivity extends AppCompatActivity {
    private Context ctx;
    private Handler mHandler = new Handler();
    private ProgressBar progressBar;
    private BottomSheetDialog builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;
        BottomNavigationView navView = findViewById(R.id.nav_view);
        progressBar = findViewById(R.id.search_progress_bar);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_victims, R.id.navigation_places, R.id.navigation_events, R.id.navigation_about).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        if (BuildConfig.DEBUG) {
            DataGenerator generator = new DataGenerator();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_nav_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setTitle("Zadejte text");

        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            ((SearchView) item.getActionView()).setOnQueryTextListener(new OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String text) {
                    if (!text.isEmpty()) {
                        progressBar.setVisibility(View.VISIBLE);
                        mHandler.removeCallbacksAndMessages(null);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showList(text);
                                mHandler.removeCallbacks(this);
                            }
                        }, 1500);
                    }
                    return true;
                }
            });
        }
        return true;
    }

    private void showList(String text) {
        NetworkConnection.getInstance().getApolloClient().query(new SearchByFullTextQuery(text)).enqueue(new ApolloCall.Callback<SearchByFullTextQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<SearchByFullTextQuery.Data> response) {
                if (Looper.myLooper() == null)
                    Looper.prepare();
                if (response.data() != null && response.data().fullText().size() > 0) {
                    runOnUiThread(() -> {

                        Fragment myFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment).getChildFragmentManager().getPrimaryNavigationFragment();
                        List<ListViewItem> listViewItems = new ArrayList<>();

                        for (SearchByFullTextQuery.FullText fullText : response.data().fullText()) {
                            if (myFragment instanceof VictimsFragment && fullText.type().equals("entity"))
                                listViewItems.add(new ListViewItem(fullText.id(), fullText.label(), fullText.type()));
                            else if (myFragment instanceof PlacesFragment && fullText.type().equals("place"))
                                listViewItems.add(new ListViewItem(fullText.id(), fullText.label(), fullText.type()));
                            else if (myFragment instanceof EventsFragment && fullText.type().equals("event"))
                                listViewItems.add(new ListViewItem(fullText.id(), fullText.label(), fullText.type()));
                            else if (myFragment instanceof HomeFragment) {
                                listViewItems.add(new ListViewItem(fullText.id(), fullText.label(), fullText.type()));
                            }
                        }
                        SearchDialog searchDialog = new SearchDialog(listViewItems,myFragment);
                        searchDialog.show(getSupportFragmentManager(), "dialog_list");
                    });
                } else
                    Toast.makeText(ctx, "Nebyli nalazeny žádné výsledky", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("IS", e.getMessage());
            }
        });
    }

    private BroadcastReceiver bridgeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SEND)) {
                if (builder != null)
                    builder.dismiss();
                Bundle bundle = new Bundle();
                bundle.putDoubleArray("location", intent.getDoubleArrayExtra("location"));
                Log.d("Broadcast", "arriaved");
                NavController navController = Navigation.findNavController((Activity) ctx, R.id.nav_host_fragment);
                navController.navigate(R.id.navigation_home, bundle);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SEND);
        registerReceiver(bridgeReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bridgeReceiver != null) unregisterReceiver(bridgeReceiver);
    }
}