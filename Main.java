import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

public class Main extends Application {

    final private static int DEFAULT_HEIGHT = 784;
    final private static int DEFAULT_WIDTH = 1000;

    final private static int TEXT_BOX_WIDTH = 50;
    final private static int TILE_SIZE_DEFAULT = 16;
    final private static int GRID_WIDTH = 20;
    final private static int GRID_HEIGHT = 20;

    private static int tileSize;
    private static int gridHeight;
    private static int gridWidth;

    private TextField tileSizeInput;
    private TextField heightInput;
    private TextField widthInput;
    private TileDisplayPane tileDisplayPane;
    private TileGridPane tileGridPane;
    private static File currentFile;
    public static void main(String[] args) {

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

        primaryStage.setTitle("Map Maker!");
        BorderPane root = new BorderPane();

        tileDisplayPane = new TileDisplayPane();
        BorderPane gridPane = new BorderPane();
        tileGridPane = new TileGridPane(gridHeight, gridWidth);

        // Zoom grid controller.
        ZoomScrollPane zoomPane = new ZoomScrollPane(tileGridPane);
        gridPane.setTop(setupButtons());
        gridPane.setCenter(zoomPane);

        root.setTop(setupMenu());
        root.setCenter(gridPane);
        root.setRight(tileDisplayPane);
        root.setPadding(new Insets(2));

        primaryStage.setScene(new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT));
        
        primaryStage.setMinHeight(DEFAULT_HEIGHT / 2);
        primaryStage.setMinWidth(DEFAULT_WIDTH / 2);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private Node setupMenu() {
        final FileChooser fileChooser = new FileChooser();
        final Menu file = new Menu("File");

        MenuItem open = new MenuItem("Open");
        open.setOnAction((ActionEvent e) -> {
            File openedFile = fileChooser.showOpenDialog(null);
            if(openedFile != null) {
                // YAY
                System.out.println("You opened " + openedFile.getName());
                currentFile = openedFile;
                loadState(currentFile);
            }
        });
        MenuItem saveAs = new MenuItem("Save As");
        saveAs.setAccelerator( new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN, KeyCombination.CONTROL_DOWN));
        saveAs.setOnAction((ActionEvent e) -> {
            File saveFile = fileChooser.showSaveDialog(null);
            saveState(saveFile);
            currentFile = saveFile;
            System.out.println("You saved " + currentFile.getName());
        });
        MenuItem save = new MenuItem("Save");
        save.setAccelerator( new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        save.setOnAction((ActionEvent e) -> {
            if(currentFile == null) {
                File saveFile = fileChooser.showSaveDialog(null);
                saveState(saveFile);
                currentFile = saveFile;
                System.out.println("You saved " + currentFile.getName());
            } else {
                saveState(currentFile);
                System.out.println("You saved " + currentFile.getName());
            }
        });
        file.getItems().addAll(open, save, saveAs);

        final Menu menu2 = new Menu("Options");
        final Menu menu3 = new Menu("Help");

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(file, menu2, menu3);
        return menuBar;
    }

    private void loadState(File loadFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(loadFile))) {

            String line = reader.readLine();
            String[] firstLine = line.split("\t");
            tileSizeInput.setText(firstLine[0]);
            heightInput.setText(firstLine[1]);
            widthInput.setText(firstLine[2]);
            tileGridPane.updateSize(Integer.parseInt(heightInput.getText()), Integer.parseInt(widthInput.getText()));

            while((line = reader.readLine()) != null && !line.isEmpty()) {
                String filePath = line.split("\\s")[2];
                filePath = filePath.substring(6);
                System.out.println(filePath);
                tileDisplayPane.addTile(new File(filePath));
            }

            tileGridPane.drawGrid();

            int height = Integer.parseInt(firstLine[1]);
            int width = Integer.parseInt(firstLine[2]);
            int row = 0;
            int col = 0;
            ArrayList<Tile> tileSet = tileDisplayPane.getTileSet();
            while((line = reader.readLine()) != null) {
                String[] tiles = (line.replaceAll("\\s","")).split(",");

                for(int i = 0; i < tiles.length; i++) {
                    int index = Integer.parseInt(tiles[i].trim());
                    //tileGridPane.add(tileSet.get(index), i, height);
                    System.out.println(index);
                    System.out.println(tiles[i]);
                    tileGridPane.tileAtPos(row, i).updateTile(tileSet.get(index));
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
            int width = tileGridPane.getGridWidth();
            int height = tileGridPane.getGridHeight();

            for(int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    Tile tile = tileGridPane.tileAtPos(i, j);
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
                tileGridPane.updateSize(Integer.parseInt(heightInput.getText()), Integer.parseInt(widthInput.getText()));
                tileGridPane.drawGrid();
            }
        });

        clearGridChanges.setOnAction((event) -> {
            tileGridPane.clearGrid();
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

}

