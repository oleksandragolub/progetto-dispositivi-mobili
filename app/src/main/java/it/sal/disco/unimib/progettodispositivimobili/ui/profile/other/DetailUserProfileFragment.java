package it.sal.disco.unimib.progettodispositivimobili.ui.profile.other;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentDetailUserProfileBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.chat.ChatMessengerFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.ui.chat.chats.ChatUtil;
import it.sal.disco.unimib.progettodispositivimobili.ui.preferiti.DetailUserPreferitiFragment;

public class DetailUserProfileFragment extends Fragment {

    FragmentDetailUserProfileBinding binding;

    ImageView profileImageView, profileImageViewCamera;
    TextInputEditText usernameEditText, emailEditText, dobEditText, genderEditText, descrizioneEditText;
    Button profileChatButton;
    TextView btnBack;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference reference;

    String profileUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetailUserProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getArguments() != null) {
            profileUserId = getArguments().getString("user2");
        } else {
            profileUserId = "";
        }

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        btnBack = binding.txtBack;
        profileChatButton = binding.profileChatBtn;
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Utenti registrati");

        profileImageView = binding.profileImageView;
        usernameEditText = binding.textViewUsername;
        emailEditText = binding.textViewEmail;
        dobEditText = binding.textViewDoB;
        genderEditText = binding.textViewGender;

        binding.profileChatBtn.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null && profileUserId != null) {
                String currentUserId = mAuth.getCurrentUser().getUid();
                String chatId = ChatUtil.generateChatId(currentUserId, profileUserId);

                FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    ReadWriteUserDetails user = new ReadWriteUserDetails();
                                    user.setUserId(profileUserId);
                                    ChatUtil.createChat(user);
                                    Toast.makeText(getContext(), "Questo chat è stato aggiunto nella lista dei tuoi chat.", Toast.LENGTH_SHORT).show();
                                }
                                openChatFragment(chatId, profileUserId);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getContext(), "Failed to retrieve chat details.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(getContext(), "User ID is missing.", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> {
            if(getActivity() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.buttonFavorite.setOnClickListener(v -> {
            if (getActivity() != null) {
                openFavoriteFragment(profileUserId);
            }
        });

        showUserProfile(profileUserId);

        return root;
    }

    private void openFavoriteFragment(String userId) {
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);

        DetailUserPreferitiFragment preferitiFragment = new DetailUserPreferitiFragment();
        preferitiFragment.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, preferitiFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openChatFragment(String chatId, String userId2) {
        ChatMessengerFragment fragment = new ChatMessengerFragment();
        Bundle args = new Bundle();
        args.putString("chatId", chatId);
        args.putString("userId2", userId2);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showUserProfile(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Utenti registrati").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (binding == null) {
                    return; // The view has been destroyed, so we should not proceed
                }

                ReadWriteUserDetails userDetails = dataSnapshot.getValue(ReadWriteUserDetails.class);
                if (userDetails != null) {
                    usernameEditText.setText(userDetails.getUsername());
                    emailEditText.setText(userDetails.getEmail());
                    dobEditText.setText(userDetails.getDob());
                    genderEditText.setText(userDetails.getGender());

                    String profileImage = "" + dataSnapshot.child("profileImage").getValue();

                    if (getActivity() != null) {
                        Glide.with(getActivity())
                                .load(profileImage)
                                .placeholder(R.drawable.profile_icone)
                                .into(binding.profileImageView);
                    }
                } else {
                    Toast.makeText(getContext(), "Dettagli utente non trovati.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (binding != null) {
                    Toast.makeText(getContext(), "Errore nel caricamento dei dettagli utente.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openFragment(Fragment fragment){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment); // Utilizza il fragment fornito come parametro
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface OnUserClickListener {
        void onUserClick(ReadWriteUserDetails user);
    }
}
