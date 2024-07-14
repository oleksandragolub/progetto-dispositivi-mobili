package it.sal.disco.unimib.progettodispositivimobili.ui.characters_api_marvel_prova.resto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.databinding.RowDetailCharacterBinding;

public class AdapterCharacter extends RecyclerView.Adapter<AdapterCharacter.HolderCharacter> implements Filterable {

    private Context context;
    public ArrayList<ModelCharacter> characterArrayList, filterList;
    private RowDetailCharacterBinding binding;

    private FilterCharacter filter;

    public AdapterCharacter(Context context, ArrayList<ModelCharacter> characterArrayList){
        this.context = context;
        this.characterArrayList = characterArrayList;
        this.filterList = characterArrayList;
    }

    @NonNull
    @Override
    public HolderCharacter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowDetailCharacterBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderCharacter(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterCharacter.HolderCharacter holder, int position) {

        ModelCharacter model = characterArrayList.get(position);
        String id = model.getId();
        String nome = model.getNome();
        String descrizione = model.getDescrizione();
        String uid = model.getUid();
        long timestamp = model.getTimestamp();

        holder.nomePersonaggio.setText(nome);
        holder.descrizionePersonaggio.setText(descrizione);

       /* holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });*/


    }

    @Override
    public int getItemCount() {
        return characterArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterCharacter(filterList, this);
        }
        return filter;
    }

    class HolderCharacter extends RecyclerView.ViewHolder{

        ImageView imageCharacter;
        TextView nomePersonaggio;
        TextView descrizionePersonaggio;

        // ImageButton deleteBtn;


        public HolderCharacter(@NonNull View itemView){
            super(itemView);

            nomePersonaggio = binding.nomePersonaggio;
            descrizionePersonaggio = binding.descripzionePersonaggio;
            imageCharacter = binding.imageView;
            //deleteBtn = binding.deleteBtn;

        }
    }


}
