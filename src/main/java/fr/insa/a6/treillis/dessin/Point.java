package fr.insa.a6.treillis.dessin;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 
 */
public class Point extends Forme{

    protected double posX;
    protected double posY;
    protected boolean segmentSelected;

    /**
     * Default constructor
     */
    public Point() {
        this(0,0, -1);
    }

    public Point(double x, double y) {
        this(x, y,-1);
    }

    public Point(double x, double y, int id) {
        posY = y;
        posX = x;
        this.id = id;

    }

    public void draw(GraphicsContext gc, Point origin){
        if(selected){
            gc.setFill(Color.RED);
            gc.fillOval(posX - 5 + origin.posX, posY - 5 + origin.posY, 11, 11);
        }else if(segmentSelected){
            gc.setStroke(Color.DARKBLUE);
            gc.setLineWidth(2);
            gc.strokeOval(posX - 5 + origin.posX, posY - 5 + origin.posY, 11, 11);
        }else{
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(2);
            gc.strokeOval(posX - 5 + origin.posX, posY - 5 + origin.posY, 11, 11);
        }
    }

    public void drawNear(GraphicsContext gc, Point origin) {
        gc.setFill(Color.BLUE);
        gc.fillOval(posX - 5 + origin.posX, posY - 5 + origin.posY,11, 11);
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public int getId() {
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public ArrayList<String> getInfos(){
        String[] str = new String[]{"posX : " + posX ,
                "posY : " + posY,
                "selected : " + selected
        };
        return new ArrayList<>(Arrays.asList(str));
    }

    public void setSegmentSelected(boolean segmentSelected) {
        this.segmentSelected = segmentSelected;
    }
}