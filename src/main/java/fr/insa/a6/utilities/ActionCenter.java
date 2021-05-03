package fr.insa.a6.utilities;

import fr.insa.a6.graphic.Graphics;
import fr.insa.a6.graphic.mainbox.MainCanvas;
import fr.insa.a6.graphic.mainbox.MainScene;
import fr.insa.a6.treillis.Treillis;
import fr.insa.a6.treillis.dessin.Forme;
import fr.insa.a6.treillis.dessin.Point;
import fr.insa.a6.treillis.dessin.Segment;
import fr.insa.a6.treillis.nodes.Noeud;
import fr.insa.a6.treillis.nodes.NoeudSimple;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class ActionCenter {

    private Forme nearest, curentSelect;
    private ArrayList<Forme> multipleSelect = new ArrayList<>();

    private double mouseX, mouseY;
    private double dragMouseX, dragMouseY;

    //bouttons de selections : 0 -> select ; 1x -> noeud; 2 -> barre; 3 -> pointTerrain; 4 -> segmentTerrain
    //x : 0 -> Noeud Simple; 1 ->
    private int selectedButton = 0;

    private MainScene mainScene;
    private Graphics graphics;
    private Scene scene;

    private Treillis treillis;

    private int segmentClick = 0;
    private Point firstSegmentPoint = null;

    private boolean inMultSelect = false, drag = false;

    public ActionCenter() {

        graphics = new Graphics();

    }

    public void init(MainScene mainScene, Treillis treillis, Scene scene){
        this.mainScene = mainScene;
        this.treillis = treillis;
        this.scene = scene;

        GraphicsContext gc = mainScene.getCanvas().getGraphicsContext();
        graphics.init(mainScene, gc, this);
        graphics.redraw(selectedButton);

        addMouseEvent();
    }


    //ajout des fonctions appelés durant différentes actions de la souris
    private void addMouseEvent() {
        MainCanvas canvas = mainScene.getCanvas();

        canvas.setOnMouseMoved(mouseEvent -> {
            mouseX = mouseEvent.getX();
            mouseY = mouseEvent.getY();
            switch (selectedButton/10) {
                case 0, 2, 4 -> selection();
            }
            graphics.redraw(selectedButton);
        });

        canvas.setOnMousePressed(mouseEvent -> {
            //treillis.addNoeud/barre()
            mouseX = mouseEvent.getX();
            mouseY = mouseEvent.getY();
            inMultSelect = false;
            removeSelectedAll();

            switch (selectedButton/10) {
                case 0 -> setSelected();
                case 1 -> addNoeud();
                case 2 -> addBarre();
                case 3, 4 -> System.out.println("marche plus :/");
            }

        });

        canvas.setOnMouseReleased(mouseEvent -> {
            dragMouseX = -1;
            dragMouseY = -1;
            drag = false;
            graphics.redraw(selectedButton);
        });

        canvas.setOnMouseDragged(mouseEvent -> {
            if (selectedButton == 0){
                drag = true;
                inMultSelect = true;
                dragMouseX = mouseEvent.getX();
                dragMouseY = mouseEvent.getY();
                dragSelection();
                graphics.redraw(selectedButton);
            }
        });
    }

    private void addNoeud(){
        switch (selectedButton){
            case 10 -> addNoeudSimple();
            case 11 -> System.out.println("11");
            default -> System.out.println(selectedButton);
        }
    }

    private NoeudSimple addNoeudSimple() {
        return addNoeudSimple(mouseX, mouseY);
    }

    private NoeudSimple addNoeudSimple(double posX, double posY) {
        NoeudSimple ns = treillis.createNoeudSimple(posX, posY);
        graphics.updateFormes(treillis);
        graphics.redraw(selectedButton);
        return ns;
    }

    //fonction permettant la selection d'un point
    private void setSelected(){
        System.out.println(selectedButton);
        if (nearest != null) {
            if (curentSelect != null) {
                curentSelect.setSelected(false);
            }
            if(nearest.equals(curentSelect)){
                nearest.setSelected(false);
                curentSelect = null;
                graphics.removeInfos();
            }else {
                nearest.setSelected(true);
                curentSelect = nearest;
                graphics.drawInfos(nearest);
            }
        }
    }

    private void addBarre(){
        segmentClick ++;
        Noeud p;
        //test si on clique a coté d'un point ou pas
        //Besoin d'ajouter la vérification que le point est créable, et quel type de point
        if(nearest != null && nearest instanceof Noeud){
            p = (Noeud) nearest;
        }else{
            p = addNoeudSimple();
        }

        if(segmentClick == 1){
            p.setSegmentSelected(true);
            firstSegmentPoint = p;
        }else{
            segmentClick = 0;
            treillis.createBarre((Noeud) firstSegmentPoint, p);
            firstSegmentPoint.setSegmentSelected(false);
            graphics.updateFormes(treillis);
        }
        graphics.redraw(selectedButton);
    }

    public void setSelectedButton(int selectedButton) {
        this.selectedButton = selectedButton;
    }

    //retire le point selectionner
    public void removeSelected() {
        if (curentSelect != null) {
            curentSelect.setSelected(false);
            mainScene.getInfos().removeInfos();
        }
        curentSelect = null;
    }

    public void removeSelectedAll() {
        multipleSelect.forEach(p -> p.setSelected(false));
        multipleSelect.clear();
        graphics.removeInfos();
    }

    //supprime un element
    public void deleteForme(Forme f){
        if(f != null){
            graphics.remove(f.getId());
            treillis.removeElement(f);
            curentSelect = null;
        }
    }

    public void deleteAllFormes() {
        multipleSelect.forEach(f -> {
            graphics.remove(f.getId());
            treillis.removeElement(f);
        });

        multipleSelect.clear();
        graphics.removeInfos();
        inMultSelect = false;
        graphics.redraw(selectedButton);
    }

    public Graphics getGraphics() {
        return graphics;
    }

    public Forme getNearest(){
        return nearest;
    }

    public void selection(){

        //ajoute les formes dans la selection
        Forme[] formes = graphics.getFormes();
        double bestDist = 15;

        for (Forme f: formes) {
            Point p;
            if(f instanceof Point) {
                p = (Point) f;
            }else{
                p = ((Segment) f).getCenter();
            }

            //trouve la forme le plus proche de la souris
            double dist = Maths.dist(p, new Point(mouseX, mouseY));
            if(dist < bestDist){
                nearest = f;
                bestDist = dist;
            }
            if(bestDist == 15) nearest = null;
        }

    }

    private void dragSelection() {
        Forme[] formes = graphics.getFormes();

        for (Forme f: formes) {
            Point p;
            if(f instanceof Point) {
                p = (Point) f;
            }else{
                p = ((Segment) f).getCenter();
            }

            if (drag) {
                if (p.getPosX() < Math.max(mouseX, dragMouseX) && p.getPosX() > Math.min(mouseX, dragMouseX) &&
                        p.getPosY() < Math.max(mouseY, dragMouseY) && p.getPosY() > Math.min(mouseY, dragMouseY)) {
                    if (!multipleSelect.contains(f)) {
                        multipleSelect.add(f);
                        f.setSelected(true);
                    }
                } else {
                    multipleSelect.remove(f);
                    f.setSelected(false);
                }
            }
        }
    }

    public boolean isDrag() {
        return drag;
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public double getDragMouseX() {
        return dragMouseX;
    }

    public double getDragMouseY() {
        return dragMouseY;
    }

    public int getSelectedButton() {
        return selectedButton;
    }

    public boolean isInMultSelect() {
        return inMultSelect;
    }

    public ArrayList<Forme> getMultipleSelect() {
        return multipleSelect;
    }
}
