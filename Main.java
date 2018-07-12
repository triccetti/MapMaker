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


    private static ListView<String> layerView;
    private static Map<String, TileGridPane> layers;
    private static Button addLayer;

    private static File currentFile;
    private Stage stage;

    public static void main(String[] args) {
        layers = new LinkedHashMap<>();
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



        layers.put("layer 1", new TileGridPane(gridHeight, gridWidth));
        layerView.getSelectionModel().select("layer 1");

        // Zoom grid controller.
        zoomPane = new ZoomScrollPane(getSelectedLayer());
        gridPane.setTop(setupButtons());
        gridPane.setCenter(zoomPane);

        BorderPane layerHolder = new BorderPane(); // = new LayerManager(tileGridPane, gridHeight, gridWidth);

        FlowPane buttonHolder = new FlowPane();
        buttonHolder.getChildren().addAll(addLayer);
        buttonHolder.setHgap(5);
        buttonHolder.setVgap(5);
        buttonHolder.setPadding(new Insets(5, 5, 15, 5));

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


        addLayer.setGraphic(new ImageView(new Image("images/add.png")));
        addLayer.setOnAction(e -> {
            layers.put("layer " + (layers.size() + 1), new TileGridPane(gridHeight, gridWidth));
            updateLayerView();
        });

        layerView.setOnMouseClicked(e -> {
            System.out.println("Switching to " + layerView.getSelectionModel().getSelectedItem());
            TileGridPane curr = layers.get(layerView.getSelectionModel().getSelectedItem());
            zoomPane.updateNode(curr);

        });

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

        MenuItem exportImage = new MenuItem("Export as image");
        exportImage.setOnAction((ActionEvent e) -> {
            fileChooser.setSelectedExtensionFilter(pngFilter);
            File exportFile = fileChooser.showSaveDialog(null);
            currentFile = exportFile;
            exportState(currentFile);

        });

        MenuItem saveTileSheet = new MenuItem("Export tile sheet");
        saveTileSheet.setOnAction((ActionEvent e) -> {
            fileChooser.setSelectedExtensionFilter(pngFilter);
            tileDisplayPane.saveAsTileSheet(fileChooser);
        });

        file.getItems().addAll(newMenu, open, save, saveAs, exportImage, saveTileSheet);


        final Menu menu2 = new Menu("Options");
        final Menu menu3 = new Menu("Help");

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(file, menu2, menu3);
        return menuBar;
    }

    private Node setupButtons() {

        Button applyChanges = new Button("Apply");
        Button clearGridChanges = new Button("Clear");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Change");
        alert.setHeaderText("Applying these changes will erase all grid and grid layer data.");
        alert.setContentText("Do you wish to continue?");
        applyChanges.setOnAction((event) -> {
            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK){
                for (TileGridPane t: layers.values()) {
                    t.updateSize(Integer.parseInt(heightInput.getText()), Integer.parseInt(widthInput.getText()));
                    t.drawGrid();
                }
            }
        });

        clearGridChanges.setOnAction((event) -> {
            TileGridPane temp = getSelectedLayer();
            temp.clearGrid();
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

    /**
     * Loads the state of a given file.
     * The file must be a mmap file made by this program.
     *
     * @param loadFile the file to load.
     */
    private void loadState(File loadFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(loadFile))) {

            String line = reader.readLine();
            String[] firstLine = line.split("\\s");
            tileSizeInput.setText(firstLine[0]);
            heightInput.setText(firstLine[1]);
            widthInput.setText(firstLine[2]);

            while((line = reader.readLine()) != null && !line.isEmpty()) {
                String filePath = line.split("\\s")[2];
                filePath = filePath.substring(6);
                System.out.println(filePath);
                tileDisplayPane.addTile(new File(filePath));
            }

            ArrayList<Tile> tileSet = tileDisplayPane.getTileSet();
            int height = Integer.parseInt(firstLine[1]);
            int width = Integer.parseInt(firstLine[2]);
            TileGridPane temp = null;

            int row = 0;
            while((line = reader.readLine()) != null) {
                if(line.contains("layer")) {
                    temp = new TileGridPane(height, width);
                    layers.put(line.trim(), temp);
                    row = 0;
                } else if(temp != null){
                    String[] tiles = (line.replaceAll("\\s", "")).split(",");
                    for (int i = 0; i < tiles.length; i++) {
                        int index = Integer.parseInt(tiles[i].trim());
                        if (index >= 0) {
                            temp.tileAtPos(row, i).updateTile(tileSet.get(index));
                        }
                    }
                    row++;
                }

            }
            updateLayerView();
            TileGridPane curr = layers.get(layerView.getSelectionModel().getSelectedItem());
            zoomPane.updateNode(curr);

            System.out.println("Loaded " + loadFile.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves all the layers, images and grid information to the save file.
     *
     * @param saveToFile the file currently opened.
     */
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

            for(String layer: layers.keySet()) {

                bw.append(layer);
                bw.newLine();

                TileGridPane t = layers.get(layer);

                /* Save grid */
                int width = t.getGridWidth();
                int height = t.getGridHeight();

                for(int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        Tile tile = t.tileAtPos(i, j);
                        bw.append(tileSet.indexOf(tile) + ", ");
                    }
                    bw.newLine();
                }
            }

            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Exports the state of the current selected grid to a PNG file.
     *
     * @param exportToFile the file to export the image to.
     */
    private void exportState(File exportToFile) {
        try {
            //Pad the capture area
            TileGridPane temp = getSelectedLayer();
            WritableImage writableImage = temp.snapshot(gridHeight * tileSize, gridWidth * tileSize);
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
            //Write the snapshot to the chosen file
            ImageIO.write(renderedImage, "png", exportToFile);
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    /**
     * Updates the layer display, should be called when layers are added or removed.
     */
    private void updateLayerView() {
        ObservableList<String> titles = FXCollections.observableArrayList(layers.keySet());
        layerView.setItems(titles);
    }

    /**
     * Returns the selected TileGridPane.
     *
     * @return selected grid pane.
     */
    private TileGridPane getSelectedLayer() {
        return layers.get(layerView.getSelectionModel().getSelectedItem());
    }
}

