package it.sal.disco.unimib.progettodispositivimobili.ui.characters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.Comic;

/*public class ComicsAdapter extends RecyclerView.Adapter<ComicsAdapter.ComicViewHolder> {
    private List<Comic> comics;

    public ComicsAdapter(List<Comic> comics) {
        this.comics = comics;
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
        holder.title.setText(comic.getTitle());
        holder.description.setText(comic.getDescription());

        String imageUrl = comic.getThumbnail().getPath() + "." + comic.getThumbnail().getExtension();
        Glide.with(holder.itemView.getContext()).load(imageUrl).into(holder.thumbnail);
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
}*/

public class ComicsAdapter extends RecyclerView.Adapter<ComicsAdapter.ComicViewHolder> {
    private List<Comic> comics;

    public ComicsAdapter(List<Comic> comics) {
        this.comics = comics;
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
        holder.title.setText(comic.getTitle());
        holder.description.setText(comic.getDescription());

        String imageUrl = comic.getThumbnail().getPath() + "." + comic.getThumbnail().getExtension();
        Glide.with(holder.itemView.getContext()).load(imageUrl).into(holder.thumbnail);

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("thumbnailUrl", imageUrl);
            bundle.putString("title", comic.getTitle());
            bundle.putString("description", comic.getDescription());

            ComicsMarvelDetailFragment fragment = new ComicsMarvelDetailFragment();
            fragment.setArguments(bundle);

            FragmentActivity activity = (FragmentActivity) v.getContext();
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
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

