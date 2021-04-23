package fr.insa.a6.graphic.mainbox;

import fr.insa.a6.graphic.Graphics;
import fr.insa.a6.treillis.dessin.Forme;
import fr.insa.a6.treillis.dessin.Point;
import fr.insa.a6.utilities.ActionCenter;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;


public class MainCanvas extends Pane {

    private GraphicsContext gc;

    private MainScene mainScene;
    private Canvas canvas;

    private ActionCenter actionCenter;
    private Graphics graphics;

    public MainCanvas(double width, double height, MainScene mainScene) {
        super();
        this.setPrefSize(width, height);

        this.canvas = new Canvas();
        this.mainScene = mainScene;
        this.actionCenter = mainScene.getActionCenter();
        this.graphics = actionCenter.getGraphics();

        gc = canvas.getGraphicsContext2D();

        canvas.setManaged(false);
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());
        this.getChildren().add(canvas);

        this.canvas.heightProperty().addListener((o) -> graphics.redraw(actionCenter.getSelectedButton()));
        this.canvas.widthProperty().addListener((o) -> graphics.redraw(actionCenter.getSelectedButton()));

    }


    public GraphicsContext getGraphicsContext() {
        return gc;
    }

}