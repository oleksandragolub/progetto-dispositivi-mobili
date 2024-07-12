package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;
import java.util.List;


import it.sal.disco.unimib.progettodispositivimobili.databinding.RowPdfUserBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.MyApplication;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.filters.FilterPdfComicsUser;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

public class AdapterPdfComicsUser extends RecyclerView.Adapter<AdapterPdfComicsUser.HolderPdfUser> implements Filterable {

    private Context context;
    public ArrayList<ModelPdfComics> pdfArrayList, filterList;
    private FilterPdfComicsUser filter;
    private RowPdfUserBinding binding;
    private OnItemClickListenerUser onItemClickListener;

    public interface OnItemClickListenerUser {
        void onItemClick(ModelPdfComics model);
    }

    public void setOnItemClickListener(OnItemClickListenerUser listener) {
        this.onItemClickListener = listener;
    }

    public AdapterPdfComicsUser(Context context, ArrayList<ModelPdfComics> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {
        ModelPdfComics model = pdfArrayList.get(position);
        String title = model.getTitolo();
        String description = model.getDescrizione();
        String pdfUrl = model.getUrl();

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);

        MyApplication.loadPdfFromUrlSinglePage(pdfUrl, title, holder.pdfView, holder.progressBar, null);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterPdfComicsUser(filterList, this);
        }
        return filter;
    }

    class HolderPdfUser extends RecyclerView.ViewHolder {

        TextView titleTv, descriptionTv;
        PDFView pdfView;
        ProgressBar progressBar;

        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);
            titleTv = binding.titleComics;
            descriptionTv = binding.descriptionComics;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
        }
    }
}
