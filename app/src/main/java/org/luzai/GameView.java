package org.luzai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends View implements SensorEventListener{
    //width and height of the whole maze and width of lines which
    //make the walls
    private int width, height, lineWidth;
    //size of the maze i.e. number of cells in it
    private int mazeSizeX, mazeSizeY;
    //width and height of cells in the maze
    float cellWidth, cellHeight;
    //the following store result of cellWidth+lineWidth
    //and cellHeight+lineWidth respectively
    float totalCellWidth, totalCellHeight;
    //the finishing point of the maze
    private int mazeFinishX, mazeFinishY;
    private org.luzai.Maze maze;
    private Activity context;
    private Paint line, red, background;

    public GameView(Context context, org.luzai.Maze maze) {
        super(context);
        this.context = (Activity) context;
        this.maze = maze;
        mazeFinishX = maze.getFinalX();
        mazeFinishY = maze.getFinalY();
        mazeSizeX = maze.getMazeWidth();
        mazeSizeY = maze.getMazeHeight();
        line = new Paint();
        line.setAntiAlias(true);
        line.setStrokeWidth(3);
        line.setColor(getResources().getColor(R.color.line));

        red = new Paint();
        red.setColor(getResources().getColor(R.color.position));
        background = new Paint();
        background.setColor(getResources().getColor(R.color.game_bg));
        setFocusable(true);
        this.setFocusableInTouchMode(true);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = (w < h) ? w : h;
        //height = width;         //for now square mazes
        height = (w < h) ? h : w;
        lineWidth = 1;          //for now 1 pixel wide walls
        cellWidth = (width - ((float) mazeSizeX * lineWidth)) / mazeSizeX;
        totalCellWidth = cellWidth + lineWidth;
        cellHeight = (height - ((float) mazeSizeY * lineWidth)) / mazeSizeY;
        totalCellHeight = cellHeight + lineWidth;
        red.setTextSize(cellHeight * 0.25f);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected void onDraw(Canvas canvas) {
        //fill in the background
        canvas.drawRect(0, 0, width, height, background);

        boolean[][] hLines = maze.getHorizontalLines();
        boolean[][] vLines = maze.getVerticalLines();
        //iterate over the boolean arrays to draw walls
        for (int i = 0; i < mazeSizeX; i++) {
            for (int j = 0; j < mazeSizeY; j++) {
                float x = j * totalCellWidth;
                float y = i * totalCellHeight;
                if (j < mazeSizeX - 1 && vLines[i][j]) {
                    //we'll draw a vertical line
                    canvas.drawLine(x + cellWidth,   //start X
                            y,               //start Y
                            x + cellWidth,   //stop X
                            y + cellHeight,  //stop Y
                            line);
                }
                if (i < mazeSizeY - 1 && hLines[i][j]) {
                    //we'll draw a horizontal line
                    canvas.drawLine(x,               //startX
                            y + cellHeight,  //startY
                            x + cellWidth,   //stopX
                            y + cellHeight,  //stopY
                            line);
                }
            }
        }
        int currentX = maze.getCurrentX(), currentY = maze.getCurrentY();
        //draw the ball
        canvas.drawCircle((currentX * totalCellWidth) + (cellWidth / 2),   //x of center
                (currentY * totalCellHeight) + (cellWidth / 2),  //y of center
                (cellWidth * 0.25f),                           //radius
                red);
        //draw the finishing point indicator
        canvas.drawText("GO!",
                (mazeFinishX * totalCellWidth) + (cellWidth * 0.25f),
                (mazeFinishY * totalCellHeight) + (cellHeight * 0.75f),
                red);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean moved = false;
        int eventX = Math.round(event.getX() / cellWidth);
        int eventY = Math.round(event.getY() / cellHeight);
        int currentX = maze.getCurrentX();
        int currentY = maze.getCurrentY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (currentY < eventY)
                    moved = maze.move(org.luzai.Maze.DOWN);
                else if (currentY > eventY)
                    moved = maze.move(org.luzai.Maze.UP);
                if (currentX < eventX)
                    moved = maze.move(org.luzai.Maze.RIGHT);
                else if (currentX > eventX)
                    moved = maze.move(org.luzai.Maze.LEFT);
            }
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            default:
                break;
        }

        if (moved) {
            //the ball was moved so we'll redraw the view
            invalidate();
            if (maze.isGameComplete()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getText(R.string.finished_title));
                LayoutInflater inflater = context.getLayoutInflater();
                View view = inflater.inflate(R.layout.finish, null);
                builder.setView(view);
                View closeButton = view.findViewById(R.id.closeGame);
                closeButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View clicked) {
                        if (clicked.getId() == R.id.closeGame) {
                            context.finish();
                        }
                    }
                });
                AlertDialog finishDialog = builder.create();
                finishDialog.show();
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent evt) {
        boolean moved = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                moved = maze.move(org.luzai.Maze.UP);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                moved = maze.move(org.luzai.Maze.DOWN);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                moved = maze.move(org.luzai.Maze.RIGHT);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                moved = maze.move(org.luzai.Maze.LEFT);
                break;
            default:
                return super.onKeyDown(keyCode, evt);
        }
        if (moved) {
            //the ball was moved so we'll redraw the view
            invalidate();
            if (maze.isGameComplete()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getText(R.string.finished_title));
                LayoutInflater inflater = context.getLayoutInflater();
                View view = inflater.inflate(R.layout.finish, null);
                builder.setView(view);
                View closeButton = view.findViewById(R.id.closeGame);
                closeButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View clicked) {
                        if (clicked.getId() == R.id.closeGame) {
                            context.finish();
                        }
                    }
                });
                AlertDialog finishDialog = builder.create();
                finishDialog.show();
            }
        }
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
