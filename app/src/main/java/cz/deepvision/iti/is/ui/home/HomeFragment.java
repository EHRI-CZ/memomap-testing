package cz.deepvision.iti.is.ui.home;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import cz.deepvision.iti.is.BaseApp;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.Location;

public class HomeFragment extends Fragment {
    private final String[] mPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private HomeViewModel homeViewModel;
    private boolean filtersEnabled = false;

    //private boolean progressBarMoved = false;
    //private int timeElapsed = 0;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Location location = null;
        if (this.getArguments() != null) {
            location = new Location(this.getArguments().getDoubleArray("location"));
        }

        homeViewModel = ViewModelProviders.of(this, new HomeViewModelFactory((Application) getContext().getApplicationContext(), location)).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        homeViewModel.setmFragment(this);


        ImageView gpsLocationProvider = root.findViewById(R.id.gps_provider);
        gpsLocationProvider.bringToFront();

        gpsLocationProvider.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(mPermission, 1);
                return;
            }
            if (BaseApp.isGpsEnabled()) {
                BaseApp.setGpsEnabled(false);
                gpsLocationProvider.setImageResource(R.drawable.ic_poloha_pasivni);
                Toast.makeText(getContext(), "Lokalizační služby byly vypnuty", Toast.LENGTH_SHORT).show();
            } else {
                BaseApp.setGpsEnabled(true);
                gpsLocationProvider.setImageResource(R.drawable.ic_poloha_aktivni);
                Toast.makeText(getContext(), "Lokalizační služby byly zapnuty", Toast.LENGTH_SHORT).show();
            }
            homeViewModel.updateCurrentPosition(BaseApp.isGpsEnabled());

        });

        LinearLayout chechBoxesLayout = root.findViewById(R.id.check_group);

        ImageView filtersIcon = root.findViewById(R.id.filters);
        filtersIcon.bringToFront();

        filtersIcon.setOnClickListener(view -> {
            if (!filtersEnabled) {
               chechBoxesLayout.animate()
                       .translationXBy(-chechBoxesLayout.getWidth())
                       .setDuration(600)
                       .setInterpolator(new AccelerateDecelerateInterpolator())
                       .setListener(new AnimatorListenerAdapter() {
                           @Override
                           public void onAnimationEnd(Animator animation) {
                               view.setClickable(true);
                               filtersIcon.setImageResource(R.drawable.ic_filtr_aktivni);

                               super.onAnimationEnd(animation);
                           }

                           @Override
                           public void onAnimationStart(Animator animation) {
                               view.setClickable(false);
                               super.onAnimationStart(animation);

                           }
                       })
                       .start();
               filtersEnabled = true;
            } else {
                chechBoxesLayout.animate()
                        .translationXBy(chechBoxesLayout.getWidth())
                        .setDuration(600)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                view.setClickable(true);
                                filtersIcon.setImageResource(R.drawable.ic_filtr_pasivni);
                                super.onAnimationEnd(animation);
                            }

                            @Override
                            public void onAnimationStart(Animator animation) {
                                view.setClickable(false);
                                super.onAnimationStart(animation);

                            }
                        })
                        .start();
                filtersEnabled = false;

            }

        });

        CheckBox timelapse_checkbox = root.findViewById(R.id.timelapse_checkbox);
        timelapse_checkbox.setOnClickListener(view -> {
            BaseApp.setTimeLapseEnabled(!BaseApp.isTimeLapseEnabled());
        });

        IndicatorSeekBar seekBar = root.findViewById(R.id.seekBar);
        seekBar.setIndicatorTextFormat("${TICK_TEXT}");
        seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            private Integer year;

            @Override
            public void onSeeking(SeekParams seekParams) {
                //  progressBarMoved = true;
                year = Integer.valueOf(seekParams.tickText);
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                Log.e("YEAR-STOP", "true");
                homeViewModel.setYear(year);


            }
        });
        IndicatorSeekBar seekBarMonths = root.findViewById(R.id.seekBar2);
        seekBarMonths.setIndicatorTextFormat("${TICK_TEXT}");
        seekBarMonths.setOnSeekChangeListener(new OnSeekChangeListener() {
            private int month;

            @Override
            public void onSeeking(SeekParams seekParams) {
                month = convertMonthToInt(seekParams.tickText);
            }

            private int convertMonthToInt(String tickText) {
                int month = 1;
                if ("1 kvartál".equals(tickText)) month = 3;
                if ("2 kvartál".equals(tickText)) month = 6;
                if ("3 kvartál".equals(tickText)) month = 9;
                if ("4 kvartál".equals(tickText)) month = 12;

                return month;
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                homeViewModel.setMonth(month);

            }
        });

        // R.id.map is a FrameLayout, not a Fragment
        getChildFragmentManager().beginTransaction().replace(R.id.map, homeViewModel.getMapFragment()).commit();

        CheckBox victims = root.findViewById(R.id.checkBoxVictims);
        CheckBox events = root.findViewById(R.id.checkBoxEvents);
        CheckBox places = root.findViewById(R.id.checkBoxPlaces);

        victims.setChecked(BaseApp.getFilters()[0]);
        events.setChecked(BaseApp.getFilters()[1]);
        places.setChecked(BaseApp.getFilters()[2]);

        setCheckBox(victims);
        setCheckBox(events);
        setCheckBox(places);

        View.OnClickListener listener = view -> {
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
        };
        victims.setOnClickListener(listener);
        events.setOnClickListener(listener);
        places.setOnClickListener(listener);

        return root;
    }

    private void setCheckBox(View view){
        if (((CheckBox) view).isChecked()) {
            ((CheckBox) view).setTextColor(getContext().getColor(R.color.iti_orange));
            ((CheckBox) view).setButtonTintList(ContextCompat.getColorStateList(getContext(), R.color.iti_orange));
        } else {
            ((CheckBox) view).setTextColor(getContext().getColor(R.color.colorAccent));
            ((CheckBox) view).setButtonTintList(ContextCompat.getColorStateList(getContext(), R.color.colorAccent));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this.getContext(), "Práva k použití udělana", Toast.LENGTH_SHORT).show();
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