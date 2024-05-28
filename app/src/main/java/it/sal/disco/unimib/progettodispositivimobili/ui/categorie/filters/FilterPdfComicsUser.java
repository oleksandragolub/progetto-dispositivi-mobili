package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.filters;

import android.widget.Filter;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterPdfComicsAdmin;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterPdfComicsUser;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

public class FilterPdfComicsUser extends Filter {

    ArrayList<ModelPdfComics> filterList;
    AdapterPdfComicsUser adapterPdfComicsUser;

    public FilterPdfComicsUser(ArrayList<ModelPdfComics> filterList, AdapterPdfComicsUser adapterPdfComicsUser) {
        this.filterList = filterList;
        this.adapterPdfComicsUser = adapterPdfComicsUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        //if(constraint!=null && constraint.length()>0){
        if(constraint!=null || constraint.length()>0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdfComics> filteredModels = new ArrayList<>();

            for(int i=0; i<filterList.size(); i++){
                if(filterList.get(i).getTitolo().toUpperCase().contains(constraint)){
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
        adapterPdfComicsUser.pdfArrayList = (ArrayList<ModelPdfComics>)results.values;

        adapterPdfComicsUser.notifyDataSetChanged();
    }
}
