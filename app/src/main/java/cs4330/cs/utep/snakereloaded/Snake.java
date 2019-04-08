package cs4330.cs.utep.snakereloaded;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Random;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


class Snake extends SurfaceView implements Runnable {
    private Thread thread = null;
    private Context context;

    // Soundpool
    private SoundPool soundPool;
    private int eat_Dot = -1;
    private int snake_crash = -1;
    private int victory = -1;

    // For vibration
    private Vibrator vibrator;

    // Snake moveset and start direction
    public enum moveset {UP, RIGHT, DOWN, LEFT}
    private moveset heading = moveset.RIGHT;

    // Screen attributes
    private int screenX;
    private int screenY;
    private final int NUM_BLOCKS_WIDE = 40;
    private int numBlocksHigh;

    // Snake attributes
    private int snakeLength;
    private int blockSize;
    private int[] snakeXs;
    private int[] snakeYs;

    // Dot position
    private int DotX;
    private int DotY;

    // How fast does it update
    private long difficulty;
    private long nextFrameTime;
    private final long FPS = 10;

    // Score board
    private int score;
    private String newHighScoreDialog;

    // Save high score
    private SharedPreferences highScores;
    private int highScore;

    // settings
    private SharedPreferences settings;
    private String theme;

    // Is snake moving?
    private volatile boolean isPlaying;

    // A canvas to paint
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Paint paint;

    // Colors
    private int[] backgroudColor;
    private int[] snakeColor;
    private int[] highscoreColor;
    private int[] dotColor;

    Handler handler;

    /**
     * Snake constructor, initialize the necessary variables
     * to start a new game.
     * @param context
     * @param size
     */
    public Snake(Context context, Point size) {
        super(context);

        this.context = context;

        screenX = size.x;
        screenY = size.y;

        // To calculate the size of the blocks
        blockSize = screenX / NUM_BLOCKS_WIDE;
        numBlocksHigh = screenY / blockSize;

        // Audio attributes
        AudioAttributes atributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(atributes)
                .build();
        // Preload the sounds before being used
        eat_Dot = soundPool.load(context, R.raw.eat_dot, 1);
        snake_crash = soundPool.load(context, R.raw.neck_snap, 1);
        victory = soundPool.load(context, R.raw.victory, 1);

        // Initialize the drawing tools
        surfaceHolder = getHolder();
        paint = new Paint();

        // Max possible score
        snakeXs = new int[200];
        snakeYs = new int[200];

        // Get the settings
        settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        difficulty = settings.getLong("difficulty", 1000);
        theme = settings.getString("theme", "colorful");

        // Set theme
        backgroudColor = new int[3];
        snakeColor = new int[3];
        highscoreColor = new int[3];
        dotColor = new int[3];
        setTheme();

        // For high scores
        highScores = context.getSharedPreferences("highScores", Context.MODE_PRIVATE);

        // Set high score depending on the difficulty
        if(difficulty == 1000)
            highScore = highScores.getInt("easy", 0);
        else if(difficulty == 500)
            highScore = highScores.getInt("regular", 0);
        else if (difficulty == 250)
            highScore = highScores.getInt("hardcore", 0);

        // Initialize vibrator
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        handler = new Handler();

        // Start the snake
        startSnake();
    }

    /**
     * Overrides run method, to keep
     * redrawing the snake.
     */
    @Override
    public void run() {
        while (isPlaying) {
            if(updateRequired()) {
                update();
                draw();
            }

        }
    }

    // Pause the game
    public void pause() {
        isPlaying = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
    }

    // Resume the game
    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Prepare the snake for a new game
     */
    public void startSnake() {
        // Start the snake attributes
        snakeLength = 2;
        snakeXs[0] = NUM_BLOCKS_WIDE / 2;
        snakeYs[0] = numBlocksHigh / 2;

        // Start dot attributes
        spawnDot();

        // Set high score depending on the difficulty
        if(difficulty == 1000)
            highScore = highScores.getInt("easy", 0);
        else if(difficulty == 500)
            highScore = highScores.getInt("regular", 0);
        else if (difficulty == 250)
            highScore = highScores.getInt("hardcore", 0);
        // Set current score to zero
        score = 0;
        nextFrameTime = System.currentTimeMillis();
    }

    /**
     * Spawn a new dot randomly in the screen
     * whenever the current dot is eaten
     */
    public void spawnDot() {
        Random random = new Random();
        DotX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        DotY = random.nextInt(numBlocksHigh - 1) + 1;
    }

    /**
     * Execute the actions of what happen when a dot is eaten.
     */
    private void eatDot(){
        // Snake size increase
        snakeLength++;
        // New dot is spawn
        spawnDot();
        // Update score
        score = score + 1;
        // Vibrate
        vibrator.vibrate(100);
        // Play eat sound
        soundPool.play(eat_Dot, 1f, 1f, 1, 0, 1);
    }

    /**
     * Base on the current position, updates
     * the location of the snake depending on the
     * direction is heading.
     */
    private void moveSnake(){
        for (int i = snakeLength; i > 0; i--) {
            snakeXs[i] = snakeXs[i - 1];
            snakeYs[i] = snakeYs[i - 1];
        }
        switch (heading) {
            case UP:
                snakeYs[0]--;
                break;

            case RIGHT:
                snakeXs[0]++;
                break;

            case DOWN:
                snakeYs[0]++;
                break;

            case LEFT:
                snakeXs[0]--;
                break;
        }
    }

    /**
     * Return either true or false if the game
     * is over or not
     * @return gameOver
     */
    private boolean isGameOver(){
        boolean gameOver = false;

        // Crash on the ledge
        if (snakeXs[0] == -1) gameOver = true;
        if (snakeXs[0] >= NUM_BLOCKS_WIDE) gameOver = true;
        if (snakeYs[0] == -1) gameOver = true;
        if (snakeYs[0] == numBlocksHigh) gameOver = true;

        // Bite itself
        for (int i = snakeLength - 1; i > 0; i--) {
            if ((i > 4) && (snakeXs[0] == snakeXs[i]) && (snakeYs[0] == snakeYs[i])) {
                gameOver = true;
            }
        }

        return gameOver;
    }

    /**
     * Check the current state and execute the
     * proper action for the game.
     */
    public void update() {
        // Check if the snake eat a dot
        if (snakeXs[0] == DotX && snakeYs[0] == DotY) {
            eatDot();
        }
        moveSnake();
        if (isGameOver()) {
            // To check if there is a new High score
            newHighScoreDialog = "Final score: "+score;
            // Play game over sound
            soundPool.play(snake_crash, 1, 1, 0, 0, 1);
            vibrator.vibrate(500);
            if(score > highScore) {
                soundPool.play(victory, 1,1,0,0,1);
                newHighScoreDialog = "NEW HIGH SCORE: "+score+"!";
                SharedPreferences.Editor editor = highScores.edit();
                if(difficulty == 1000)
                    editor.putInt("easy", score);
                else if(difficulty == 500)
                    editor.putInt("regular", score);
                else if (difficulty == 250)
                    editor.putInt("hardcore", score);

                editor.commit();
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    pause();
                    new AlertDialog.Builder(context)
                            .setTitle("Game over!")
                            .setMessage(newHighScoreDialog+"\nDo you want to keep playing?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    resume();
                                    startSnake();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent quit = new Intent(context, MainActivity.class);
                                    context.startActivity(quit);
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
            });
        }
    }

    /**
     * Draw the gameboard
     */
    public void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            // Set the background color
            canvas.drawColor(Color.argb(255, backgroudColor[0], backgroudColor[1], backgroudColor[2]));

            // Set the color of the snake
            paint.setColor(Color.argb(255, snakeColor[0], snakeColor[1], snakeColor[2]));

            // Draw the scoreboard
            paint.setTextSize(90);
            canvas.drawText("Score: " + score, 10, 70, paint);
            canvas.drawText("||", screenX-70,70, paint);

            // Drawing the snake
            for (int i = 0; i < snakeLength; i++) {
                canvas.drawRect(snakeXs[i] * blockSize,
                        (snakeYs[i] * blockSize),
                        (snakeXs[i] * blockSize) + blockSize,
                        (snakeYs[i] * blockSize) + blockSize,
                        paint);
            }

            paint.setColor(Color.argb(255, highscoreColor[0], highscoreColor[1], highscoreColor[2]));
            canvas.drawText("High Score: "+ highScore, 500, 70, paint);

            // Set the dot color
            paint.setColor(Color.argb(255, dotColor[0], dotColor[1], dotColor[2]));

            // Draw Dot
            canvas.drawRect(DotX * blockSize,
                    (DotY * blockSize),
                    (DotX * blockSize) + blockSize,
                    (DotY * blockSize) + blockSize,
                    paint);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * Check whether a redraw is required
     * also affected by the game difficulty
     * @return
     */
    public boolean updateRequired() {
        if(nextFrameTime <= System.currentTimeMillis()){
            nextFrameTime =System.currentTimeMillis() + difficulty / FPS;
            return true;
        }

        return false;
    }

    /**
     * Base on the selected theme
     * prepare the color to be used.
     */
    public void setTheme(){
        switch (theme){
            case "colorful": // Red | Green | Blue
                backgroudColor[0] = 255; backgroudColor[1] = 255; backgroudColor[2] = 255;
                snakeColor[0] = 0; snakeColor[1] = 204; snakeColor[2] = 0;
                highscoreColor[0] = 0; highscoreColor[1] = 0; highscoreColor[2] = 204;
                dotColor[0] = 255; dotColor[1] = 0; dotColor[2] = 0;
                break;
            case "blacknwhite":
                backgroudColor[0] = 0; backgroudColor[1] = 0; backgroudColor[2] = 0;
                snakeColor[0] = 255; snakeColor[1] = 255; snakeColor[2] = 255;
                highscoreColor[0] = 255; highscoreColor[1] = 255; highscoreColor[2] = 255;
                dotColor[0] = 255; dotColor[1] = 255; dotColor[2] = 255;
                break;
            case "retro":
                backgroudColor[0] = 156; backgroudColor[1] = 203; backgroudColor[2] = 149;
                snakeColor[0] = 21; snakeColor[1] = 46; snakeColor[2] = 17;
                highscoreColor[0] = 0; highscoreColor[1] = 0; highscoreColor[2] = 0;
                dotColor[0] = 0; dotColor[1] = 0; dotColor[2] = 0;
                break;
        }
    }

    /**
     * Override onTouchEvent
     * to determine which movements change the snake direction
     */
    float x1, x2, y1, y2;
    int minDistance = 100;
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                x1 = motionEvent.getX();
                y1 = motionEvent.getY();
                if(motionEvent.getX() > screenX-70 && motionEvent.getY() < 140){
                    this.pause();
                    new AlertDialog.Builder(context)
                            .setTitle("Game pause!")
                            .setMessage("Do you want to keep playing?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    resume();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent quit = new Intent(context, MainActivity.class);
                                    context.startActivity(quit);
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
                break;
            case MotionEvent.ACTION_UP:
                x2 = motionEvent.getX();
                y2 = motionEvent.getY();
                float deltaX = Math.abs(x2 - x1);
                float deltaY = Math.abs(y2 - y1);

                // In case of UP or DOWN swipe
                if(deltaX - deltaY < 0) {
                    if (deltaY >= minDistance) {
                        switch (heading) {
                            case UP:
                                break;
                            case RIGHT:
                                if(y2 - y1 < 0)
                                    heading = moveset.UP;
                                else
                                    heading = moveset.DOWN;
                                break;
                            case DOWN:
                                break;
                            case LEFT:
                                if(y2 - y1 < 0)
                                    heading = moveset.UP;
                                else
                                    heading = moveset.DOWN;
                                break;
                        }
                    }
                }
                // In case of a RIGHT or LEFT swipe
                else{
                    if (deltaX >= minDistance) {
                        switch (heading) {
                            case UP:
                                if(x2 - x1 < 0)
                                    heading = moveset.LEFT;
                                else
                                    heading = moveset.RIGHT;
                                break;
                            case RIGHT:
                                break;
                            case DOWN:
                                if(x2 - x1 < 0)
                                    heading = moveset.LEFT;
                                else
                                    heading = moveset.RIGHT;
                                break;
                            case LEFT:
                                break;
                        }
                    }
                }


        }
        return true;
    }


}