package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsMarvelDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;

public class AdapterApiComics extends RecyclerView.Adapter<AdapterApiComics.ComicViewHolder> {
    private List<Comic> comics;
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
        this.activity = activity;
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comics, parent, false);
        return new ComicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComicViewHolder holder, int position) {
        Comic comic = comics.get(position);
        String title = comic.getTitle();
        String description = comic.getDescription();

        holder.title.setText(title != null ? title : "No Title Available");
        holder.description.setText(description != null ? description : "No Description Available");

        String imageUrl = comic.getThumbnail();
        Glide.with(holder.itemView.getContext()).load(imageUrl).into(holder.thumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(comic);
            }
        });
    }

    @Override
    public int getItemCount() {
        return comics.size();
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