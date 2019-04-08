package cs4330.cs.utep.snakereloaded;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {
    Button play;
    Button options;
    TextView tittle;

    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tittle = findViewById(R.id.tittle);

        settings = this.getSharedPreferences("settings", Context.MODE_PRIVATE);

        String theme = settings.getString("theme", "colorful");

        if(theme.equalsIgnoreCase("colorful")) {
            tittle.getRootView().setBackgroundColor(Color.argb(255,255,255,255));
            tittle.setTextColor(Color.argb(255,50,204,50));
        }
        else if(theme.equalsIgnoreCase("blacknwhite")) {
            tittle.getRootView().setBackgroundColor(Color.argb(255,0,0,0));
            tittle.setTextColor(Color.argb(255,255,255,255));
        }
        else if (theme.equalsIgnoreCase("retro")) {
            tittle.getRootView().setBackgroundColor(Color.argb(255,156,203,149));
            tittle.setTextColor(Color.argb(255,0,0,0));
        }

        play = findViewById(R.id.play);
        options = findViewById(R.id.options);

        play.setOnClickListener(view ->{
            Intent startGame = new Intent(MainActivity.this, SnakeGame.class);
            startActivity(startGame);
        });

        options.setOnClickListener(view ->{
            Intent options = new Intent(MainActivity.this, OptionsActivity.class);
            startActivity(options);
        });
    }
}
