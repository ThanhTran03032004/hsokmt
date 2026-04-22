package Game.template;

import Game.core.Util;

import java.awt.geom.Point2D;

public class Point {
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Point getPoint(int centerX, int centerY, int rectangleWidth) {
        int offsetX = Util.nextInt(rectangleWidth + 1) - rectangleWidth / 2;
        int offsetY = Util.nextInt(rectangleWidth + 1) - rectangleWidth / 2;

        int x2 = centerX + offsetX;
        int y2 = centerY + offsetY;

        return new Point(x2, y2);
    }
    public static double Distance(double x1, double y1, double x2, double y2) {
        return Point2D.distance(x1, y1, x2, y2);
    }
}
