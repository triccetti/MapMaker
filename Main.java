import com.sun.corba.se.impl.orbutil.ObjectWriter;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main extends Application {
    final private static String TITLE = "Map Maker";

    final private static int DEFAULT_HEIGHT = 784;
    final private static int DEFAULT_WIDTH = 1000;

    final private static int TEXT_BOX_WIDTH = 50;
    final private static int TILE_SIZE_DEFAULT = 16;
    final private static int GRID_WIDTH = 20;
    final private static int GRID_HEIGHT = 20;
    private static int PANEL_WIDTH = 40 + Tile.LARGE_TILE_SIZE;

    private static FileChooser.ExtensionFilter mapFilter = new FileChooser.ExtensionFilter("mmap files (*.mmap)", "*.mmap");
    private static FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");

    private static int tileSize;
    private static int gridHeight;
    private static int gridWidth;

    private TextField tileSizeInput;
    private TextField heightInput;
    private TextField widthInput;
    private TileDisplayPane tileDisplayPane;
    private ZoomScrollPane zoomPane;
    //private LayerManager layerManager;
    private static TileGridPane currentTileGridPane;


    private static ListView<String> layerView;
    private static Map<String, TileGridPane> layers;
    private static Button addLayer;

    private static File currentFile;
    private Stage stage;

    public static void main(String[] args) {
        layers = new HashMap<>();
        layerView = new ListView<>();
        addLayer = new Button("Add Layer");

        tileSize = TILE_SIZE_DEFAULT;
        gridHeight = GRID_HEIGHT;
        gridWidth = GRID_WIDTH;
        currentFile = null;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        tileSizeInput = new TextField();
        heightInput = new TextField();
        widthInput = new TextField();
        stage = primaryStage;
        stage.setTitle(TITLE);
        BorderPane root = new BorderPane();

        tileDisplayPane = new TileDisplayPane();
        BorderPane gridPane = new BorderPane();

        TileGridPane tileGridPane = new TileGridPane(gridHeight, gridWidth);

        currentTileGridPane = tileGridPane;

        layers.put("layer 1", tileGridPane);

        // Zoom grid controller.
        zoomPane = new ZoomScrollPane(currentTileGridPane);
        gridPane.setTop(setupButtons());
        gridPane.setCenter(zoomPane);

        BorderPane layerHolder = new BorderPane(); // = new LayerManager(tileGridPane, gridHeight, gridWidth);

        FlowPane buttonHolder = new FlowPane();
        buttonHolder.getChildren().addAll(addLayer);
        buttonHolder.setHgap(5);
        buttonHolder.setVgap(5);
        buttonHolder.setPadding(new Insets(5, 5, 15, 5));

        addLayer.setGraphic(new ImageView(new Image("images/add.png")));
        addLayer.setOnAction(e -> {
            layers.put("layer " +(layers.size() + 1), new TileGridPane(gridHeight, gridWidth));
            updateLayerView();
        });

        layerView.setOnMouseClicked(e -> {
            System.out.println("clicked on " + layerView.getSelectionModel().getSelectedItem());
            currentTileGridPane = layers.get(layerView.getSelectionModel().getSelectedItem());
            currentTileGridPane.drawGrid();
            zoomPane = new ZoomScrollPane(currentTileGridPane);
        });

        updateLayerView();
        layerHolder.setTop(buttonHolder);
        layerHolder.setCenter(layerView);
        layerHolder.setMaxWidth(PANEL_WIDTH);
        layerHolder.setMinHeight(PANEL_WIDTH);
        layerHolder.setPrefSize(PANEL_WIDTH, PANEL_WIDTH * 2);
        layerView.setPrefSize(PANEL_WIDTH, layerHolder.getHeight());
        layerHolder.setStyle("-fx-background-color: #eaeaea;" +
                "-fx-border-color: #000000;" +
                "-fx-border-width: 2px;");


        root.setTop(setupMenu());
        root.setCenter(gridPane);
        root.setRight(tileDisplayPane);
        root.setLeft(layerHolder);
        root.setPadding(new Insets(2));

        stage.setScene(new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT));

        stage.setMinHeight(DEFAULT_HEIGHT / 2);
        stage.setMinWidth(DEFAULT_WIDTH / 2);
        stage.sizeToScene();
        stage.show();
    }

    private Node setupMenu() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(mapFilter, pngFilter);
        final Menu file = new Menu("File");

        MenuItem newMenu = new MenuItem("New");
        newMenu.setOnAction((ActionEvent e) -> {
            System.out.println("New file created.");
            currentTileGridPane.clearGrid();
            tileDisplayPane.clearTiles();
            currentFile = null;
            stage.setTitle(TITLE + " [new file]");
        });

        MenuItem open = new MenuItem("Open");
        open.setAccelerator( new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN, KeyCombination.CONTROL_DOWN));
        open.setOnAction((ActionEvent e) -> {
            fileChooser.setSelectedExtensionFilter(mapFilter);
            File openedFile = fileChooser.showOpenDialog(null);
            if(openedFile != null) {
                // YAY
                stage.setTitle(TITLE + " [" + openedFile.getName() + "]");
                currentFile = openedFile;
                loadState(currentFile);
            }
        });
        MenuItem saveAs = new MenuItem("Save As");
        saveAs.setAccelerator( new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN, KeyCombination.CONTROL_DOWN));
        saveAs.setOnAction((ActionEvent e) -> {
            fileChooser.setSelectedExtensionFilter(mapFilter);
            File saveFile = fileChooser.showSaveDialog(null);
            saveState(saveFile);
            currentFile = saveFile;
            stage.setTitle(TITLE + " [" + currentFile.getName() + "]");
        });

        MenuItem save = new MenuItem("Save");
        save.setAccelerator( new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        save.setOnAction((ActionEvent e) -> {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            System.out.println(currentFile);
            if(currentFile == null) {
                fileChooser.setSelectedExtensionFilter(mapFilter);
                File saveFile = fileChooser.showSaveDialog(null);
                if(saveFile!= null) {
                    saveState(saveFile);
                    currentFile = saveFile;
                }
            } else {
                saveState(currentFile);
            }
            stage.setTitle(TITLE + " [" + currentFile.getName() + "] last save: " + dtf.format(now));
        });

        MenuItem export = new MenuItem("Export as image");
        export.setOnAction((ActionEvent e) -> {
            fileChooser.setSelectedExtensionFilter(pngFilter);
            File exportFile = fileChooser.showSaveDialog(null);
            currentFile = exportFile;
            exportState(currentFile);

        });
        file.getItems().addAll(newMenu, open, save, saveAs, export);

        final Menu menu2 = new Menu("Options");
        final Menu menu3 = new Menu("Help");

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(file, menu2, menu3);
        return menuBar;
    }

    private void loadState(File loadFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(loadFile))) {

            String line = reader.readLine();
            String[] firstLine = line.split("\\s");
            tileSizeInput.setText(firstLine[0]);
            heightInput.setText(firstLine[1]);
            widthInput.setText(firstLine[2]);
            currentTileGridPane.updateSize(Integer.parseInt(heightInput.getText()), Integer.parseInt(widthInput.getText()));

            while((line = reader.readLine()) != null && !line.isEmpty()) {
                String filePath = line.split("\\s")[2];
                filePath = filePath.substring(6);
                System.out.println(filePath);
                tileDisplayPane.addTile(new File(filePath));
            }

            currentTileGridPane.drawGrid();

            int height = Integer.parseInt(firstLine[1]);
            int width = Integer.parseInt(firstLine[2]);
            int row = 0;
            int col = 0;
            ArrayList<Tile> tileSet = tileDisplayPane.getTileSet();
            while((line = reader.readLine()) != null) {
                String[] tiles = (line.replaceAll("\\s","")).split(",");

                for(int i = 0; i < tiles.length; i++) {
                    int index = Integer.parseInt(tiles[i].trim());
                    if(index >= 0) {
                        //tileGridPane.add(tileSet.get(index), i, height);
                        System.out.println(index);
                        System.out.println(tiles[i]);
                        currentTileGridPane.tileAtPos(row, i).updateTile(tileSet.get(index));
                    }
                }
                row++;
            }
            System.out.println("end of file");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveState(File saveToFile) {
        PrintWriter fw = null;

        try {
            fw = new PrintWriter(saveToFile.getPath());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(tileSizeInput.getText() + "\t" + heightInput.getText() + "\t" + widthInput.getText());
            bw.newLine();
            ArrayList<Tile> tileSet = tileDisplayPane.getTileSet();
            /* Save tileset */
            for (Tile tile: tileSet) {

                bw.append(tileSet.indexOf(tile) + "\t" + tile.toString());
                bw.newLine();
            }
            bw.newLine();

            /* Save grid */
            int width = currentTileGridPane.getGridWidth();
            int height = currentTileGridPane.getGridHeight();

            for(int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    Tile tile = currentTileGridPane.tileAtPos(i, j);
                    bw.append(tileSet.indexOf(tile) + ", ");

                }
                bw.newLine();
            }
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportState(File exportToFile) {
        try {
            //Pad the capture area
            WritableImage writableImage = currentTileGridPane.snapshot(gridHeight * tileSize, gridWidth * tileSize);
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
            //Write the snapshot to the chosen file
            ImageIO.write(renderedImage, "png", exportToFile);
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    private Node setupButtons() {

        Button applyChanges = new Button("Apply");
        Button clearGridChanges = new Button("Clear");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Change");
        alert.setHeaderText("Applying these changes will erase all grid data.");
        alert.setContentText("Do you wish to continue?");
        applyChanges.setOnAction((event) -> {
            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK){
                currentTileGridPane.updateSize(Integer.parseInt(heightInput.getText()), Integer.parseInt(widthInput.getText()));
                currentTileGridPane.drawGrid();
            }
        });

        clearGridChanges.setOnAction((event) -> {
            currentTileGridPane.clearGrid();
        });

        tileSizeInput.setPrefWidth(TEXT_BOX_WIDTH);
        heightInput.setPrefWidth(TEXT_BOX_WIDTH);
        widthInput.setPrefWidth(TEXT_BOX_WIDTH);

        tileSizeInput.setText("" + tileSize);
        heightInput.setText("" + gridHeight);
        widthInput.setText("" + gridWidth);

        FlowPane buttonHolder = new FlowPane();
        Label tileSizeLabel = new Label("Tile Size:");
        Label gridHeightLabel = new Label("Grid Height:");
        Label gridWidthLabel = new Label("Grid width:");

        buttonHolder.getChildren().addAll(tileSizeLabel, tileSizeInput, gridHeightLabel,
                heightInput, gridWidthLabel, widthInput, applyChanges, clearGridChanges);
        buttonHolder.setHgap(5);
        buttonHolder.setVgap(5);
        buttonHolder.setPadding(new Insets(5));
        buttonHolder.setAlignment(Pos.CENTER);
        return buttonHolder;
    }

    private void updateLayerView() {
        ObservableList<String> titles = FXCollections.observableArrayList(layers.keySet());
        layerView.setItems(titles);
    }
}

