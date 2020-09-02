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
import android.view.*;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import cz.deepvision.iti.is.graphql.SearchByFullTextQuery;
import cz.deepvision.iti.is.models.DataGenerator;
import cz.deepvision.iti.is.models.victims.ListViewItem;
import cz.deepvision.iti.is.ui.home.LisViewAdapter;
import io.realm.BuildConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Context ctx;
    private Handler mHandler = new Handler();
    private BottomSheetDialog builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setItemIconTintList(null);
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

                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.postDelayed(() -> {
                        showList(text);
                        Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
                    }, 1000);
                    return true;
                }

                ;
            });
        }
        return true;
    }

    private void showList(String text) {
        ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://77.236.207.194:8529/_db/ITI_DV/iti").build();
        apolloClient.query(new SearchByFullTextQuery(text)).enqueue(new ApolloCall.Callback<SearchByFullTextQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<SearchByFullTextQuery.Data> response) {
                Looper.prepare();
                if (response.data() != null && response.data().fullText() != null) {
                    runOnUiThread(() -> {
                        builder = new BottomSheetDialog(ctx);
                        View root = LayoutInflater.from(ctx).inflate(R.layout.custom_person_list, null);
                        List<ListViewItem> listViewItems = new ArrayList<>();
                        final String simpleName = getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager().getFragments().get(0).getClass().getSimpleName();
                        for (SearchByFullTextQuery.FullText fullText : response.data().fullText()) {
                            if (simpleName.equals("VictimsFragment") && fullText.type().equals("entity"))
                                listViewItems.add(new ListViewItem(fullText.id(), fullText.label(), fullText.type()));
                            else if (simpleName.equals("PlacesFragment") && fullText.type().equals("place"))
                                 listViewItems.add(new ListViewItem(fullText.id(), fullText.label(), fullText.type()));
                            else if (simpleName.equals("EventsFragment") && fullText.type().equals("event"))
                                listViewItems.add(new ListViewItem(fullText.id(), fullText.label(), fullText.type()));
                            else if(simpleName.equals("HomeFragment")){
                                listViewItems.add(new ListViewItem(fullText.id(), fullText.label(), fullText.type()));
                            }
                        }
                        LisViewAdapter lisViewAdapter = new LisViewAdapter(listViewItems, getSupportFragmentManager().getPrimaryNavigationFragment());
                        RecyclerView container = root.findViewById(R.id.person_list);
                        container.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL));

                        container.setHasFixedSize(true);
                        container.setLayoutManager(new LinearLayoutManager(ctx));

                        container.setAdapter(lisViewAdapter);
                        builder.setContentView(root);

                        if (listViewItems.size() > 0) if (!builder.isShowing()) builder.show();

                        Button button = root.findViewById(R.id.btn_close_list);
                        button.setOnClickListener(view -> builder.dismiss());
                    });
                }
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
                builder.dismiss();
                Bundle bundle = new Bundle();
                bundle.putDoubleArray("location",intent.getDoubleArrayExtra("location"));
                Log.d("Broadcast", "arriaved");
                NavController navController = Navigation.findNavController((Activity) ctx, R.id.nav_host_fragment);
                navController.navigate(R.id.navigation_home,bundle);
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