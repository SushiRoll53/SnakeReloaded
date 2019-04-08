package cs4330.cs.utep.snakereloaded;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    Button reset;

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
        reset = findViewById(R.id.reset);

        play.setOnClickListener(view ->{
            Intent startGame = new Intent(MainActivity.this, SnakeGame.class);
            startActivity(startGame);
        });

        options.setOnClickListener(view ->{
            Intent options = new Intent(MainActivity.this, OptionsActivity.class);
            startActivity(options);
        });
        reset.setOnClickListener(view ->{
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("ATTENTION")
                    .setMessage("Are you sure you want to set all high scores to zero?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences highScores = getSharedPreferences("highScores", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = highScores.edit();
                            editor.putInt("easy", 0);
                            editor.putInt("regular", 0);
                            editor.putInt("hardcore", 0);
                            editor.commit();
                        }
                    })
                    .show();
        });
    }
}
