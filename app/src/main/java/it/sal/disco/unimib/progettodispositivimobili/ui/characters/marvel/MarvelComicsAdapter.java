package it.sal.disco.unimib.progettodispositivimobili.ui.characters.marvel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.R;

public class MarvelComicsAdapter extends RecyclerView.Adapter<MarvelComicsAdapter.ComicViewHolder> {
    private List<Comix> comixes = new ArrayList<>();

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comic, parent, false);
        return new ComicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComicViewHolder holder, int position) {
        Comix comix = comixes.get(position);
        holder.titleTextView.setText(comix.getTitle());
        Glide.with(holder.thumbnailImageView.getContext())
                .load(comix.getThumbnail().getPath() + "." + comix.getThumbnail().getExtension())
                .into(holder.thumbnailImageView);
    }

    @Override
    public int getItemCount() {
        return comixes.size();
    }

    public void setComics(List<Comix> comixes) {
        this.comixes = comixes;
        notifyDataSetChanged();
    }

    static class ComicViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView thumbnailImageView;

        ComicViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnailImageView);
        }
    }
}

