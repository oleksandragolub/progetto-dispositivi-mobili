package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.RowCommentBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.MyApplication;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelComment;

public class AdapterComment extends  RecyclerView.Adapter<AdapterComment.HolderComment>{

    private Context context;
    private ArrayList<ModelComment> commentArrayList;
    private FirebaseAuth firebaseAuth;

    private RowCommentBinding binding;

    public AdapterComment(Context context, ArrayList<ModelComment> commentArrayList) {
        this.context = context;
        this.commentArrayList = commentArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderComment(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderComment holder, int position) {

        ModelComment modelComment = commentArrayList.get(position);
        String id = modelComment.getId();
        String comicsId = modelComment.getComicsId();
        String comment = modelComment.getComment();
        String uid = modelComment.getUid();
        String timestamp = modelComment.getTimestamp();

        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

        holder.dateTv.setText(date);
        holder.commentTv.setText(comment);

        loadUserDetails(modelComment, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseAuth.getCurrentUser() != null && uid.equals(firebaseAuth.getUid())){
                    deleteComment(modelComment, holder);
                }
            }
        });

    }

    private void deleteComment(ModelComment modelComment, HolderComment holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Eliminazione del commento").setMessage("Sei sicuro che ti serve eliminare questo commento?")
                .setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
                        ref.child(modelComment.getComicsId())
                                .child("Comments")
                                .child(modelComment.getId())
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        String toast_text = context.getString(R.string.toast_delete);
                                        Toast.makeText(context, toast_text, Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Failed to delete due to "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).setNegativeButton("Cancella", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void loadUserDetails(ModelComment modelComment, HolderComment holder) {
        String uid = modelComment.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Utenti registrati");
        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = "" + snapshot.child("username").getValue();
                String profileImage = ""+snapshot.child("profileImage").getValue();

                if (name != null && !name.isEmpty()) {
                    holder.nameTv.setText(name);
                } else {
                    holder.nameTv.setText("Anonymous"); // un valore di default se il nome è nullo o vuoto
                }

                try {
                    Glide.with(context).load(profileImage).placeholder(R.drawable.baseline_person_24_gray).into(holder.profileIv);
                } catch (Exception e){
                    holder.profileIv.setImageResource(R.drawable.baseline_person_24_gray);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String toast_text = context.getString(R.string.toast_user_detail);
                Toast.makeText(context, toast_text + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentArrayList.size();
    }

    class HolderComment extends RecyclerView.ViewHolder{

        ShapeableImageView profileIv;
        TextView nameTv, dateTv, commentTv;

        public HolderComment(@NonNull View itemView){
            super(itemView);

            profileIv = binding.profileIv;
            nameTv = binding.nameTv;
            dateTv = binding.dateTv;
            commentTv = binding.commentTv;
        }
    }
}
