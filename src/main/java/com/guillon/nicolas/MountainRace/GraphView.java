package com.guillon.nicolas.MountainRace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Custom class which will draw the graph
 */
public class GraphView extends View {

    /**
     * Integer array which contains altitude values
     */
    int graphArrayAltitude[] = null;
    /**
     * Max and min altitude values
     */
    int maxY, minY;
    /**
     * Integer array which contains speed values
     */
    int graphArraySpeed[] = null;
    /**
     * Max and min speed values
     */
    int maxY2, minY2;
    /**
     * Painter to draw on the graph
     */
    Paint paint;

    /**
     * Margin value used in the graph
     */
    int margin = 5;

    /**
     * Constructor that takes in a context
     * @param context
     */
    public GraphView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor that takes in a context and a list of attributes that were set in XML
     * @param context
     * @param attrs
     */
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor that takes a context, attributes set and also a default style
     * @param context
     * @param attrs
     * @param defStyle
     */
    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Actions occurred during the construction
     */
    private void init() {
        // Initializes the paint
        paint = new Paint();
        paint.setTextSize(50);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(5);
    }

    /**
     * Initializes the class values with the data retrieved
     * @param ngraphArray
     * @param maxAltitude
     * @param minAltitude
     * @param ngraphArray2
     * @param maxSpeed
     * @param minSpeed
     */
    public void setGraphArray(int ngraphArray[], int maxAltitude, int minAltitude,
                              int ngraphArray2[], int maxSpeed, int minSpeed)
    {
        maxY = maxAltitude;
        minY = minAltitude;
        graphArrayAltitude = ngraphArray;

        maxY2 = maxSpeed;
        minY2 = minSpeed;
        graphArraySpeed = ngraphArray2;
    }

    /**
     * Method which draws the altitude graph
     * @param canvas
     */
    public void drawAltitudeLines(Canvas canvas) {
        // Sets the paint color and initializes some values
        paint.setColor(getResources().getColor(R.color.altitude));
        int maxX = graphArrayAltitude.length;
        int height = maxY - minY;

        if (height == 0) {
            // We draw a line in the middle if the value doesn't change at all
            canvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2, paint);
        }
        else {
            // Else we calculate the width and height factor and draw the lines
            float factorX = getWidth() / ((float)maxX-1);
            float factorY = getHeight() / (float)height;

            for(int i = 1; i < graphArrayAltitude.length; ++i) {
                int x0 = i - 1;
                int y0 = graphArrayAltitude[i-1];
                int x1 = i;
                int y1 = graphArrayAltitude[i];

                int sx = (int)(x0 * factorX);
                int sy = getHeight() - (int)((y0 - minY)* factorY);
                int ex = (int)(x1*factorX);
                int ey = getHeight() - (int)((y1 - minY)* factorY);

                // If the values are near the bottom/top of the graph we but a margin
                if (y0 - minY == 0)
                    sy -= margin;
                else if (y0 - minY == height)
                    sy += margin;

                if (y1 - minY == 0)
                    ey -= margin;
                else if (y1 - minY == height)
                    ey += margin;
                canvas.drawLine(sx, sy, ex, ey, paint);
            }
        }
    }

    /**
     * Method which draws the speed graph
     * @param canvas
     */
    public void drawSpeedLines(Canvas canvas) {
        // Sets the paint color and initializes some values
        paint.setColor(getResources().getColor(R.color.speed));
        int maxX2 = graphArraySpeed.length;
        int height = maxY2 - minY2;

        if (height == 0) {
            // We draw a line in the middle if the value doesn't change at all
            canvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2, paint);
        }
        else {
            // Else we calculate the width and height factor and draw the lines
            float factorX = getWidth() / ((float)maxX2-1);
            float factorY = getHeight() / (float)height;

            for(int i = 1; i < graphArraySpeed.length; ++i) {
                int x0 = i - 1;
                int y0 = graphArraySpeed[i-1];
                int x1 = i;
                int y1 = graphArraySpeed[i];

                int sx = (int)(x0 * factorX);
                int sy = getHeight() - (int)((y0 - minY2)* factorY);
                int ex = (int)(x1*factorX);
                int ey = getHeight() - (int)((y1 - minY2)* factorY);

                // If the values are near the bottom/top of the graph we but a margin
                if (y0 - minY2 == 0)
                    sy -= margin;
                else if (y0 - minY2 == height)
                    sy += margin;

                if (y1 - minY2 == 0)
                    ey -= margin;
                else if (y1 - minY2 == height)
                    ey += margin;
                canvas.drawLine(sx, sy, ex, ey, paint);
            }
        }
    }

    /**
     * Method overridden to draw the contents of the widget
     * Simply draws the legend and the graph
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // If the arrays haven't been filled correctly we do nothing
        if(graphArrayAltitude == null || graphArraySpeed == null)
        {
            return;
        }

        // Draws the two graphs
        drawAltitudeLines(canvas);
        drawSpeedLines(canvas);

        // Draws the legend
        paint.setColor(getResources().getColor(R.color.altitude));
        canvas.drawText("" + maxY + "m", 20, 50, paint);
        canvas.drawText("" + minY + "m", 20, getHeight() - 30, paint);
        paint.setColor(getResources().getColor(R.color.speed));
        canvas.drawText("" + maxY2 + "m/s", getWidth() - 170, 50, paint);
        canvas.drawText("" + minY2 + "m/s", getWidth() - 170, getHeight() - 30, paint);
    }
}
