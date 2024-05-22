package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import static it.sal.disco.unimib.progettodispositivimobili.ui.categorie.Constants.MAX_BYTES_PDF;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.MyApplication;
import it.sal.disco.unimib.progettodispositivimobili.databinding.RowPdfAdminBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.filters.FilterPdfComicsAdmin;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelCategory;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.ProfileFragment;

public class AdapterPdfComicsAdmin extends RecyclerView.Adapter<AdapterPdfComicsAdmin.HolderPdfAdmin> implements Filterable {

    private Context context;
    public ArrayList<ModelPdfComics> pdfArrayList, filterList;
    private FilterPdfComicsAdmin filter;
    private RowPdfAdminBinding binding;

    private static final String TAG = "PDF_ADAPTER_TAG";
    public AdapterPdfComicsAdmin(Context context, ArrayList<ModelPdfComics> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
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
        long timestamp = model.getTimestamp();

        String formattedDate = MyApplication.formatTimestamp(timestamp);

        holder.titleTV.setText(title);
        holder.descriptionTV.setText(description);
        holder.dateTV.setText(formattedDate);

        loadCategory(model, holder);
        loadPdfFromUrl(model, holder);
        loadPdfSize(model, holder);
    }

    private void loadPdfSize(ModelPdfComics model, HolderPdfAdmin holder) {
        String pdfUrl = model.getUrl();

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                double bytes = storageMetadata.getSizeBytes();
                Log.d(TAG, "onSuccess: "+ model.getTitolo() + " " + bytes);

                double kb = bytes/1024;
                double mb = kb/1024;

                if (mb >= 1){
                    holder.sizeTV.setText(String.format("%.2f", mb)+" MB");
                } else if (kb >= 1){
                    holder.sizeTV.setText(String.format("%.2f", kb)+" KB");
                } else {
                    holder.sizeTV.setText(String.format("%.2f", bytes)+" bytes");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: "+ e.getMessage());
            }
        });
    }

    private void loadPdfFromUrl(ModelPdfComics model, HolderPdfAdmin holder) {

        String pdfUrl = model.getUrl();
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG, "onSuccess: "+ model.getTitolo() + " succesfylly got the file");

                holder.pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError(new OnErrorListener() {
                            @Override
                            public void onError(Throwable t) {
                                holder.progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "onError: "+ t.getMessage());
                            }
                        }).onPageError(new OnPageErrorListener(){
                            @Override
                            public void onPageError(int page, Throwable t) {
                                holder.progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "onPageError: "+ t.getMessage());
                            }
                        }).onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {
                                holder.progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "loadComplete: pdf loaded");
                            }
                        })
                        .load();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                holder.progressBar.setVisibility(View.INVISIBLE);
                //Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: failed getting file from url due to "+ e.getMessage());
            }
        });
    }


   private void loadCategory(ModelPdfComics model, HolderPdfAdmin holder) {
       String categoryId = model.getCategoryId();

       DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
       ref.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               String category = "" + snapshot.child("category").getValue();
               holder.categoryTV.setText(category);
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {
               Log.e(TAG, "onCancelled: " + error.getMessage());
           }
       });
   }
    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null ){
            filter = new FilterPdfComicsAdmin(filterList, this);
        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTV, descriptionTV, categoryTV, sizeTV, dateTV;
        ImageButton moreBTN;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTV = binding.titleComics;
            descriptionTV = binding.descriptionComics;
            categoryTV = binding.categoryComics;
            sizeTV = binding.sizeComics;
            dateTV = binding.dateComics;
            moreBTN = binding.moreBtn;
        }
    }
}
