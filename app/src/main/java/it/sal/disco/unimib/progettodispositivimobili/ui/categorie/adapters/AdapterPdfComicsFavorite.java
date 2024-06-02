package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;

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
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfComicsFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfComicsFavorite holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }


    class HolderPdfComicsFavorite extends RecyclerView.ViewHolder {
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton removeFavBtn;

        public HolderPdfComicsFavorite (@NonNull View itemView) {
            super(itemView);

            titleTv = binding.titleComics;
            descriptionTv = binding.descriptionComics;
            categoryTv = binding.categoryComics;
            sizeTv = binding.sizeComics;
            dateTv = binding.dateComics;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            removeFavBtn = binding.removeFavBtn;
        }
    }

}
