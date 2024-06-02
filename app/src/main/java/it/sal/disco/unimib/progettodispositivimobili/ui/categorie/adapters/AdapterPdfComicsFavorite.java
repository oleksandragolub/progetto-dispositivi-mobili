package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.databinding.RowPdfFavoriteBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

public class AdapterPdfComicsFavorite extends RecyclerView.Adapter<AdapterPdfComicsFavorite.HolderPdfComicsFavorite>{

    private Context context;
    private ArrayList<ModelPdfComics> pdfArrayList;

    private RowPdfFavoriteBinding binding;

    public AdapterPdfComicsFavorite(Context context, ArrayList<ModelPdfComics> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfComicsFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfComicsFavorite holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


    class HolderPdfComicsFavorite extends RecyclerView.ViewHolder {
        public HolderPdfComicsFavorite (@NonNull View itemView) {
            super(itemView);
        }
    }

}
