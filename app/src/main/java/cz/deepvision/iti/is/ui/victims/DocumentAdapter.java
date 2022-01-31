package cz.deepvision.iti.is.ui.victims;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cz.deepvision.iti.is.OnShowAnotherElement;
import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.victims.Document;
import cz.deepvision.iti.is.ui.dialog.DocumentDialog;
import cz.deepvision.iti.is.ui.dialog.VictimDialog;
import cz.deepvision.iti.is.util.Requester;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {
    private List<Document> items;
    private Context ctx;
    private Fragment fragment;

    public DocumentAdapter(List<Document> items, Fragment fragment) {
        setHasStableIds(true);
        this.items = items;
        this.fragment = fragment;
        this.ctx = fragment.requireContext();
    }

    @NonNull
    @Override
    public DocumentAdapter.DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DocumentAdapter.DocumentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.document_info, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }


    public class DocumentViewHolder extends RecyclerView.ViewHolder {
        private Document document;
        private TextView documentName;
        private ImageView documentImage;
        private DocumentDialog documentDialog;
        private int position;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            documentName = itemView.findViewById(R.id.document_name);
            documentImage = itemView.findViewById(R.id.document_image);
        }

        public void bind(Document doc) {
            this.document = doc;
            documentName.setText(doc.getName());
            if (doc.getPreviewImage() != null) {
                Requester requester = new Requester(fragment.getActivity());
                requester.makeRequestForAdapter(doc.getPreviewImage(), documentImage);
            }

            documentDialog = new DocumentDialog(fragment, document);
            documentDialog.setOnShowAnotherElement(onShowAnotherElement);

            documentImage.setOnClickListener(view -> {
                documentDialog.show(fragment.getActivity().getSupportFragmentManager(), "dialog_document_fullscreen");
            });



        }

        private OnShowAnotherElement onShowAnotherElement = new OnShowAnotherElement() {
            @Override
            public void showNext() {
                if (position + 1 < items.size())
                    showAnotherElement(items.get(++position), -1);
            }

            @Override
            public void showPrevious() {
                if (position - 1 >= 0)
                    showAnotherElement(items.get(--position), 1);
            }
        };

        public void showAnotherElement(Document document, int style) {

            DocumentDialog dialog = new DocumentDialog(fragment, document, style);

            dialog.show(fragment.getChildFragmentManager(), VictimDialog.class.getName());
            dialog.setOnShowAnotherElement(onShowAnotherElement);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                documentDialog.dismiss();
                documentDialog = dialog;
            }, 500);
        }
    }

}
