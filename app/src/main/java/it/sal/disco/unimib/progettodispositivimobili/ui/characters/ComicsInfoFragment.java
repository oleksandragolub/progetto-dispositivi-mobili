package it.sal.disco.unimib.progettodispositivimobili.ui.characters;

import androidx.fragment.app.Fragment;

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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsInfoBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.Url;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.Comic;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.ComicDataWrapper;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.MarvelComicService;

public class ComicsInfoFragment extends Fragment {
    private EditText txtSearch = null;
    private TextView lblTitle = null;
    private TextView lblDescribed = null;
    private TextView lblDetails = null;
    private ImageView imgComic = null;
    private ProgressBar progress = null;
    private MarvelComicService service = null;

    final String PUBLIC_API_KEY = "93e5146b36c6609ec6a87d8104728ed2";
    final String PRIVATE_API_KEY = "80e7b32472204a8f30779ecb3e20815e84384d7b";
    private static final String TAG = "ComicInfoFragment";
    private FragmentComicsInfoBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComicsInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initViews(root);

        service = new MarvelComicService(PUBLIC_API_KEY, PRIVATE_API_KEY);

        return root;
    }

    public void initViews(View root) {
        txtSearch = root.findViewById(R.id.txtSearch);
        lblTitle = root.findViewById(R.id.lblTitle);
        lblDescribed = root.findViewById(R.id.lblDescribed);
        imgComic = root.findViewById(R.id.imgComic);
        progress = root.findViewById(R.id.progress);
        lblDetails = root.findViewById(R.id.lblDetails);

        Button button = root.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnGetComicInfoOnClick(v);
            }
        });
    }

    public void btnGetComicInfoOnClick(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        StringBuilder text = new StringBuilder();

        if (txtSearch.getText().toString().isEmpty()) {
            text.append(getString(R.string.empty));
            alert.setMessage(text);
            alert.setPositiveButton(R.string.close, null);
            alert.show();
        } else {
            lblTitle.setVisibility(View.INVISIBLE);
            imgComic.setVisibility(View.GONE);
            lblDescribed.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            getComicInfo(txtSearch.getText().toString());
        }
    }

    public void getComicInfo(String title) {
        Log.d(TAG, "Requesting data for: " + title);
        service.requestComicData(title, (isNetworkError, statusCode, root) -> {
            getActivity().runOnUiThread(() -> {
                progress.setVisibility(View.INVISIBLE); // Hide the ProgressBar

                if (!isNetworkError && statusCode == 200 && root != null) {
                    Log.d(TAG, "Data received successfully");
                    showComicInfo(root);
                } else {
                    showAlert(getString(R.string.service_error) + " Code: " + statusCode);
                }
            });
        });
    }

    @SuppressLint("SetTextI18n")
    public void showComicInfo(ComicDataWrapper root) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            if (root.getData().getResults().isEmpty()) {
                showAlert(getString(R.string.no_exist));
            } else {
                Comic comic = root.getData().getResults().get(0);
                String title = comic.getTitle();
                String description = comic.getDescription();
                String urlImage = comic.getThumbnail().getPath() + "." + comic.getThumbnail().getExtension();

                Log.d(TAG, "Comic Title: " + title);
                Log.d(TAG, "Comic Description: " + description);

                lblTitle.setText(title);

                if (description != null && !description.isEmpty()) {
                    lblDescribed.setText(description);
                } else {
                    lblDescribed.setText(R.string.no_description_available);
                }

                getImage(urlImage);

                // Mostra altri dettagli del fumetto
                StringBuilder details = new StringBuilder();
                details.append("ID: ").append(comic.getId()).append("\n");

                if (comic.getUrls() != null) {
                    for (Url url : comic.getUrls()) {
                        details.append("URL: ").append(url.getType()).append(" - ").append(url.getUrl()).append("\n");
                    }
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
            getActivity().runOnUiThread(() -> showImage(bitmap, imgComic));
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
            getActivity().runOnUiThread(() -> {
                progress.setVisibility(View.INVISIBLE);
                showAlert(getString(R.string.image_download_error));
            });
        }
    }

    public void showImage(Bitmap image, ImageView imageView) {
        lblTitle.setVisibility(View.VISIBLE);
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