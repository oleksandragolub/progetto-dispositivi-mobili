package it.sal.disco.unimib.progettodispositivimobili.ui.characters.resto;

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

import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.MarvelService;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.Root;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.Result;

import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentCharacterDetailsBinding;

public class CharacterDetailsFragment extends Fragment {
    private static final String PUBLIC_API_KEY = "93e5146b36c6609ec6a87d8104728ed2";
    private static final String PRIVATE_API_KEY = "80e7b32472204a8f30779ecb3e20815e84384d7b";
    private static final String TAG = "CharacterDetailsFragment";
    private FragmentCharacterDetailsBinding binding;

    private ArrayList<ModelCharacter> characterArrayList;

    private AdapterCharacter adapterCharacter;

    private static final String TS = "1";
    private static final String HASH = "";
    private static final String HOSTNAME = "https://gateway.marvel.com/";
    private static final String SERVICE = "v1/public/chatacters?";

    private ImageView imgCharacter = null;
    TextInputEditText namePerson;
    TextView descriptionP;
    Button btnGetInfo;

    private EditText txtSearch = null;
    private TextView lblName = null;
    private TextView lblDescribed = null;
    private ImageView imgSuperHero = null;
    private ProgressBar progress = null;
    private MarvelService service = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCharacterDetailsBinding.inflate(inflater, container, false);
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

       /*binding.searchPersonaggio.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {

           }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
               try {
                   adapterCharacter.getFilter().filter(s);
               }catch (Exception e){}
           }

           @Override
           public void afterTextChanged(Editable s) {

           }
       });*/


        //loadComicsDetails();
        //initViews();
      /*  initEvents();

        return root;
    }*/

   /* public void initViews() {
        btnGetInfo = binding.obtenerinfo;
        imgCharacter = binding.;
        namePerson = binding.textViewNamePersonaggio;
        descriptionP = binding.descriptionPersonaggio;
    }*/


    //ELENCA TUTTI PERSONAGGI
  /*  public void initEvents() {
        btnGetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInfo();
            }
        });
    }

    public void getInfo() {
        String name = namePerson.getText().toString();

        String urlSearch = HOSTNAME + SERVICE
                + "ts=" + TS
                + "&apikey=" + PUBLIC_KEY
                + "&hash=" + HASH
                + "&name=" + name;

        urlSearch = urlSearch.replace("http:", "https:");

        try {
            URL url = new URL(urlSearch);
            new DownloadDataTask().execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private class DownloadDataTask extends AsyncTask<URL, Void, Root> {

        @Override
        protected Root doInBackground(URL... urls) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = urls[0];
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                String response = new java.util.Scanner(in).useDelimiter("\\A").next();

                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson json = gsonBuilder.create();
                return json.fromJson(response, Root.class);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(Root root) {
            if (root != null && root.getData().getResults().size() > 0) {
                showInfo(root.getData().getResults().get(0));
            }
        }
    }

    public void showInfo(Result result) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            Thumbnail thumbnail = result.getThumbnail();
            String description = result.getDescription();
            String urlImage = thumbnail.getPath() + "." + thumbnail.getExtension();
            descriptionP.setText(description);
            getImage(urlImage);
        });
    }

    public void getImage(String urlImage) {
        try {
            URL url = new URL(urlImage.replace("http:", "https:"));
            new DownloadImageTask().execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private class DownloadImageTask extends AsyncTask<URL, Void, Bitmap> {
        private ImageView imageView;

        public DownloadImageTask() {
            this.imageView = imgCharacter;
        }

        @Override
        protected Bitmap doInBackground(URL... urls) {
            try {
                URL url = urls[0];
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}*/

