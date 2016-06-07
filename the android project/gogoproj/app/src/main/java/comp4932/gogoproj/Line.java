package comp4932.gogoproj;


import android.graphics.Point;
import android.graphics.PointF;

import java.security.InvalidParameterException;

//simple line class to keep things straight
class Line {
    Point startPt;
    Point endPt;
    boolean initialization;

    public Line(Point start, Point end, boolean ini) {
        this.startPt = start;
        this.endPt = end;
        initialization = ini;

    }

}