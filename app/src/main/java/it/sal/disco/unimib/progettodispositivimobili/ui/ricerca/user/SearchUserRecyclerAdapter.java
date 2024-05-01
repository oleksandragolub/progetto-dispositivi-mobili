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


   /* @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ReadWriteUserDetails dataItem = dataList.get(position);

        if (fragment != null && fragment.isAdded()) {
            Glide.with(fragment)
                    .load(dataItem.getDataImage())
                    .into(holder.profilePic);
        }

        holder.emailText.setText(dataItem.getEmail());
        holder.usernameText.setText(dataItem.getUsername());
*/

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

  //  }

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


   /* public class MyViewHolder extends RecyclerView.ViewHolder {

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
*/
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


