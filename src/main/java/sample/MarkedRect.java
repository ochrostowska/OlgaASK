package sample;


public class MarkedRect {
    private int xStart;
    private int xEnd;
    private int yStart;
    private int yEnd;
    private int width;
    private int height;
    public boolean toRemove = false;

    public MarkedRect(int xStart, int yStart, int width, int height) {
        this.xStart = xStart;
        this.yStart = yStart;
        this.width = width;
        this.height = height;
        xEnd = xStart+width;
        yEnd = yStart+height;
    }

    public void setToRemove(boolean toRemove) {
        this.toRemove = toRemove;
    }

    public int getxStart() {
        return xStart;
    }

    public int getxEnd() {
        return xEnd;
    }

    public int getyStart() {
        return yStart;
    }

    public int getyEnd() {
        return yEnd;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
