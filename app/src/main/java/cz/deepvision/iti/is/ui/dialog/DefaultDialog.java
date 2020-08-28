package cz.deepvision.iti.is.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.Location;
import cz.deepvision.iti.is.util.LayoutGenerator;
import cz.deepvision.iti.is.util.Requester;

public abstract class DefaultDialog extends DialogFragment implements Requester.UpdatePhoto {
    private Context ctx;
    private Dialog dialog;
    private boolean smallDialog;
    private ConstraintLayout root;
    private TextView name;
    private ImageView photo;
    private ImageView firstIcon;
    private ImageView secondIcon;
    private ImageView thirdIcon;
    private LinearLayout infoContainer;
    private LinearLayout iconsContainer;
    private RecyclerView documentContainer;
    Fragment fragment;

    public DefaultDialog() {
    }

    public DefaultDialog(@NonNull Context context, boolean smallDialog) {
        this.ctx = context;
        this.smallDialog = smallDialog;
        LayoutGenerator.init(ctx);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_MyTheme_BottomSheetDialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog = getDialog();
        if (dialog != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height;
            if (smallDialog)
                height = ViewGroup.LayoutParams.WRAP_CONTENT;
            else
                height = ViewGroup.LayoutParams.MATCH_PARENT;

            if (smallDialog) {

                Window window = dialog.getWindow();
                WindowManager.LayoutParams wlp = window.getAttributes();
                wlp.gravity = Gravity.BOTTOM;
                wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                window.setAttributes(wlp);
            }
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.custom_detail_info, null);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        root = (ConstraintLayout) view;
        name = view.findViewById(R.id.txt_info);
        photo = view.findViewById(R.id.img_general);
        infoContainer = view.findViewById(R.id.info_container);
        iconsContainer = view.findViewById(R.id.info_container);
        documentContainer = view.findViewById(R.id.document_container);

        firstIcon = view.findViewById(R.id.img_full_view);
        secondIcon = view.findViewById(R.id.img_navigate);
        thirdIcon = view.findViewById(R.id.img_close_info);
        thirdIcon.setOnClickListener(icon -> {
            dismiss();
        });
        firstIcon.setOnClickListener(view1 -> updateDataOnMap(null));

        if (smallDialog) {
            getName().setTextSize(25);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getName().setLineHeight(60);
            }
        } else {
            getName().setTextSize(30);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getName().setLineHeight(65);
            }
        }
        if (!smallDialog) {
            if (!fragment.getClass().getName().equals("cz.deepvision.iti.is.ui.home.HomeFragment")){
                getFirstIcon().setVisibility(View.VISIBLE);
                getFirstIcon().setImageDrawable(getCtx().getDrawable(R.drawable.ic_iti_show_on_map));
            }
            else getFirstIcon().setVisibility(View.GONE);
        }

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setAlignItems(AlignItems.FLEX_START);
        layoutManager.setJustifyContent(JustifyContent.CENTER);
        documentContainer.setVisibility(isSmallDialog() ? View.GONE : View.VISIBLE);
        documentContainer.setLayoutManager(layoutManager);

    }

    public boolean isSmallDialog() {
        return smallDialog;
    }

    protected interface Updater<T> {
        void updateData(T data);

    }

    protected void showDataOnMap(Location location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.requireContext());
        AlertDialog alertDialog = builder.create();
      /*  if (location == null) {
            builder.setTitle("Upozornění");
            builder.setMessage("U záznamů není uvedná poslední lokace, nelze přesměrovat na google mapy");
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                alertDialog.dismiss();
            });
        } else {*/
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra("location", new double[]{location.getLat(), location.getLng()});
    /*    }
        alertDialog.show();*/
    }

    protected void updateDataOnMap(Location location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.requireContext());
        AlertDialog alertDialog = builder.create();
        if (location == null) {
            builder.setTitle("Upozornění");
            builder.setMessage("U záznamů není uvedná poslední lokace, nelze přesměrovat na google mapy");
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                alertDialog.dismiss();
            });
        } else {
            builder.setTitle("Přesměrování");
            builder.setMessage("Chcete otevřít Google maps ?");
            builder.setPositiveButton("Ano", (dialogInterface, i) -> {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + location.getLat() + "," + location.getLng());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(ctx.getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            });
            builder.setNegativeButton("Ne", (dialogInterface, i) -> {
                alertDialog.dismiss();
            });
        }
        builder.setPositiveButton("Ano", (dialogInterface, i) -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + location.getLat() + "," + location.getLng());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(ctx.getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        });

        alertDialog.show();
    }

    @Override
    public void update(Bitmap bmp) {
        if (bmp != null) {
            getPhoto().setImageBitmap(bmp);
        }
    }

    public ConstraintLayout getRoot() {
        return root;
    }

    public ImageView getFirstIcon() {
        return firstIcon;
    }

    public ImageView getSecondIcon() {
        return secondIcon;
    }

    public ImageView getThirdIcon() {
        return thirdIcon;
    }

    public Context getCtx() {
        return ctx;
    }

    public TextView getName() {
        return name;
    }

    public void setName(TextView name) {
        this.name = name;
    }

    public ImageView getPhoto() {
        return photo;
    }

    public LinearLayout getInfoContainer() {
        return infoContainer;
    }

    public LinearLayout getIconsContainer() {
        return iconsContainer;
    }

    public RecyclerView getDocumentContainer() {
        return documentContainer;
    }

    public void setDocumentContainer(RecyclerView documentContainer) {
        this.documentContainer = documentContainer;
    }
}
