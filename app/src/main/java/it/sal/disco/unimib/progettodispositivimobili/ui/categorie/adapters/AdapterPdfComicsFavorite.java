package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.RowPdfFavoriteBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.MyApplication;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.ComicsPdfDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.ComicsPdfListAdminFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.preferiti.PreferitiFragment;

public class AdapterPdfComicsFavorite extends RecyclerView.Adapter<AdapterPdfComicsFavorite.HolderPdfComicsFavorite>{

    private Context context;
    private ArrayList<ModelPdfComics> pdfArrayList;

    private RowPdfFavoriteBinding binding;

    private static final String TAG = "FAV_COMICS_TAG";

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
        ModelPdfComics model = pdfArrayList.get(position);

        loadComicsDetails(model, holder);

       /* holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("comicsId", model.getId());

                ComicsPdfDetailFragment comicsPdfDetailFragment = new ComicsPdfDetailFragment();
                comicsPdfDetailFragment.setArguments(bundle);

                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.nav_host_fragment, comicsPdfDetailFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });*/

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("comicsId", model.getId());

                ComicsPdfDetailFragment comicsPdfDetailFragment = new ComicsPdfDetailFragment();
                comicsPdfDetailFragment.setArguments(bundle);

                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.nav_host_fragment, comicsPdfDetailFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        holder.removeFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.removeFromFavorite(context, model.getId());
            }
        });

    }

    private void loadComicsDetails(ModelPdfComics model, HolderPdfComicsFavorite holder) {
        String comicsId = model.getId();
        Log.d(TAG, "loadComicsDetails: Comics Details of Comics ID: " + comicsId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.child(comicsId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comicsTitle = ""+snapshot.child("titolo").getValue();
                String description = ""+snapshot.child("descrizione").getValue();
                String categoryId = ""+snapshot.child("categoryId").getValue();
                String comicsUrl = ""+snapshot.child("url").getValue();
                String timestamp = ""+snapshot.child("timestamp").getValue();
                String uid = ""+snapshot.child("uid").getValue();
                String viewsCount = ""+snapshot.child("viewsCount").getValue();
                String downloadsCount = ""+snapshot.child("downloadsCount").getValue();

                model.setFavorite(true);
                model.setTitolo(comicsTitle);
                model.setDescrizione(description);
                model.setTimestamp(Long.parseLong(timestamp));
                model.setCategoryId(categoryId);
                model.setUid(uid);
                model.setUrl(comicsUrl);

                String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                MyApplication.loadCategory(categoryId, holder.categoryTv);
                MyApplication.loadPdfFromUrlSinglePage(""+comicsUrl, ""+comicsTitle, holder.pdfView, holder.progressBar, null);
                MyApplication.loadPdfSize(""+comicsUrl, ""+comicsTitle, holder.sizeTv);

                holder.titleTv.setText(comicsTitle);
                holder.descriptionTv.setText(description);
                holder.dateTv.setText(date);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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