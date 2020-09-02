package cz.deepvision.iti.is.ui.home;

import android.Manifest;
import android.app.Application;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import com.warkiz.widget.IndicatorSeekBar;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.Location;

public class HomeFragment extends Fragment {
    private final String[] mPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET};
    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(mPermission, 1);
        }
        Location location = null;
        if(this.getArguments() != null){
            location = new Location(this.getArguments().getDoubleArray("location"));
        }
            homeViewModel = ViewModelProviders.of(this, new HomeViewModelFactory((Application) getContext().getApplicationContext(), location)).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        homeViewModel.setmFragment(this);

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


        IndicatorSeekBar seekBar = root.findViewById(R.id.seekBar);
        seekBar.setIndicatorTextFormat("${TICK_TEXT}");

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
                final AlertDialog dialog = builder.create();
                builder.setTitle("Práva k použití");
                builder.setMessage("Pro aktualizaci mapy v reálném čase a zobrazení dat, je nutné udělit aplikaci oprávnění");
                builder.setPositiveButton("Potvrdit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        requestPermissions(mPermission, 1);
                    }
                });
                builder.setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        }
    }
}