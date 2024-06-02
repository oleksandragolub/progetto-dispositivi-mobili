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

import it.sal.disco.unimib.progettodispositivimobili.databinding.RowPdfAdminBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.MyApplication;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.ComicsPdfDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.ComicsPdfEditFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.filters.FilterPdfComicsAdmin;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

public class AdapterPdfComicsAdmin extends RecyclerView.Adapter<AdapterPdfComicsAdmin.HolderPdfAdmin> implements Filterable {

    private Context context;
    public ArrayList<ModelPdfComics> pdfArrayList, filterList;
    private FilterPdfComicsAdmin filter;
    private RowPdfAdminBinding binding;
    private ProgressDialog progressDialog;

    private static final String TAG = "PDF_ADAPTER_TAG";
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
        long timestamp = model.getTimestamp();
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        String pdfUrl = model.getUrl();

        String formattedDate = MyApplication.formatTimestamp(timestamp);

        holder.titleTV.setText(title);
        holder.descriptionTV.setText(description);
        holder.dateTV.setText(formattedDate);

        //loadCategory(model, holder);
        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTV);

        //loadPdfFromUrl(model, holder);
        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar,
                null);

        //loadPdfSize(model, holder);
        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTV);

        holder.moreBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(model, holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("comicsId", pdfId);

                ComicsPdfDetailFragment comicsPdfDetailFragment = new ComicsPdfDetailFragment();
                comicsPdfDetailFragment.setArguments(bundle);

                FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.nav_host_fragment, comicsPdfDetailFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    private void moreOptionsDialog(ModelPdfComics model, HolderPdfAdmin holder) {
        String comicsId = model.getId();
        String comicsUrl = model.getUrl();
        String comicsTitolo = model.getTitolo();

        String[] options = {"Modifica", "Elimina"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Scegli l'opzione").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    openComicsPdfEditFragment(model.getId());
                } else if (which==1) {
                    MyApplication.deleteComics(context,
                            "" + comicsId,
                            "" + comicsUrl,
                            "" + comicsTitolo);
                    //deleteComics(model, holder);
                }
            }
        }).show();
    }

    private void openComicsPdfEditFragment(String comicsId) {
        // Crea una nuova istanza del fragment di modifica
        ComicsPdfEditFragment fragment = new ComicsPdfEditFragment();

        // Passa l'ID del fumetto come argomento
        Bundle args = new Bundle();
        args.putString("comicsId", comicsId);
        fragment.setArguments(args);

        FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

  /*  private void deleteComics(ModelPdfComics model, HolderPdfAdmin holder) {
        String comicsId = model.getId();
        String comicsUrl = model.getUrl();
        String comicsTitolo = model.getTitolo();

        Log.d(TAG, "deleteComics: Deleting...");
        progressDialog.setMessage("Deleting " + comicsTitolo + "...");
        progressDialog.show();

        Log.d(TAG, "deleteComics: Deleting from storage...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(comicsUrl);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: Deleting from storage");
                Log.d(TAG, "onSuccess: Now deleting info from db");
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comics");
                reference.child(comicsId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted from db too");
                        progressDialog.dismiss();
                        Toast.makeText(context, "Comics Deleted Successfully...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from db due to "+ e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Failed to delete from storage due to " + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }*/

   /* private void loadPdfSize(ModelPdfComics model, HolderPdfAdmin holder) {
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
    }*/

   /* private void loadPdfFromUrl(ModelPdfComics model, HolderPdfAdmin holder) {

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
    }*/


   /*private void loadCategory(ModelPdfComics model, HolderPdfAdmin holder) {
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
   }*/
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
