package it.sal.disco.unimib.progettodispositivimobili.ui.start_app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Random;

import it.sal.disco.unimib.progettodispositivimobili.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Randomize bookmark image in the splash activity
        ImageView bookmarkImage = findViewById(R.id.imageView_bookmark);

        int[] images = {R.drawable.bookmark_batman, R.drawable.bookmark_ironman,
                R.drawable.bookmark_joker, R.drawable.bookmark_spiderman, R.drawable.bookmark_superman};

        Random rand = new Random();

        bookmarkImage.setBackgroundResource(images[rand.nextInt(images.length)]);
        //bookmarkImage.setImageResource(images[rand.nextInt(images.length)]);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }, 2000); // dove 2000 significa 2 secondi
    }
}
