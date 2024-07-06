package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.github.barteksc.pdfviewer.PDFView;
import java.util.ArrayList;
import java.util.List;
import it.sal.disco.unimib.progettodispositivimobili.databinding.RowPdfAdminBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.MyApplication;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.filters.FilterPdfComicsAdmin;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.ComicsPdfEditFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

public class AdapterPdfComicsAdmin extends RecyclerView.Adapter<AdapterPdfComicsAdmin.HolderPdfAdmin> implements Filterable {

    private Context context;
    public ArrayList<ModelPdfComics> pdfArrayList, filterList;
    private FilterPdfComicsAdmin filter;
    private RowPdfAdminBinding binding;
    private ProgressDialog progressDialog;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(ModelPdfComics model);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public AdapterPdfComicsAdmin(Context context, ArrayList<ModelPdfComics> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Per favore, aspetta");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        ModelPdfComics model = pdfArrayList.get(position);
        String title = model.getTitolo();
        String description = model.getDescrizione();
        String pdfUrl = model.getUrl();

        holder.titleTV.setText(title);
        holder.descriptionTV.setText(description);

        MyApplication.loadPdfFromUrlSinglePage(pdfUrl, title, holder.pdfView, holder.progressBar, null);

        holder.moreBTN.setOnClickListener(v -> moreOptionsDialog(model, holder));

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(model);
            }
        });
    }

    private void moreOptionsDialog(ModelPdfComics model, HolderPdfAdmin holder) {
        String comicsId = model.getId();
        String comicsUrl = model.getUrl();
        String comicsTitolo = model.getTitolo();

        String[] options = {"Modifica", "Elimina"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Scegli l'opzione").setItems(options, (dialog, which) -> {
            if (which == 0) {
                openComicsPdfEditFragment(model.getId());
            } else if (which == 1) {
                MyApplication.deleteComics(context,
                        "" + comicsId,
                        "" + comicsUrl,
                        "" + comicsTitolo);
            }
        }).show();
    }

    private void openComicsPdfEditFragment(String comicsId) {
        ComicsPdfEditFragment fragment = new ComicsPdfEditFragment();
        Bundle args = new Bundle();
        args.putString("comicsId", comicsId);
        fragment.setArguments(args);

        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void addMoreComics(List<ModelPdfComics> newComics) {
        int startPosition = pdfArrayList.size();
        pdfArrayList.addAll(newComics);
        notifyItemRangeInserted(startPosition, newComics.size());
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterPdfComicsAdmin(filterList, this);
        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder {
        TextView titleTV, descriptionTV;
        PDFView pdfView;
        ProgressBar progressBar;
        ImageButton moreBTN;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);
            titleTV = binding.titleComics;
            descriptionTV = binding.descriptionComics;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            moreBTN = binding.moreBtn;
        }
    }
}