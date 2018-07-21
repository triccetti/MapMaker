import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TileDisplayPane extends BorderPane {

    public static int PANEL_WIDTH = 45 + Tile.LARGE_TILE_SIZE;
    private static int SPACING = 20;

    private Button addTile;
    private Button removeTile;
    private Button addFolder;

    private GridPane tileDisplay;

    private FlowPane view;

    private TileSet tileSet;

    public TileDisplayPane() {
        super();
        addTile = new Button("Add");
        removeTile = new Button("Remove");
        addFolder = new Button("Add All");

        tileDisplay = new GridPane();

        view = new FlowPane();

        tileSet = new TileSet();
        init();
    }

    private void init() {

        addTile.setGraphic(new ImageView(new Image("images/add.png")));
        addFolder.setGraphic(new ImageView(new Image("images/addFolder.png")));
        removeTile.setGraphic(new ImageView(new Image("images/remove.png")));

        tileDisplay.setPadding(new Insets(2));
        tileDisplay.setHgap(2);
        tileDisplay.setVgap(2);

        final FileChooser fileChooser = new FileChooser();


        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.TAB) {
                event.consume();
                tileSet.setNextSelected();
                updateTileSet();
                view.getChildren().clear();
                view.getChildren().add(tileSet.getSelected().getEnlargedView());
            }
        });

        addTile.setOnAction((ActionEvent event) -> {
            File file = fileChooser.showOpenDialog(null);
            if(file != null) {
                addTile(file);
            }
            updateTileSet();
        });

        addFolder.setOnAction((ActionEvent event) -> {
            List<File> files = fileChooser.showOpenMultipleDialog(null);
            if(files != null) {
                for (File file : files) {
                    addTile(file);
                }
                updateTileSet();
            }
        });

        removeTile.setOnAction((ActionEvent event) -> {
            if(tileSet.hasSelectedTile()) {
                tileSet.removeSelected();
                updateTileSet();
                view.getChildren().clear();
            }
        });

        FlowPane buttonHolder = new FlowPane();
        buttonHolder.getChildren().addAll(addTile, addFolder, removeTile);
        buttonHolder.setHgap(5);
        buttonHolder.setVgap(5);
        buttonHolder.setPadding(new Insets(5, 5, 10, 5));
        buttonHolder.setAlignment(Pos.CENTER);

        FlowPane previewPane = new FlowPane();
        previewPane.setStyle("-fx-background-color: #909090;");
        previewPane.setPadding(new Insets(0,0,0, 10));


        Text preview = new Text("PREVIEW:");
        preview.setFill(Color.WHITE);
        preview.setFont(Font.font("Helvetica", FontWeight.LIGHT, 35));

        view.setPadding(new Insets(SPACING));
        view.setPrefSize(PANEL_WIDTH, PANEL_WIDTH);
        previewPane.getChildren().addAll(preview, view);

        setMaxWidth(PANEL_WIDTH);
        setMinHeight(PANEL_WIDTH);
        setPrefSize(PANEL_WIDTH, PANEL_WIDTH * 2);
        setTop(buttonHolder);

        ScrollPane sp = new ScrollPane(tileDisplay);
        sp.setFitToWidth(true);
        setCenter(sp);
        setBottom(previewPane);
        setStyle("-fx-background-color: #eaeaea;" +
                 "-fx-border-color: #000000;" +
                 "-fx-border-width: 2px;");
    }

    public void addTile(Tile tile) {
        if (tile != null) {
            if (!tileSet.containsTile(tile)) {
                tileSet.addTile(tile);
            }
        }
        updateTileSet();
    }

    public void addTile(File file) {
        if (file != null) {
            Tile temp = new Tile(file);
            if (!tileSet.containsTile(temp)) {
                tileSet.addTile(temp);
            }
        }
        updateTileSet();
    }

    public void clearTiles() {
        tileSet.clear();
    }

    private void updateTileSet() {
        int row = 0;
        int col = 0;

        tileDisplay.getChildren().clear();
        for(Tile tile : tileSet.getTiles()) {
            tile.setOnMouseClicked((MouseEvent event) -> {
                onClickTileHandler(tile);
            });
            if (tileSet.isSelected(tile)) {
                tile.setEffect(new DropShadow(SPACING, Color.BLACK));
            } else {
                tile.setEffect(null);
            }

            if(!tileDisplay.getChildren().contains(tile)) {
                tileDisplay.add(tile, col, row);
                col++;
                if (col > 5) {
                    row++;
                    col = 0;
                }
            }
        }
    }

    public void onClickTileHandler(Tile tile) {
        tileSet.setSelected(tile);
        System.out.println(tile.getTileName() + " is selected!");
        updateTileSet();
        view.getChildren().clear();
        view.getChildren().add(tile.getEnlargedView());
    }

    public ArrayList<Tile> getTileSet() {
        return tileSet.getTiles();
    }

    public String saveAsTileSheet(File saveFile, int tileSize) {

        ArrayList<Tile> tiles = getTileSet();
        GridPane tileSheet = new GridPane();
        int row = 0;
        int col = 0;

        for(Tile t : tiles) {
            tileSheet.add(new Tile(t), col, row);
            col++;
            if(col >= 8) {
                row++;
                col = 0;
            }
        }

        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        ImageView imageView = new ImageView(tileSheet.snapshot(sp,null));
        imageView.setPreserveRatio(true);
        if(tiles.size() < 8) {
            imageView.setFitWidth(tiles.size() * tileSize);
        } else {
            imageView.setFitWidth(8 * tileSize);
        }
        // imageView.setFitHeight(row * tileSize);
        WritableImage image = imageView.snapshot(sp, null);

        RenderedImage renderedImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(renderedImage, "png", saveFile);
        } catch (IOException e) {
            System.out.println("Could not save tile sheet.");
        }
        return saveFile.getPath();
    }

    public void loadTileSet(File tileSheetFile, int tileSize, int numTiles) {
        ImageView tileSheet = new ImageView(new Image(tileSheetFile.toURI().toString()));
        int tileSheetHeight = (int) tileSheet.getImage().getHeight() / tileSize;
        int tileSheetWidth = (int) tileSheet.getImage().getWidth() / tileSize;
        System.out.println(tileSheetHeight + " " + tileSheetWidth);

        int countedTiles = 0;
        for(int i = 0; i < tileSheetHeight; i++) {
            for(int j = 0; j < tileSheetWidth; j++) {
                try {
                    File temp = File.createTempFile("tileSheet" , ".png");

                    SnapshotParameters sp = new SnapshotParameters();
                    sp.setViewport(new Rectangle2D(j * tileSize, i * tileSize , tileSize,  tileSize));
                    sp.setFill(Color.TRANSPARENT);
                    ImageView imageView = new ImageView(tileSheet.snapshot(sp, null));
                    imageView.setFitHeight(tileSize);
                    imageView.setFitWidth(tileSize);
                    WritableImage image = imageView.snapshot(sp, null);
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", temp);
                    addTile(new Tile(temp));
                    countedTiles++;
                    if(countedTiles > numTiles) {
                        break;
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
