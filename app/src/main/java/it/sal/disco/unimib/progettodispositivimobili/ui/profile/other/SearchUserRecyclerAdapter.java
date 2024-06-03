package it.sal.disco.unimib.progettodispositivimobili.ui.profile.other;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
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
       /* if (fragment != null && fragment.isAdded()) {
            Glide.with(fragment).load(user.getDataImage()).into(holder.binding.imageProfilePic);
        }*/
        holder.binding.emailText.setText(user.getEmail());
        holder.binding.usernameText.setText(user.getUsername());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final SearchUserRecyclerRowBinding binding;

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


