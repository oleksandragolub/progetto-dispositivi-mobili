package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.filters.FilterApiComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

public class AdapterApiComics extends RecyclerView.Adapter<AdapterApiComics.ComicViewHolder> implements Filterable {
    private List<Comic> comics;
    private List<Comic> comicsFiltered;
    private FragmentActivity activity;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Comic comic);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public AdapterApiComics(List<Comic> comics, FragmentActivity activity) {
        this.comics = comics;
        this.comicsFiltered = comics;
        this.activity = activity;
    }

    public void addComics(List<Comic> newComics) {
        comics.addAll(newComics);
        notifyDataSetChanged(); // Notify the adapter of data changes
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comics, parent, false);
        return new ComicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComicViewHolder holder, int position) {
        Comic comic = comicsFiltered.get(position);
        holder.title.setText(comic.getTitle());
        holder.description.setText(comic.getDescription());
        Glide.with(holder.itemView.getContext())
                .load(comic.getThumbnail())
                .apply(new RequestOptions().override(100, 150)) // Apply RequestOptions here
                .thumbnail(0.1f) // Use thumbnail directly on RequestBuilder
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.thumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(comic);
            }
        });
    }

    @Override
    public int getItemCount() {
        return comicsFiltered.size(); // Use comicsFiltered instead of comics
    }

    public void updateComics(List<Comic> newComics) {
        comics.clear();
        comics.addAll(newComics);
        comicsFiltered = comics;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new FilterApiComics(comics, this);
    }

    static class ComicViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageView thumbnail;

        public ComicViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.comicTitle);
            description = itemView.findViewById(R.id.comicDescription);
            thumbnail = itemView.findViewById(R.id.comicThumbnail);
        }
    }
}
