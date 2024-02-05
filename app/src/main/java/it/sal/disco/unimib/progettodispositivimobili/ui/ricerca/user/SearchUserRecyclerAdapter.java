package it.sal.disco.unimib.progettodispositivimobili.ui.ricerca.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.databinding.SearchUserRecyclerRowBinding;

public class SearchUserRecyclerAdapter extends RecyclerView.Adapter<SearchUserRecyclerAdapter.MyViewHolder> {

    private List<ReadWriteUserDetails> dataList;
    private OnUserClickListener onUserClickListener; // Aggiungi questa variabile
    private Fragment fragment;

    //private FragmentManager fragmentManager;

    /*public SearchUserRecyclerAdapter(List<ReadWriteUserDetails> dataList){
        this.dataList = dataList;
    }*/
    public SearchUserRecyclerAdapter(List<ReadWriteUserDetails> dataList, OnUserClickListener onUserClickListener) {
        this.dataList = dataList;
        this.onUserClickListener = onUserClickListener; // Inizializza il listener
    }

    public interface OnUserClickListener {
        void onUserClick(ReadWriteUserDetails user);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Utilizzo del View Binding per inflazionare il layout
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_user_recycler_row, parent, false);
        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ReadWriteUserDetails dataItem = dataList.get(position);

        if (fragment != null && fragment.isAdded()) {
            Glide.with(fragment)
                    .load(dataItem.getDataImage())
                    .into(holder.profilePic);
        }

        holder.emailText.setText(dataItem.getEmail());
        holder.usernameText.setText(dataItem.getUsername());


        /*holder.userRowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailUserProfileFragment.class);
                intent.putExtra("Image", dataList.get(holder.getAdapterPosition()).getDataImage());
                intent.putExtra("Username", dataList.get(holder.getAdapterPosition()).getEmail());
                intent.putExtra("Email", dataList.get(holder.getAdapterPosition()).getUsername());

                context.startActivity(intent);
            }
        });*/

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        SearchUserRecyclerRowBinding binding;
        TextView usernameText;
        TextView emailText;
        ImageView profilePic;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.username_text);
            emailText = itemView.findViewById(R.id.email_text);
            profilePic = itemView.findViewById(R.id.image_profile_pic);
            //userRowLayout = itemView.findViewById(R.id.Serch_User_Row_Layout);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onUserClickListener != null) {
                    onUserClickListener.onUserClick(dataList.get(position)); // Passa l'utente cliccato al listener
                }
            });
        }

    }
}


