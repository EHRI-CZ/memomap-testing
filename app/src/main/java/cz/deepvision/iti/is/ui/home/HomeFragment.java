package cz.deepvision.iti.is.ui.home;

import android.Manifest;
import android.app.Application;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.Location;

public class HomeFragment extends Fragment {
    private final String[] mPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET};
    private HomeViewModel homeViewModel;
    private boolean progressBarMoved = false;
    private int timeElapsed = 0;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(mPermission, 1);
        }
        Location location = null;
        if (this.getArguments() != null) {
            location = new Location(this.getArguments().getDoubleArray("location"));
        }
        homeViewModel = ViewModelProviders.of(this, new HomeViewModelFactory((Application) getContext().getApplicationContext(), location)).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
//        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        homeViewModel.setmFragment(this);

        IndicatorSeekBar seekBar = root.findViewById(R.id.seekBar);
        seekBar.setIndicatorTextFormat("${TICK_TEXT}");
        seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
                progressBarMoved = true;

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Fragment myFragment = null;
                if (getParentFragment() != null) {
                    myFragment = (Fragment) getParentFragment().getChildFragmentManager().findFragmentById(R.id.nav_host_fragment);
                }
                if (myFragment != null && myFragment instanceof HomeFragment) {
                    Log.e("Time elapsed", String.valueOf(timeElapsed));
                    timeElapsed++;
                    if (!progressBarMoved)
                        handler.postDelayed(this, 1000);
                }

                if (!progressBarMoved && myFragment != null && timeElapsed == 30) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                    builder.setTitle("Tip");
                    builder.setMessage("Zkuste pohnout časovou osou");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Ok", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    });
                    builder.show();
                    progressBarMoved = true;
                    handler.removeCallbacks(this);
                }
            }
        };
        handler.postDelayed(runnable, 1000);

        // R.id.map is a FrameLayout, not a Fragment
        getChildFragmentManager().beginTransaction().replace(R.id.map, homeViewModel.getMapFragment()).commit();
        CheckBox victims = root.findViewById(R.id.checkBoxVictims);
        CheckBox events = root.findViewById(R.id.checkBoxEvents);
        CheckBox places = root.findViewById(R.id.checkBoxPlaces);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    ((CheckBox) view).setTextColor(getContext().getColor(R.color.iti_orange));
                    ((CheckBox) view).setButtonTintList(ContextCompat.getColorStateList(getContext(), R.color.iti_orange));
                } else {
                    ((CheckBox) view).setTextColor(getContext().getColor(R.color.colorAccent));
                    ((CheckBox) view).setButtonTintList(ContextCompat.getColorStateList(getContext(), R.color.colorAccent));
                }
                Boolean[] filters = new Boolean[3];
                filters[0] = victims.isChecked();
                filters[1] = events.isChecked();
                filters[2] = places.isChecked();
                homeViewModel.updateFilters(filters);
            }
        };
        victims.setOnClickListener(listener);
        events.setOnClickListener(listener);
        places.setOnClickListener(listener);

        return root;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this.getContext(), "Práva k použití udělana", Toast.LENGTH_SHORT).show();
                homeViewModel.setUpMapUpdateListener();
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Práva k použití");
                builder.setMessage("Pro aktualizaci mapy v reálném čase a zobrazení dat, je nutné udělit aplikaci oprávnění");
                builder.setPositiveButton("Potvrdit", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    requestPermissions(mPermission, 1);
                });
                builder.setNegativeButton("Zrušit", (dialogInterface, i) -> dialogInterface.dismiss());
                builder.show();
            }
        }
    }
}