package kzn.mrcrabs.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GameOver extends AppCompatActivity {

    TextView tvPoints, tvHighest, tvMode;
    SharedPreferences sharedPreferences;
    ImageView ivNewHighest;
    TableLayout scoreTable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over);

        tvPoints     = findViewById(R.id.tvPoints);
        tvHighest    = findViewById(R.id.tvHighest);
        tvMode       = findViewById(R.id.tvMode);
        ivNewHighest = findViewById(R.id.ivNewHighest);
        scoreTable   = findViewById(R.id.scoreTable);

        boolean isTimeMode = getIntent().getBooleanExtra("timeMode", false);
        int     points     = getIntent().getIntExtra("points", 0);

        tvMode.setText(isTimeMode ? "TIME TRIAL" : "CLASSIC");
        tvPoints.setText(String.valueOf(points));

        // Separate high-score records per mode
        sharedPreferences = getSharedPreferences("my_pref", MODE_PRIVATE);
        String key = isTimeMode ? "highest_time" : "highest";
        int highest = sharedPreferences.getInt(key, 0);

        if (points > highest) {
            ivNewHighest.setVisibility(View.VISIBLE);
            highest = points;
            sharedPreferences.edit().putInt(key, highest).apply();
        }
        tvHighest.setText(String.valueOf(highest));

        // ── Animations ───────────────────────────────────────────────────────
        View root = findViewById(android.R.id.content);
        root.setAlpha(0f);
        root.animate()
                .alpha(1f)
                .setDuration(350)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        scoreTable.setTranslationY(160f);
        scoreTable.setAlpha(0f);
        scoreTable.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(200)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();

        if (ivNewHighest.getVisibility() == View.VISIBLE) {
            ivNewHighest.setScaleX(0f);
            ivNewHighest.setScaleY(0f);
            ivNewHighest.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(450)
                    .setStartDelay(650)
                    .setInterpolator(new OvershootInterpolator(2f))
                    .start();
        }
    }

    public void restart(View view) {
        Intent intent = new Intent(GameOver.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void exit(View view) {
        Intent intent = new Intent(GameOver.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
