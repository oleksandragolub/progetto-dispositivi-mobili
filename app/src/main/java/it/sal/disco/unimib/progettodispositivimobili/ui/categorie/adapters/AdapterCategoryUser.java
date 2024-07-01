package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.RowCategoryUserBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsPdfListUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.filters.FilterCategoryUser;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelCategory;

public class AdapterCategoryUser extends RecyclerView.Adapter<AdapterCategoryUser.HolderCategoryUser> implements Filterable {
    private Context context;
    public ArrayList<ModelCategory> categoryArrayList, filterList;
    private RowCategoryUserBinding binding;
    private FilterCategoryUser filter;

    public AdapterCategoryUser(Context context, ArrayList<ModelCategory> categoryArrayList){
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
    }

    @NonNull
    @Override
    public HolderCategoryUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCategoryUserBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderCategoryUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategoryUser holder, int position) {
        ModelCategory model = categoryArrayList.get(position);
        String id = model.getId();
        String category = model.getCategory();
        holder.categoryTv.setText(category);

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("categoryId", id);
            bundle.putString("categoryTitle", category);
            ComicsPdfListUserFragment comicsPdfListUserFragment = new ComicsPdfListUserFragment();
            comicsPdfListUserFragment.setArguments(bundle);
            FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.nav_host_fragment, comicsPdfListUserFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterCategoryUser(filterList, this);
        }
        return filter;
    }

    class HolderCategoryUser extends RecyclerView.ViewHolder {
        TextView categoryTv;

        public HolderCategoryUser(@NonNull View itemView) {
            super(itemView);
            categoryTv = binding.categoryTitle;
        }
    }
}
