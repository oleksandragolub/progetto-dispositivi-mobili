package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.filters;

import android.widget.Filter;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterCategory;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterPdfComicsAdmin;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelCategory;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

public class FilterPdfComicsAdmin extends Filter {

    ArrayList<ModelPdfComics> filterList;
    AdapterPdfComicsAdmin adapterPdfComicsAdmin;

    public FilterPdfComicsAdmin(ArrayList<ModelPdfComics> filterList, AdapterPdfComicsAdmin adapterPdfComicsAdmin) {
        this.filterList = filterList;
        this.adapterPdfComicsAdmin = adapterPdfComicsAdmin;
    }


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if(constraint!=null && constraint.length()>0){

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
        adapterPdfComicsAdmin.pdfArrayList = (ArrayList<ModelPdfComics>)results.values;

        adapterPdfComicsAdmin.notifyDataSetChanged();
    }
}
