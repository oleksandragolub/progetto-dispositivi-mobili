package it.sal.disco.unimib.progettodispositivimobili.ui.characters;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentCharacterInfoBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.MarvelService;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.Result;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.Root;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.Url;

public class CharacterInfoFragment extends Fragment {
    private EditText txtSearch = null;
    private TextView lblName = null;
    private TextView lblDescribed = null;
    private TextView lblDetails = null;
    private ImageView imgSuperHero = null;
    private ProgressBar progress = null;
    private MarvelService service = null;

    final String PUBLIC_API_KEY = "93e5146b36c6609ec6a87d8104728ed2";
    final String PRIVATE_API_KEY = "80e7b32472204a8f30779ecb3e20815e84384d7b";
    private static final String TAG = "CharacterInfoFragment";
    private FragmentCharacterInfoBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCharacterInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initViews(root);

        service = new MarvelService(PUBLIC_API_KEY, PRIVATE_API_KEY);

        return root;
    }

    public void initViews(View root) {
        txtSearch = root.findViewById(R.id.txtSearch);
        lblName = root.findViewById(R.id.lblName);
        lblDescribed = root.findViewById(R.id.lblDescribed);
        imgSuperHero = root.findViewById(R.id.imgSuperHero);
        progress = root.findViewById(R.id.progress);
        lblDetails = root.findViewById(R.id.lblDetails);

        Button button = root.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnGetCharacterInfoOnClick(v);
            }
        });
    }

    public void btnGetCharacterInfoOnClick(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        StringBuilder text = new StringBuilder();

        if (txtSearch.getText().toString().isEmpty()) {
            text.append(getString(R.string.empty));
            alert.setMessage(text);
            alert.setPositiveButton(R.string.close, null);
            alert.show();
        } else {
            lblName.setVisibility(View.INVISIBLE);
            imgSuperHero.setVisibility(View.GONE);
            lblDescribed.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            getCharacterInfo(txtSearch.getText().toString());
        }
    }

    public void getCharacterInfo(String name) {
        Log.d(TAG, "Requesting data for: " + name);
        service.requestCharacterData(name, (isNetworkError, statusCode, root) -> {
            getActivity().runOnUiThread(() -> {
                progress.setVisibility(View.INVISIBLE); // Hide the ProgressBar

                if (!isNetworkError && statusCode == 200 && root != null) {
                    Log.d(TAG, "Data received successfully");
                    showCharacterInfo(root);
                } else {
                    showAlert(getString(R.string.service_error) + " Code: " + statusCode);
                }
            });
        });
    }

    @SuppressLint("SetTextI18n")
    public void showCharacterInfo(Root root) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            if (root.getData().getResults().isEmpty()) {
                showAlert(getString(R.string.no_exist));
            } else {
                Result character = root.getData().getResults().get(0);
                String name = character.getName();
                String description = character.getDescription();
                String urlImage = character.getThumbnail().getPath() + "." + character.getThumbnail().getExtension();

                Log.d(TAG, "Character Name: " + name);
                Log.d(TAG, "Character Description: " + description);

                lblName.setText(name);

                if (description != null && !description.isEmpty()) {
                    lblDescribed.setText(description);
                } else {
                    lblDescribed.setText(R.string.no_description_available);
                }

                getImage(urlImage);

                // Mostra altri dettagli del personaggio
                StringBuilder details = new StringBuilder();
                details.append("ID: ").append(character.getId()).append("\n");
                details.append("Modified: ").append(character.getModified()).append("\n");

                if (character.getUrls() != null) {
                    for (Url url : character.getUrls())
                        details.append("URL: ").append(url.getType()).append(" - ").append(url.getUrl()).append("\n");
                }

                if (character.getComics() != null) {
                    details.append("Comics: ").append(character.getComics().getAvailable()).append("\n");
                }

                if (character.getStories() != null) {
                    details.append("Stories: ").append(character.getStories().getAvailable()).append("\n");
                }

                if (character.getEvents() != null) {
                    details.append("Events: ").append(character.getEvents().getAvailable()).append("\n");
                }

                if (character.getSeries() != null) {
                    details.append("Series: ").append(character.getSeries().getAvailable()).append("\n");
                }

                // Assicurati di avere un TextView per visualizzare questi dettagli
                lblDetails.setText(details.toString());
            }
        });
    }

    public void getImage(String urlImage) {
        urlImage = urlImage.replace("http", "https");
        try {
            URL url = new URL(urlImage);
            new Thread(() -> downloadImage(url)).start();
        } catch (MalformedURLException e) {
            Log.d(TAG, "MalformedURLException: " + e.toString());
            getActivity().runOnUiThread(() -> {
                progress.setVisibility(View.INVISIBLE);
                showAlert(getString(R.string.image_download_error));
            });
        }
    }

    public void downloadImage(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            getActivity().runOnUiThread(() -> showImage(bitmap, imgSuperHero));
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
            getActivity().runOnUiThread(() -> {
                progress.setVisibility(View.INVISIBLE);
                showAlert(getString(R.string.image_download_error));
            });
        }
    }

    public void showImage(Bitmap image, ImageView imageView) {
        lblName.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(image);
        imageView.setVisibility(View.VISIBLE);
        lblDescribed.setVisibility(View.VISIBLE);
        progress.setVisibility(View.INVISIBLE); // Hide the ProgressBar when the image is displayed
    }

    private void showAlert(String message) {
        if (getActivity() == null) {
            return;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setMessage(message);
        alert.setPositiveButton(R.string.close, null);
        alert.show();
        progress.setVisibility(View.INVISIBLE); // Hide the ProgressBar in case of error
    }
}
