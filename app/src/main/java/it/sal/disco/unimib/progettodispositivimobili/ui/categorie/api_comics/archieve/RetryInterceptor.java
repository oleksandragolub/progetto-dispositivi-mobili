package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {
    private int maxRetries;
    private int retryCount = 0;

    public RetryInterceptor(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        boolean responseOK = false;
        IOException exception = null;

        while (!responseOK && retryCount < maxRetries) {
            try {
                response = chain.proceed(request);
                responseOK = response.isSuccessful();
            } catch (IOException e) {
                exception = e;
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw e;  // Lancia l'eccezione se ha raggiunto il numero massimo di tentativi
                }
            }
        }

        if (response == null && exception != null) {
            throw exception;  // Lancia l'eccezione se la risposta è null e c'è un'eccezione
        }

        return response;
    }
}