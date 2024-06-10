package it.sal.disco.unimib.progettodispositivimobili.ui.ricerca.comics;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentRicercaBinding;


public class RicercaFragment extends Fragment {

    private FragmentRicercaBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        RicercaViewModel ricercaViewModel =
                new ViewModelProvider(getActivity()).get(RicercaViewModel.class);

        binding = FragmentRicercaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textRicerca;
        ricercaViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}