package cs4330.cs.utep.snakereloaded;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class OptionsActivity extends Activity {

    Button easy;
    Button regular;
    Button hardcore;

    Button colorful;
    Button blacknwhite;
    Button retro;

    Button back;

    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options_layout);
        settings = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
        easy = findViewById(R.id.easy);
        regular = findViewById(R.id.regular);
        hardcore = findViewById(R.id.hardcore);

        colorful = findViewById(R.id.colorful);
        blacknwhite = findViewById(R.id.backnwhite);
        retro = findViewById(R.id.retro);
        back = findViewById(R.id.back);

        TextView difTittle = findViewById(R.id.difficulty);
        TextView theTittle = findViewById(R.id.theme);

        long difficulty = settings.getLong("difficulty", 1000);
        String theme = settings.getString("theme", "colorful");

        // Disable the current difficulty
        if(difficulty == 1000)
            easy.setEnabled(false);
        else if(difficulty == 500)
            regular.setEnabled(false);
        else if (difficulty == 250)
            hardcore.setEnabled(false);

        // Disable the current theme
        if(theme.equalsIgnoreCase("colorful")) {
            colorful.setEnabled(false);
            colorful.getRootView().setBackgroundColor(Color.argb(255,255,255,255));
            difTittle.setTextColor(Color.argb(255,50,204,50));
            theTittle.setTextColor(Color.argb(255,50,204,50));
        }
        else if(theme.equalsIgnoreCase("blacknwhite")) {
            blacknwhite.setEnabled(false);
            blacknwhite.getRootView().setBackgroundColor(Color.argb(255,0,0,0));
            difTittle.setTextColor(Color.argb(255,255,255,255));
            theTittle.setTextColor(Color.argb(255,255,255,255));
        }
        else if (theme.equalsIgnoreCase("retro")) {
            retro.setEnabled(false);
            retro.getRootView().setBackgroundColor(Color.argb(255,156,203,149));
            difTittle.setTextColor(Color.argb(255,0,0,0));
            theTittle.setTextColor(Color.argb(255,0,0,0));
        }

        SharedPreferences.Editor editor = settings.edit();

        // Select difficulty
        easy.setOnClickListener(view ->{
            editor.putLong("difficulty", 1000);
            editor.commit();
            easy.setEnabled(false);
            regular.setEnabled(true);
            hardcore.setEnabled(true);
        });

        regular.setOnClickListener(view ->{
            editor.putLong("difficulty", 500);
            editor.commit();
            easy.setEnabled(true);
            regular.setEnabled(false);
            hardcore.setEnabled(true);
        });

        hardcore.setOnClickListener(view ->{
            editor.putLong("difficulty", 250);
            editor.commit();
            easy.setEnabled(true);
            regular.setEnabled(true);
            hardcore.setEnabled(false);
        });

        // Select theme
        colorful.setOnClickListener(view ->{
            editor.putString("theme", "colorful");
            editor.commit();
            colorful.setEnabled(false);
            blacknwhite.setEnabled(true);
            retro.setEnabled(true);
            colorful.getRootView().setBackgroundColor(Color.argb(255,255,255,255));
            difTittle.setTextColor(Color.argb(255,50,204,50));
            theTittle.setTextColor(Color.argb(255,50,204,50));
        });

        blacknwhite.setOnClickListener(view ->{
            editor.putString("theme", "blacknwhite");
            editor.commit();
            colorful.setEnabled(true);
            blacknwhite.setEnabled(false);
            retro.setEnabled(true);
            blacknwhite.getRootView().setBackgroundColor(Color.argb(255,0,0,0));
            difTittle.setTextColor(Color.argb(255,255,255,255));
            theTittle.setTextColor(Color.argb(255,255,255,255));
        });

        retro.setOnClickListener(view ->{
            editor.putString("theme", "retro");
            editor.commit();
            colorful.setEnabled(true);
            blacknwhite.setEnabled(true);
            retro.setEnabled(false);
            retro.getRootView().setBackgroundColor(Color.argb(255,156,203,149));
            difTittle.setTextColor(Color.argb(255,0,0,0));
            theTittle.setTextColor(Color.argb(255,0,0,0));
        });

        // Go back to main
        back.setOnClickListener(view ->{
            startActivity(new Intent(OptionsActivity.this, MainActivity.class));
        });
    }
}
