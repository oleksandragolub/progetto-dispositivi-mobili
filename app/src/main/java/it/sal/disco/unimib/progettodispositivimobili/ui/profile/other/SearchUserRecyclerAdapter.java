package it.sal.disco.unimib.progettodispositivimobili.ui.profile.other;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.databinding.SearchUserRecyclerRowBinding;

public class SearchUserRecyclerAdapter extends RecyclerView.Adapter<SearchUserRecyclerAdapter.MyViewHolder> {

    private List<ReadWriteUserDetails> dataList;
    private OnUserClickListener onUserClickListener;

    public SearchUserRecyclerAdapter(List<ReadWriteUserDetails> dataList, OnUserClickListener onUserClickListener) {
        this.dataList = dataList;
        this.onUserClickListener = onUserClickListener;
    }

    public interface OnUserClickListener {
        void onUserClick(ReadWriteUserDetails user);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SearchUserRecyclerRowBinding binding = SearchUserRecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ReadWriteUserDetails user = dataList.get(position);

        holder.binding.emailText.setText(user.getEmail());
        holder.binding.usernameText.setText(user.getUsername());

        // Carica l'immagine utilizzando Glide
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfileImage())
                    .placeholder(R.drawable.baseline_person_24_gray) // Immagine di placeholder
                    .error(R.drawable.baseline_person_24_gray) // Immagine di errore
                    .into(holder.binding.imageProfilePic);
        } else {
            // Se non c'è un'immagine di profilo, mostra un'immagine di default
            holder.binding.imageProfilePic.setImageResource(R.drawable.baseline_person_24_gray);
        }

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final SearchUserRecyclerRowBinding binding;
        ShapeableImageView profileIv;

        public MyViewHolder(SearchUserRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onUserClickListener != null) {
                    onUserClickListener.onUserClick(dataList.get(position));
                }
            });
        }
    }
}


