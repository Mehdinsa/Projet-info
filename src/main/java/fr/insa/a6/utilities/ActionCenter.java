package fr.insa.a6.utilities;

import fr.insa.a6.graphic.Graphics;
import fr.insa.a6.graphic.mainbox.MainCanvas;
import fr.insa.a6.graphic.mainbox.MainScene;
import fr.insa.a6.treillis.Treillis;
import fr.insa.a6.treillis.Type;
import fr.insa.a6.treillis.dessin.Forme;
import fr.insa.a6.treillis.dessin.Point;
import fr.insa.a6.treillis.dessin.Segment;
import fr.insa.a6.treillis.nodes.Noeud;
import fr.insa.a6.treillis.nodes.NoeudSimple;
import fr.insa.a6.treillis.terrain.Terrain;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;


public class ActionCenter {

    private Forme nearest, curentSelect;
    private ArrayList<Forme> multipleSelect = new ArrayList<>();
    private Terrain selectedTerrain;

    private Type barreType = null;

    private double mouseX, mouseY;
    private double dragMouseX, dragMouseY;

    //bouttons de selections : 0 -> select ; 1xx -> noeud; 2xx -> barre;  3xx -> terrain; 4xx -> pointTerrain; 5xx -> segmentTerrain
    //x : 0 -> Noeud Simple; 1 ->
    private int selectedButton = 0;

    private MainScene mainScene;
    private Graphics graphics;
    private Stage stage;
    private Options options = new Options();
    private String name;

    private Treillis treillis;

    private int currentClick = 0;
    private Point firstSegmentPoint = null;

    private double terrainX, terrainY;

    private boolean inMultSelect = false, drag = false;

    private boolean inDrawing = true;

    public ActionCenter() {

        graphics = new Graphics();

    }

    //initialisation de la classe
    // impossible de le faire dans son constructeur car cette classe a besoin de "mainscene" qui a besoin de
    // cet "action center" pour etre initialisé
    public void init(MainScene mainScene, Treillis treillis, Stage stage, String name){
        this.mainScene = mainScene;
        this.treillis = treillis;
        this.stage = stage;
        this.name = name;

        GraphicsContext gc = mainScene.getCanvas().getGraphicsContext();
        graphics.init(mainScene, gc, this);
        graphics.updateFormes(treillis);
        graphics.redraw(selectedButton, inDrawing);

        addMouseEvent();
    }

    public void reload(String name) throws IOException, ParseException {


        mainScene = new MainScene((int) options.getWidth(), (int) options.getHeight(), this);
        Scene scene = new Scene(mainScene, options.getWidth(), options.getHeight());

        graphics.setMainScene(mainScene);

        if(options.getTheme().equals("light")){
            scene.getStylesheets().add("stylesSheet/lightTheme/lightStyle.css");
        }else{
            scene.getStylesheets().add("stylesSheet/darkTheme/darkStyle.css");
        }

        stage.setScene(scene);
        stage.show();
        if(name.equals("")) name = "~Nouveau~";
        stage.setTitle(name);
        graphics.updateFormes(treillis);
        graphics.redraw(selectedButton, inDrawing);

        addMouseEvent();
    }

    public void load(String name) throws IOException, ParseException {
        treillis = Sauvegarde.getTreillis(options.getSavePath() + name + ".json");
        this.name = name;

        graphics.resetFormes();
        reload(name);
    }

    public void newTreillis() throws IOException, ParseException {
        treillis = new Treillis();
        this.name = "";

        graphics.resetFormes();
        reload("");
    }


    public void saveAct(String path)
    {
        Sauvegarde.saveTreillis(treillis, path);
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
            graphics.redraw(selectedButton, inDrawing);
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
                case 3 -> addTerrain();
                case 4 -> addPointTrn();
                case 5 -> System.out.println("marche plus :/");
            }
        });

        canvas.setOnMouseReleased(mouseEvent -> {
            dragMouseX = -1;
            dragMouseY = -1;
            drag = false;
            graphics.redraw(selectedButton, inDrawing);
        });

        canvas.setOnMouseDragged(mouseEvent -> {
            if (selectedButton == 0){
                drag = true;
                inMultSelect = true;
                dragMouseX = mouseEvent.getX();
                dragMouseY = mouseEvent.getY();
                dragSelection();
                graphics.redraw(selectedButton, inDrawing);
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
        graphics.redraw(selectedButton, inDrawing);
        return ns;
    }

    //fonction permettant la selection d'un point
    private void setSelected(){
        Terrain tempTerrain = selectedTerrain;
        if(selectedTerrain != null){
            selectedTerrain.setSelected(false);
        }
        selectedTerrain = null;
        if (curentSelect != null) {
            curentSelect.setSelected(false);
        }
        if (nearest != null) {
            if(nearest.equals(curentSelect)){
                nearest.setSelected(false);
                curentSelect = null;
                graphics.removeInfos();
            }else {
                nearest.setSelected(true);
                curentSelect = nearest;
                graphics.drawInfos(nearest);
            }
        }else if(treillis.getTerrain() != null){
            curentSelect = null;

            if(selectedTerrain != null){
                selectedTerrain.setSelected(false);
                graphics.removeInfos();
                selectedTerrain = null;
            }else if(treillis.getTerrain().contain(mouseX, mouseY)){
                selectedTerrain.setSelected(true);
                graphics.drawInfos(selectedTerrain);

            }
        }
    }

    //fonction de création d'une barre
    private void addBarre(){
        currentClick++;
        Noeud p;
        //test si on clique a coté d'un point ou pas
        //Besoin d'ajouter la vérification que le point est créable, et quel type de point
        if(nearest != null && nearest instanceof Noeud){
            p = (Noeud) nearest;
        }else{
            p = addNoeudSimple();
        }
        if(currentClick == 1){
            p.setSegmentSelected(true);
            firstSegmentPoint = p;
        }else{
            currentClick = 0;
            treillis.createBarre((Noeud) firstSegmentPoint, p, barreType);
            firstSegmentPoint.setSegmentSelected(false);
            graphics.updateFormes(treillis);
        }
        graphics.redraw(selectedButton, inDrawing);
    }

    private void addTerrain() {
        currentClick ++;

        if(currentClick == 1){
            terrainX = mouseX;
            terrainY = mouseY;
        }else{
            currentClick = 0;
            treillis.createTerrain(Math.min(terrainX, mouseX), Math.min(terrainY, mouseY), Math.max(terrainX, mouseX), Math.max(terrainY, mouseY));
        }
    }

    public void addPointTrn() {
        Terrain terrain = treillis.getTerrain();
        if(terrain.contain(mouseX, mouseY)){
            terrain.addPoint(mouseX, mouseY);
        }
    }

    public void setSelectedButton(int selectedButton) {
        this.selectedButton = selectedButton;
    }

    public void setInDrawing(boolean inDrawing){
        this.inDrawing = inDrawing;
    }

    //retire le point selectionné
    public void removeSelected() {
        if (curentSelect != null) {
            curentSelect.setSelected(false);
        }
        if(selectedTerrain != null) {
            selectedTerrain.setSelected(false);
        }
        mainScene.getInfos().removeInfos();
        curentSelect = null;
    }

    //retire tout les points selectionnés
    public void removeSelectedAll() {
        multipleSelect.forEach(p -> p.setSelected(false));
        multipleSelect.clear();
        graphics.removeInfos();
    }

    //supprime un element
    public void deleteForme(Forme f){
        if(f != null){
            graphics.remove(f);
            treillis.removeElement(f);
            curentSelect = null;
        }
    }

    //supprime les élements selectionés
    public void deleteAllFormes() {
        multipleSelect.forEach(f -> {
            graphics.remove(f);
            treillis.removeElement(f);
        });

        multipleSelect.clear();
        graphics.removeInfos();
        inMultSelect = false;
        graphics.redraw(selectedButton, inDrawing);
    }

    public void deleteTerrain(Terrain t){
        if(t != null){
            treillis.setTerrain(null);
            selectedTerrain = null;
        }
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
            if(bestDist == 15) {
                nearest = null;
            }
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

    public String getName() {
        return name;
    }

    public double getTerrainX() {
        return terrainX;
    }

    public double getTerrainY() {
        return terrainY;
    }

    public int getCurrentClick() {
        return currentClick;
    }

    public Treillis getTreillis() {
        return treillis;
    }

    public void setBarreType(Type barreType) {
        this.barreType = barreType;
    }

    public boolean isInDrawing() {
        return inDrawing;
    }

    public boolean isInMultSelect() {
        return inMultSelect;
    }

    public ArrayList<Forme> getMultipleSelect() {
        return multipleSelect;
    }
}
