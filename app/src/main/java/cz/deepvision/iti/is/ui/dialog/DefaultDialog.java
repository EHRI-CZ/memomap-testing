package cz.deepvision.iti.is.ui.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import cz.deepvision.iti.is.OnShowAnotherElement;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.Location;
import cz.deepvision.iti.is.ui.home.HomeFragment;
import cz.deepvision.iti.is.util.LayoutGenerator;
import cz.deepvision.iti.is.util.Requester;

public abstract class DefaultDialog extends DialogFragment implements Requester.UpdatePhoto {
    protected Context ctx;
    protected Dialog dialog;
    protected boolean smallDialog;
    protected ConstraintLayout root;
    protected TextView name;
    protected ImageView photo;
    protected ImageView firstIcon;
    protected ImageView secondIcon;
    protected ImageView thirdIcon;
    protected LinearLayout infoContainer;
    protected LinearLayout iconsContainer;
    protected RecyclerView documentContainer;
    protected boolean isImageFitToScreen = false;
    protected float oldTouchValue, currentX;
    private OnShowAnotherElement onShowAnotherElement;
    private final int MIN_DISTANCE = 150;
    private int style = 0;
    Fragment fragment;


    /**
     * @param inputFragment Fragment for checking current position
     * @param smallDialog Fullscreen ?
     * @param style         0 - default, -1 left slide, 1 right slide, 2 slideUp
     */

    public DefaultDialog(@NonNull Fragment inputFragment, boolean smallDialog, int style) {
        this.ctx = inputFragment.requireContext();
        fragment = inputFragment;
        this.smallDialog = smallDialog;
        this.style = style;
        LayoutGenerator.init(ctx);
    }

    public DefaultDialog(@NonNull Fragment inputFragment, boolean smallDialog) {
        this.ctx = inputFragment.requireContext();
        fragment = inputFragment;
        this.smallDialog = smallDialog;
        style = 0;
        LayoutGenerator.init(ctx);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAllowEnterTransitionOverlap(true);
        setAllowReturnTransitionOverlap(true);
        if (style == 0)
            setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_MyTheme_BottomSheetDialog);
        else if (style == -1)
            setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_MyTheme_BottomSheetDialog_SlideLeft);
        else if (style == 1)
            setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_MyTheme_BottomSheetDialogSlideRight);
        else
            setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_MyTheme_BottomSheetDialogSlideUpDown);
    }

    public void setOnShowAnotherElement(OnShowAnotherElement onShowAnotherElement) {
        this.onShowAnotherElement = onShowAnotherElement;
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height;
            if (smallDialog) height = ViewGroup.LayoutParams.WRAP_CONTENT;
            else height = ViewGroup.LayoutParams.MATCH_PARENT;

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

    @SuppressLint("ClickableViewAccessibility")
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
            icon.animate().setDuration(200).rotationBy(360).alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dismiss();
                }
            });
        });

        setCancelable(false);
        firstIcon.setOnClickListener(view1 -> updateDataOnMap(null));

        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        oldTouchValue = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        currentX = event.getX();
                        if (oldTouchValue < currentX) {
                            if (currentX - oldTouchValue > MIN_DISTANCE)
                                if (onShowAnotherElement != null) {
                                    onShowAnotherElement.showPrevious();
                                }
                        } else {
                            if (oldTouchValue - currentX > MIN_DISTANCE)
                                if (onShowAnotherElement != null) {
                                    onShowAnotherElement.showNext();
                                }
                        }
                        break;
                }
                return false;
            }
        };
        if (getParentFragment() != null) {
            if (!smallDialog && !(getParentFragment() instanceof HomeFragment)) {
                root.setOnTouchListener(onTouchListener);
                infoContainer.setOnTouchListener(onTouchListener);
                documentContainer.setOnTouchListener(onTouchListener);
            }
        }

        if (smallDialog) {
            name.setTextSize(25);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                name.setLineHeight(60);
            }
        } else {
            name.setTextSize(30);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                name.setLineHeight(100);
            }
        }
        if (!smallDialog) {
            if (!fragment.getClass().getName().equals("cz.deepvision.iti.is.ui.home.HomeFragment")) {
                firstIcon.setVisibility(View.VISIBLE);
                firstIcon.setImageDrawable(ctx.getDrawable(R.drawable.ic_iti_menu_map));
            } else {
                firstIcon.setVisibility(View.GONE);
            }
            documentContainer.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        }
    }

    public boolean isSmallDialog() {
        return smallDialog;
    }

    protected interface Updater<T> {
        void updateData(T data);
    }


    protected void hideUI(int visibility) {
        name.setVisibility(visibility);
        infoContainer.setVisibility(visibility);
        documentContainer.setVisibility(visibility);

    }

    protected void showDataOnMap(Location location) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra("location", new double[]{location.getLat(), location.getLng()});
        dialog.dismiss();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ctx.sendBroadcast(intent);
    }

    protected void updateDataOnMap(Location location) {
        fragment.getActivity().runOnUiThread(() -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(fragment.requireContext());
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
                dialogInterface.dismiss();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    @Override
    public void update(Bitmap bmp) {
        if (bmp != null) {
            photo.setImageBitmap(bmp);
        }
    }
}
