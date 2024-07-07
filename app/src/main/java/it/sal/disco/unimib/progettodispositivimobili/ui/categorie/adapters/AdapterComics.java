package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;
import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.MyApplication;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsMarvelDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsPdfDetailUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

public class AdapterComics extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private static final int VIEW_TYPE_API = 0;
    private static final int VIEW_TYPE_MANUAL = 1;
    private List<ModelPdfComics> comicsList;
    private List<ModelPdfComics> comicsListFiltered;
    private FragmentActivity activity;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(ModelPdfComics comic);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public AdapterComics(List<ModelPdfComics> comicsList, FragmentActivity activity) {
        this.comicsList = comicsList;
        this.comicsListFiltered = comicsList;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        return comicsListFiltered.get(position).isFromApi() ? VIEW_TYPE_API : VIEW_TYPE_MANUAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_API) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comics, parent, false);
            return new ApiComicViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_pdf_user, parent, false);
            return new ManualComicViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ModelPdfComics comic = comicsListFiltered.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_API) {
            ApiComicViewHolder apiHolder = (ApiComicViewHolder) holder;
            apiHolder.title.setText(comic.getTitolo());
            apiHolder.description.setText(comic.getDescrizione());
            Glide.with(apiHolder.itemView.getContext()).load(comic.getUrl()).into(apiHolder.thumbnail);
            apiHolder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(comic);
                }
            });
        } else {
            ManualComicViewHolder manualHolder = (ManualComicViewHolder) holder;
            manualHolder.titleTv.setText(comic.getTitolo());
            manualHolder.descriptionTv.setText(comic.getDescrizione());
            MyApplication.loadPdfFromUrlSinglePage(comic.getUrl(), comic.getTitolo(), manualHolder.pdfView, manualHolder.progressBar, null);
            manualHolder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(comic);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return comicsListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.count = comicsList.size();
                    results.values = comicsList;
                } else {
                    String searchStr = constraint.toString().toUpperCase();
                    List<ModelPdfComics> resultData = new ArrayList<>();
                    for (ModelPdfComics comic : comicsList) {
                        if (comic.getTitolo().toUpperCase().contains(searchStr)) {
                            resultData.add(comic);
                        }
                    }
                    results.count = resultData.size();
                    results.values = resultData;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                comicsListFiltered = (List<ModelPdfComics>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    static class ApiComicViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageView thumbnail;

        public ApiComicViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.comicTitle);
            description = itemView.findViewById(R.id.comicDescription);
            thumbnail = itemView.findViewById(R.id.comicThumbnail);
        }
    }

    static class ManualComicViewHolder extends RecyclerView.ViewHolder {
        TextView titleTv, descriptionTv;
        PDFView pdfView;
        ProgressBar progressBar;

        public ManualComicViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.title_comics);
            descriptionTv = itemView.findViewById(R.id.description_comics);
            pdfView = itemView.findViewById(R.id.pdfView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}

