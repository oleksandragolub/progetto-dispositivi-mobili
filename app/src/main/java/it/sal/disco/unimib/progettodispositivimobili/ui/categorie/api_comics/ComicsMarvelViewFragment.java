package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsMarvelViewBinding;

public class ComicsMarvelViewFragment extends Fragment {
    private FragmentComicsMarvelViewBinding binding;

    private static final String TAG = "ComicsMarvelViewFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComicsMarvelViewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getArguments() != null) {
            String pdfUrl = getArguments().getString("pdfUrl");
            String title = getArguments().getString("title");
            binding.toolbarTitleTv.setText(title);
            new DownloadFileTask().execute(pdfUrl);
        }

        binding.buttonBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return root;
    }

    private class DownloadFileTask extends AsyncTask<String, Void, File> {
        @Override
        protected File doInBackground(String... strings) {
            String fileUrl = strings[0];
            File pdfFile = null;
            try {
                pdfFile = new File(getActivity().getCacheDir(), "downloaded.pdf");
                if (pdfFile.exists()) {
                    pdfFile.delete();
                }
                pdfFile.createNewFile();

                URL url = new URL(fileUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);

                byte[] buffer = new byte[1024];
                int bufferLength;

                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, bufferLength);
                }

                fileOutputStream.close();
                inputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return pdfFile;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            if (file != null) {

                if (binding != null && binding.pdfView != null) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.pdfView.fromFile(file)
                            .enableSwipe(true)
                            .swipeHorizontal(true)
                            .pageSnap(true)
                            .pageFling(true)
                            .autoSpacing(true)  // Optional: turn off auto spacing
                            .pageFitPolicy(FitPolicy.HEIGHT) // Fit each page both vertically and horizontally
                            // .spacing(5)  // Set spacing between pages to 0
                            .onPageChange(new OnPageChangeListener() {
                                @Override
                                public void onPageChanged(int page, int pageCount) {
                                    int currentPage = page + 1;
                                    binding.toolbarSubtitleTv.setText(currentPage + "/" + pageCount);
                                }
                            })
                            .onError(new OnErrorListener() {
                                @Override
                                public void onError(Throwable t) {
                                    if (getActivity() != null) {
                                        Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .onPageError(new OnPageErrorListener() {
                                @Override
                                public void onPageError(int page, Throwable t) {
                                    if (getActivity() != null) {
                                        Toast.makeText(getActivity(), "Error on page " + page + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .load();
                }
            } else {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Failed to download PDF", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
