package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.MyApplication;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsMarvelDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsPdfDetailUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

public class AdapterComics extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_API = 0;
    private static final int VIEW_TYPE_MANUAL = 1;
    private List<ModelPdfComics> comicsList;
    private FragmentActivity activity;

    public AdapterComics(List<ModelPdfComics> comicsList, FragmentActivity activity) {
        this.comicsList = comicsList;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        return comicsList.get(position).isFromApi() ? VIEW_TYPE_API : VIEW_TYPE_MANUAL;
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
        ModelPdfComics comic = comicsList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_API) {
            ApiComicViewHolder apiHolder = (ApiComicViewHolder) holder;
            apiHolder.title.setText(comic.getTitolo());
            apiHolder.description.setText(comic.getDescrizione());
            Glide.with(apiHolder.itemView.getContext()).load(comic.getUrl()).into(apiHolder.thumbnail);
            apiHolder.itemView.setOnClickListener(v -> openComicDetailFragment(comic));
        } else {
            ManualComicViewHolder manualHolder = (ManualComicViewHolder) holder;
            manualHolder.titleTv.setText(comic.getTitolo());
            manualHolder.descriptionTv.setText(comic.getDescrizione());
            MyApplication.loadPdfFromUrlSinglePage(comic.getUrl(), comic.getTitolo(), manualHolder.pdfView, manualHolder.progressBar, null);
            // MyApplication.loadCategory(comic.getCategoryId(), manualHolder.categoryTv);
            manualHolder.itemView.setOnClickListener(v -> openComicDetailFragment(comic));
        }
    }

    @Override
    public int getItemCount() {
        return comicsList.size();
    }

    private void openComicDetailFragment(ModelPdfComics comic) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("modelPdfComics", comic);
        Fragment detailFragment;
        if (comic.isFromApi()) {
            detailFragment = new ComicsMarvelDetailFragment();
        } else {
            detailFragment = new ComicsPdfDetailUserFragment();
        }
        detailFragment.setArguments(bundle);
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
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
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
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
