package it.sal.disco.unimib.progettodispositivimobili.ui.categorie;

import static it.sal.disco.unimib.progettodispositivimobili.ui.categorie.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterPdfComicsAdmin;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

public class MyApplication extends Application {



    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

   /* @Override
    public void onCreate() {
        super.onCreate();
    }*/

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());
    }

    public static final String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ITALY);
        cal.setTimeInMillis(timestamp);

        //String date = DateFormat.getDateInstance().format("dd/MM/yyyy", cal).toString();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);
        String date = sdf.format(cal.getTime());

        return date;
    }

    public static void deleteComics(Context context, String comicsId, String comicsUrl, String comicsTitolo) {
        String TAG = "DELETE_BOOK_TAG";

        Log.d(TAG, "deleteComics: Deleting...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Per favore aspetta un attimo");
        progressDialog.setMessage("Deleting " + comicsTitolo + "...");
        progressDialog.show();

        Log.d(TAG, "deleteComics: Deleting from storage...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(comicsUrl);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: Deleting from storage");
                Log.d(TAG, "onSuccess: Now deleting info from db");
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comics");
                reference.child(comicsId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted from db too");
                        progressDialog.dismiss();
                        Toast.makeText(context, "Comics Deleted Successfully...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from db due to "+ e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Failed to delete from storage due to " + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTV) {
        String TAG = "PDF_SIZE_TAG";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                double bytes = storageMetadata.getSizeBytes();
                Log.d(TAG, "onSuccess: "+ pdfTitle + " " + bytes);

                double kb = bytes/1024;
                double mb = kb/1024;

                if (mb >= 1){
                    sizeTV.setText(String.format("%.2f", mb)+" MB");
                } else if (kb >= 1){
                    sizeTV.setText(String.format("%.2f", kb)+" KB");
                } else {
                    sizeTV.setText(String.format("%.2f", bytes)+" bytes");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: "+ e.getMessage());
            }
        });
    }


    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar) {
        String TAG = "PDF_LOAD_SINGLE_TAG";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG, "onSuccess: "+ pdfTitle + " succesfylly got the file");

                pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError(new OnErrorListener() {
                            @Override
                            public void onError(Throwable t) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "onError: "+ t.getMessage());
                            }
                        }).onPageError(new OnPageErrorListener(){
                            @Override
                            public void onPageError(int page, Throwable t) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "onPageError: "+ t.getMessage());
                            }
                        }).onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "loadComplete: pdf loaded");
                            }
                        })
                        .load();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                //Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: failed getting file from url due to "+ e.getMessage());
            }
        });
    }

    public static void loadCategory(String categoryId, TextView categoryTV) {
        String TAG = "PDF_LOAD_CATEGORY_TAG";

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String category = "" + snapshot.child("category").getValue();
                categoryTV.setText(category);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    public static void incrementComicsViewCoint(String comicsId) {
        String TAG = "PDF_LOAD_CATEGORY_TAG";

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.child(comicsId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String viewsCount= "" + snapshot.child("viewsCount").getValue();

                if(viewsCount.equals("") || viewsCount.equals("null")){
                    viewsCount = "0";
                }

                long newViewsCount = Long.parseLong(viewsCount) + 1;
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("viewsCount", newViewsCount);

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comics");
                reference.child(comicsId).updateChildren(hashMap);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    private static void incrementComicsDownloadCount(String comicsId) {
        Log.d(TAG_DOWNLOAD, "incrementComicsDownloadCount: Incrementing Comics Download Count");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.child(comicsId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                Log.d(TAG_DOWNLOAD, "onDataChange: Downloads Count: " + downloadsCount);

                if (downloadsCount.equals("") || downloadsCount.equals("null")){
                    downloadsCount = "0";
                }

                long newDownloadsCount = Long.parseLong(downloadsCount) + 1;
                Log.d(TAG_DOWNLOAD, "onDataChange: New Download Count: " + newDownloadsCount);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("downloadsCount", newDownloadsCount);

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comics");
                reference.child(comicsId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG_DOWNLOAD, "onSuccess: Downloads Count updated...");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to update Downloads Count due to " + e.getMessage());

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static void downloadComics(Context context, String comicsId, String comicsTitle, String comicsUrl) {
        Log.d(TAG_DOWNLOAD, "downloadComics: downloading comics...");
        String nameWithExtension = comicsTitle + ".pdf";
        Log.d(TAG_DOWNLOAD, "downloadComics: NAME: " + nameWithExtension);

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Per favore aspetta");
        progressDialog.setMessage("Downloading " + nameWithExtension + "...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(comicsUrl);
        storageReference.getBytes(MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG_DOWNLOAD, "onSuccess: Comics Downloaded");
                Log.d(TAG_DOWNLOAD, "onSuccess: Saving comics...");
                saveDownloadedComics(context, progressDialog, bytes, nameWithExtension, comicsId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG_DOWNLOAD, "onFailure: Failed to download due to " + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context, "Failed to download due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void saveDownloadedComics(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String comicsId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedComics: Saving downloaded comics");
        try {
            OutputStream out;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, nameWithExtension);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                out = context.getContentResolver().openOutputStream(uri);
            } else {
                File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsFolder.exists()) {
                    downloadsFolder.mkdirs();
                }

                String filePath = downloadsFolder.getPath() + "/" + nameWithExtension;
                File file = new File(filePath);
                out = new FileOutputStream(file);
            }

            if (out != null) {
                out.write(bytes);
                out.close();
                Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show();
                Log.d(TAG_DOWNLOAD, "saveDownloadedComics: Saved to Download Folder");
                progressDialog.dismiss();

                incrementComicsDownloadCount(comicsId);
            } else {
                throw new Exception("Output stream is null");
            }
        } catch (Exception e) {
            Log.d(TAG_DOWNLOAD, "saveDownloadedComics: Failed saving to Download Folder due to " + e.getMessage());
            Toast.makeText(context, "Failed saving to Download Folder due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }
}