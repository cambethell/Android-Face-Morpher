package comp4932.gogoproj;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.HashMap;

public class MaskView extends View {
    static int selectedLine = -1;
    Paint paint = new Paint();
    ArrayList<Line> lines = new ArrayList<Line>();
    MaskView MV;

    //the maskview constructor
    public MaskView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint.setAntiAlias(true);
        paint.setStrokeWidth(6f);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    //set the maskview and draw lines
    public void setMV(MaskView MV) {
        this.MV = MV;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Line line : lines) {
            canvas.drawLine(line.startPt.x, line.startPt.y, line.endPt.x, line.endPt.y, paint);
            canvas.drawLine(line.endPt.x + 7, line.endPt.y + 7, line.endPt.x - 7, line.endPt.y+7, paint);
            canvas.drawLine(line.endPt.x - 7, line.endPt.y + 7, line.endPt.x - 7, line.endPt.y-7, paint);
            canvas.drawLine(line.endPt.x + 7, line.endPt.y - 7, line.endPt.x - 7, line.endPt.y-7, paint);
            canvas.drawLine(line.endPt.x + 7, line.endPt.y + 7, line.endPt.x+ 7, line.endPt.y-7, paint);

        }

        MV.invalidate();
    }

    //when the maskview sitting on the image is touched:
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < lines.size(); i++) {
                Line line = lines.get(i);

                //if they touch an end of an existing line
                //this also works for dragging
                //will flip a line if you touch the other end
                if (Math.sqrt(Math.pow(line.startPt.x - event.getX(), 2) +
                        Math.pow(line.startPt.y - event.getY(), 2)) < 50) {
                    selectedLine = i;
                    Point temp = line.startPt;
                    line.startPt = line.endPt;
                    line.endPt = temp;
                    line = new Line(line.endPt, line.startPt, false);

                    invalidate();
                    return true;

                } else if (Math.sqrt(Math.pow(line.endPt.x - event.getX(), 2) +
                        Math.pow(line.endPt.y - event.getY(), 2)) < 50) {
                    selectedLine = i;

                    invalidate();
                    return true;
                }
            }

            Line line = new Line(new Point((int) event.getX(), (int) event.getY()),
                    new Point((int) event.getX(), (int) event.getY()),
                    true);

            lines.add(new Line(line.startPt, line.endPt, true));
            MV.lines.add(lines.get(lines.size() - 1));
            if(selectedLine == -1)
                selectedLine = lines.size() - 1;

            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && lines.size() > 0) {
            Line current = lines.get(selectedLine);
            current.endPt = new Point((int) event.getX(), (int) event.getY());


            invalidate();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP && lines.size() > 0) {
            Line current = lines.get(selectedLine);
            current.endPt = new Point((int) event.getX(), (int) event.getY());

            if (current.initialization) {
                current.initialization = false;
                MV.lines.remove(MV.lines.size() - 1);
                MV.lines.add(new Line(current.startPt, current.endPt, false));
            }

            invalidate();
            selectedLine = -1;
            return true;
        } else {
            return false;
        }
    }
}