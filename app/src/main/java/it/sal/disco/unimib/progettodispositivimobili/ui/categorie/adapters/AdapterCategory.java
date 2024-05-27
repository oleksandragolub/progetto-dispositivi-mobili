package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.RowCategoryBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments.ComicsPdfListAdminFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.filters.FilterCategory;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelCategory;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> implements Filterable {

    private Context context;
    public ArrayList<ModelCategory> categoryArrayList, filterList;

    private RowCategoryBinding binding;

    private FilterCategory filter;

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList){
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {

        ModelCategory model = categoryArrayList.get(position);
        String id = model.getId();
        String category = model.getCategory();
        String uid = model.getUid();
        long timestamp = model.getTimestamp();

        holder.categoryTv.setText(category);

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Elimina").setMessage("Sei sicuro di voler eliminare questo caralogo?")
                        .setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(context.getApplicationContext(), "Eliminazione...", Toast.LENGTH_SHORT).show();
                                deleteCategory(model, holder);
                            }
                        }).setNegativeButton("Cancella", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

    /*    holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ComicsPdfListAdminFragment.class);
                intent.putExtra("categoryId", id);
                intent.putExtra("categoryTitle", category);
                context.startActivity(intent);
            }
        });*/

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("categoryId", id);
                bundle.putString("categoryTitle", category);

                ComicsPdfListAdminFragment comicsPdfListAdminFragment = new ComicsPdfListAdminFragment();
                comicsPdfListAdminFragment.setArguments(bundle);

                FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.nav_host_fragment, comicsPdfListAdminFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });


    }

    private void deleteCategory(ModelCategory model, HolderCategory holder) {
        String id = model.getId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context.getApplicationContext(), "Eliminazione effettuata con successo...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context.getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterCategory(filterList, this);
        }
        return filter;
    }

    class HolderCategory extends RecyclerView.ViewHolder{

        TextView categoryTv;

        ImageButton deleteBtn;


        public HolderCategory(@NonNull View itemView){
            super(itemView);

            categoryTv = binding.categoryTitle;
            deleteBtn = binding.deleteBtn;

        }
    }
}
