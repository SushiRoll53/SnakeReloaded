package cs4330.cs.utep.snakereloaded;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class SnakeGame extends Activity {
    // Declare an instance of snake
    Snake snake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the dimension of the device
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // Initialize the snake
        snake = new Snake(this, size);

        // Display snake
        setContentView(snake);
    }

    @Override
    protected void onResume() {
        super.onResume();
        snake.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snake.pause();
    }
}
