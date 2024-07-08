package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve;

import android.util.Log;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {
    private static final String TAG = "RetryInterceptor";
    private int maxRetries;
    private int retryCount = 0;

    public RetryInterceptor(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        IOException exception = null;

        while (retryCount < maxRetries) {
            try {
                response = chain.proceed(request);
                if (response.isSuccessful()) {
                    return response;
                }
            } catch (IOException e) {
                exception = e;
                retryCount++;
                Log.d(TAG, "Retrying... (" + retryCount + " out of " + maxRetries + ")");
            }
        }

        if (response == null && exception != null) {
            throw exception;  // Lancia l'eccezione se la risposta è null e c'è un'eccezione
        }

        return response != null ? response : chain.proceed(request);  // Assicura che venga sempre restituita una risposta
    }
}