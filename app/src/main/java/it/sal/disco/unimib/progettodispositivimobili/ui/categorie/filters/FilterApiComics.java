package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.filters;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterApiComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;

public class FilterApiComics extends Filter {

    private List<Comic> filterList;
    private AdapterApiComics adapter;

    public FilterApiComics(List<Comic> filterList, AdapterApiComics adapter) {
        this.filterList = filterList;
        this.adapter = adapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();
            List<Comic> filteredComics = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint) ||
                        filterList.get(i).getDescription().toUpperCase().contains(constraint)) {
                    filteredComics.add(filterList.get(i));
                }
            }

            results.count = filteredComics.size();
            results.values = filteredComics;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.updateComics((List<Comic>) results.values);
    }
}
