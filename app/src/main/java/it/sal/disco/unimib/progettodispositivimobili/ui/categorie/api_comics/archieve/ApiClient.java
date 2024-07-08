package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve;

import java.util.concurrent.TimeUnit;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ApiClient {
    private static final String BASE_URL = "https://Aleksa01Go.pythonanywhere.com";
    private static Retrofit retrofit = null;
    private static final int TIMEOUT = 60;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Configura l'istanza di HttpLoggingInterceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configura l'istanza di OkHttpClient con l'interceptor di logging e retry
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(new RetryInterceptor(5))  // 5 tentativi di retry
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
