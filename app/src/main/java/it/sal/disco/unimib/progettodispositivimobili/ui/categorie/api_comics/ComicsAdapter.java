package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics;

import android.os.Bundle;
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
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.Comic;

public class ComicsAdapter extends RecyclerView.Adapter<ComicsAdapter.ComicViewHolder> {
    private List<Comic> comics;
    private FragmentActivity activity;

    public ComicsAdapter(List<Comic> comics, FragmentActivity activity) {
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
            ModelPdfComics modelPdfComics = new ModelPdfComics();
            modelPdfComics.setId(comic.getId());
            modelPdfComics.setTitolo(comic.getTitle());
            modelPdfComics.setDescrizione(comic.getDescription());
            modelPdfComics.setUrl(comic.getThumbnail());

            Bundle bundle = new Bundle();
            bundle.putSerializable("modelPdfComics", modelPdfComics);

            ComicsMarvelDetailFragment detailFragment = new ComicsMarvelDetailFragment();
            detailFragment.setArguments(bundle);

            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.nav_host_fragment, detailFragment);
            transaction.addToBackStack(null);
            transaction.commit();
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