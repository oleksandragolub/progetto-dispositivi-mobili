package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.RowPdfFavoriteBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.MyApplication;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.filters.FilterPdfComicsFavorite;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.ComicsPdfDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

public class AdapterPdfComicsFavorite extends RecyclerView.Adapter<AdapterPdfComicsFavorite.HolderPdfComicsFavorite> implements Filterable {

    private Context context;
    public ArrayList<ModelPdfComics> pdfArrayList, filterList;
    private FilterPdfComicsFavorite filter;
    private RowPdfFavoriteBinding binding;

    private OnItemClickListenerFavorite onItemClickListener;

    public interface OnItemClickListenerFavorite {
        void onItemClick(ModelPdfComics model);
    }

    public void setOnItemClickListener(AdapterPdfComicsFavorite.OnItemClickListenerFavorite listener) {
        this.onItemClickListener = listener;
    }

    public AdapterPdfComicsFavorite(Context context, ArrayList<ModelPdfComics> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfComicsFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfComicsFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfComicsFavorite holder, int position) {
        ModelPdfComics model = pdfArrayList.get(position);

        if (model.isFromApi()) {
            loadApiComicsDetails(model, holder);
        } else {
            loadComicsDetails(model, holder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(model);
            }
        });

        holder.removeFavBtn.setOnClickListener(v -> MyApplication.removeFromFavorite(context, model.getId()));
    }

    private void loadComicsDetails(ModelPdfComics model, HolderPdfComicsFavorite holder) {
        String comicsId = model.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.child(comicsId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comicsTitle = snapshot.child("titolo").getValue(String.class);
                String description = snapshot.child("descrizione").getValue(String.class);
                String comicsUrl = snapshot.child("url").getValue(String.class);

                model.setFavorite(true);
                model.setTitolo(comicsTitle);
                model.setDescrizione(description);
                model.setUrl(comicsUrl);

                holder.titleTv.setText(comicsTitle);
                holder.descriptionTv.setText(description);

                MyApplication.loadPdfFromUrlSinglePage(comicsUrl, comicsTitle, holder.pdfView, holder.progressBar);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadApiComicsDetails(ModelPdfComics model, HolderPdfComicsFavorite holder) {
        String comicsTitle = model.getTitolo();
        String description = model.getDescrizione();
        String comicsUrl = model.getUrl();

        holder.titleTv.setText(comicsTitle);
        holder.descriptionTv.setText(description);

        if (comicsUrl != null && !comicsUrl.isEmpty()) {
            MyApplication.loadPdfFromApi(comicsUrl, holder.pdfView, holder.progressBar, null);
        } else {
            holder.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterPdfComicsFavorite(filterList, this);
        }
        return filter;
    }

    class HolderPdfComicsFavorite extends RecyclerView.ViewHolder {
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton removeFavBtn;

        public HolderPdfComicsFavorite(@NonNull View itemView) {
            super(itemView);

            titleTv = binding.titleComics;
            descriptionTv = binding.descriptionComics;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            removeFavBtn = binding.removeFavBtn;
        }
    }
}