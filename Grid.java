
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;


public class Grid extends BorderPane {

    private static final double HALF_PIXEL_OFFSET = 0.5;
    final private double TEXTBOX_WIDTH = 50;
    final public int TILE_SIZE_DEFAULT = 16;

    final public int GRID_WIDTH = 60;
    final public int GRID_HEIGHT = 40;

    private FlowPane buttonHolder;
    private TextField tileSizeInput;
    private TextField heightInput;
    private TextField widthInput;

    private int tileSize;
    private int gridHeight;
    private int gridWidth;

    private double scaleValue = 0.7;
    private double zoomIntensity = 0.02;
    private Node target;
    private Node zoomNode;

    private Canvas canvas;
    private GraphicsContext cnt;

    public Grid(double height, double width) {
        super();
        this.tileSize = TILE_SIZE_DEFAULT;
        this.gridHeight = GRID_HEIGHT;
        this.gridWidth = GRID_WIDTH;
        tileSizeInput = new TextField();
        heightInput = new TextField();
        widthInput = new TextField();
        canvas = new Canvas(width,height);
        cnt = canvas.getGraphicsContext2D();
        init();
    }

    public Grid(double height, double width, int tileSize, int gridHeight, int gridWidth) {
        super();
        this.tileSize = tileSize;
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
        tileSizeInput = new TextField();
        heightInput = new TextField();
        widthInput = new TextField();
        canvas = new Canvas(width, height);
        cnt = canvas.getGraphicsContext2D();
        init();
    }


    private class Delta { double x, y; }

    private void init() {
        tileSizeInput.setPrefWidth(TEXTBOX_WIDTH);
        heightInput.setPrefWidth(TEXTBOX_WIDTH);
        widthInput.setPrefWidth(TEXTBOX_WIDTH);
        tileSizeInput.setText("" + tileSize);
        heightInput.setText("" + gridHeight);
        widthInput.setText("" + gridWidth);


//        Delta initial_mouse_pos = new Delta();
//
//        canvas.setOnScrollStarted(event -> {
//            initial_mouse_pos.x = event.getX();
//            initial_mouse_pos.y = event.getY();
//        });
//
//        canvas.setOnScroll(event -> {
//            double zoom_fac = 1.05;
//            double delta_y = event.getDeltaY();
//
//            if(delta_y < 0) {
//                zoom_fac = 2.0 - zoom_fac;
//            }
//
//            Scale newScale = new Scale();
//            newScale.setPivotX(event.getX());
//            newScale.setPivotY(event.getY());
//            newScale.setX( canvas.getScaleX() * zoom_fac );
//            newScale.setY( canvas.getScaleY() * zoom_fac );
//
//            canvas.getTransforms().add(newScale);
//            drawGrid();
//            event.consume();
//        });
        canvas.setOnMouseClicked((MouseEvent e) -> {
            addTile(e.getX(), e.getY());
            System.out.println(e.getX() + " , " + e.getY());
        });

        attachInputListeners();

        buttonHolder = new FlowPane();
        buttonHolder.getChildren().addAll(new Label("Tile Size:"), tileSizeInput, new Label("Grid Height:"),
                heightInput, new Label("Grid width:"),widthInput);
        buttonHolder.setHgap(5);
        buttonHolder.setVgap(5);
        buttonHolder.setPadding(new Insets(5, 5, 10, 5));
        buttonHolder.setAlignment(Pos.CENTER);
        buttonHolder.setStyle("-fx-background-color: #909090;");
        setTop(buttonHolder);
        setCenter(new ZoomScrollPane(canvas));

        drawGrid();
    }

    private void attachInputListeners() {
        tileSizeInput.setOnKeyTyped((KeyEvent e) -> {
            char ch = e.getCharacter().charAt(0);
            if (!isNumber(ch)) {
                e.consume();
            }
        });

        tileSizeInput.textProperty().addListener((observable, oldValue, newValue) -> {
            tileSize = Integer.parseInt(newValue);
        });

        heightInput.setOnKeyTyped((KeyEvent e) -> {
            char ch = e.getCharacter().charAt(0);
            if (!isNumber(ch)) {
                e.consume();
            }
        });
        heightInput.textProperty().addListener((observable, oldValue, newValue) -> {
            gridHeight = Integer.parseInt(newValue);
        });

        widthInput.setOnKeyTyped((KeyEvent e) -> {
            char ch = e.getCharacter().charAt(0);
            if (!isNumber(ch)) {
                e.consume();
            }
        });
        widthInput.textProperty().addListener((observable, oldValue, newValue) -> {
            gridWidth = Integer.parseInt(newValue);
        });

    }

    private boolean isNumber(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public void setCanvasSize(double height, double width) {
        canvas.setWidth(width - 2);
        canvas.setHeight(height - buttonHolder.getHeight() - 2);
        drawGrid();
    }

    public void drawGrid() {
        double rowPadding = canvas.getWidth() / gridWidth;
        double colPadding = canvas.getHeight() / gridHeight;

        cnt.clearRect(0, 0, getWidth(), getHeight());

        double height = canvas.getHeight();
        double width = canvas.getWidth();

        // vertical lines
        cnt.setFill(Color.BLACK);
        for (int i = 0; i <= gridWidth; i++) {
            cnt.strokeLine(0, snap((i + 1) * rowPadding), width, snap((i + 1) * rowPadding));
        }
        for (int j = 0; j <= gridHeight; j++) {
            cnt.strokeLine(snap((j + 1) * colPadding), 0, snap((j + 1) * colPadding), height);
        }
    }


    private void addTile(double x, double y) {
        double cellHeight = (int) canvas.getHeight() / gridHeight;
        double cellWidth = (int) canvas.getWidth() / gridWidth;



        Tile currentTile = TileSet.getSelected();
        System.out.println(currentTile.isEmptyTile());
        if(!currentTile.isEmptyTile()) {
            cnt.drawImage(currentTile.getImage(), (x / cellWidth) * cellWidth, (y / cellHeight) * cellHeight);
        }
    }


    private static double snap(double y) {
        return ((int) y) + HALF_PIXEL_OFFSET;
    }
}
