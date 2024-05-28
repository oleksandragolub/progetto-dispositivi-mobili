package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.filters;

import android.widget.Filter;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterCategoryUser;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelCategory;

public class FilterCategoryUser extends Filter {

    ArrayList<ModelCategory> filterList;
    AdapterCategoryUser adapterCategory;

    public FilterCategoryUser(ArrayList<ModelCategory> filterList, AdapterCategoryUser adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if(constraint!=null && constraint.length()>0){

            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModels = new ArrayList<>();

            for(int i=0; i<filterList.size(); i++){
                if(filterList.get(i).getCategory().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }else{
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>)results.values;

        adapterCategory.notifyDataSetChanged();
    }
}
