package it.sal.disco.unimib.progettodispositivimobili.ui.characters.resto;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterCharacter extends Filter {

    ArrayList<ModelCharacter> filterList;
    AdapterCharacter adapterCharacter;

    public FilterCharacter(ArrayList<ModelCharacter> filterList, AdapterCharacter adapterCharacter) {
        this.filterList = filterList;
        this.adapterCharacter = adapterCharacter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if(constraint!=null && constraint.length()>0){

            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCharacter> filteredModels = new ArrayList<>();

            for(int i=0; i<filterList.size(); i++){
                if(filterList.get(i).getNome().toUpperCase().contains(constraint)){
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
        adapterCharacter.characterArrayList = (ArrayList<ModelCharacter>)results.values;

        adapterCharacter.notifyDataSetChanged();
    }
}
