package it.sal.disco.unimib.progettodispositivimobili.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentAdminHomeBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.ComicsUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelCategory;

public class HomeAdminFragment extends Fragment {

    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;
    private FragmentAdminHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        return root;
    }


    private void setupViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter = new HomeAdminFragment.ViewPagerAdapter(getActivity().getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, getActivity());

        categoryArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();

                ModelCategory modelAll = new ModelCategory("01", "All", "", 1);
                ModelCategory modelMostViewed = new ModelCategory("02", "Most Viewed", "", 1);
                ModelCategory modelMostDownloaded = new ModelCategory("03", "Most Downloaded", "", 1);

                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
                categoryArrayList.add(modelMostDownloaded);

                viewPagerAdapter.addFragment(ComicsUserFragment.newInstance(modelAll.getId(), modelAll.getCategory(), modelAll.getUid()), modelAll.getCategory());
                viewPagerAdapter.addFragment(ComicsUserFragment.newInstance(modelMostViewed.getId(), modelMostViewed.getCategory(), modelMostViewed.getUid()), modelMostViewed.getCategory());
                viewPagerAdapter.addFragment(ComicsUserFragment.newInstance(modelMostDownloaded.getId(), modelMostDownloaded.getCategory(), modelMostDownloaded.getUid()), modelMostDownloaded.getCategory());

                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    if (model != null) {
                        categoryArrayList.add(model);
                        viewPagerAdapter.addFragment(ComicsUserFragment.newInstance(model.getId(), model.getCategory(), model.getUid()), model.getCategory());
                    }
                }

                viewPager.setAdapter(viewPagerAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Gestisci l'errore se necessario
            }

        });binding.tabLayout.setupWithViewPager(viewPager);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<ComicsUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;


        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(ComicsUserFragment fragment, String title){
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position){
            return fragmentTitleList.get(position);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }
}