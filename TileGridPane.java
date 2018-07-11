
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;

import java.util.Comparator;
import java.util.Map;


public class TileGridPane extends GridPane {


    private static int PANEL_WIDTH = 40 + Tile.LARGE_TILE_SIZE;
    private ListView<String> layerView;
    private Map<String, TileGridPane> layers;
    private Button addLayer;

    private Tile[][] grid;
    private int height;
    private int width;
    private boolean isDragged;

    public TileGridPane(int height, int width) {
        this.height = height;
        this.width = width;
        grid = new Tile[width][height];
        isDragged = false;
        drawGrid();
    }

    public void updateSize(int height, int width) {
        this.height = height;
        this.width = width;
        grid = new Tile[width][height];
        getChildren().clear();
    }

    public void drawGrid() {

        setAlignment(Pos.CENTER);
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                Tile tile = Tile.getEmptyTile();

                addEventFilter(MouseEvent.DRAG_DETECTED , (MouseEvent event) -> {
                    System.out.println("Drag ON " + tile.getTileName());
                    if (!event.isMiddleButtonDown()) {
                        event.consume();
                    }
                    startFullDrag();
                });

                tile.setOnMouseDragEntered((MouseEvent e) -> {
                    System.out.println("Drag ON " + tile.getTileName());
                    if(TileSet.getSelected() != null && !e.isMiddleButtonDown()) {
                        tile.updateTile(TileSet.getSelected());
                    }
                });

                tile.addEventFilter(MouseEvent.ANY, event -> {
                        if (!event.isMiddleButtonDown()) {
                            event.consume();
                        }
                        if (event.isPrimaryButtonDown() && TileSet.getSelected() != null) {
                            System.out.println("CLICK ON " + tile.getTileName());
                            tile.updateTile(TileSet.getSelected());
                            isDragged = true;
                        }
                        if (event.isSecondaryButtonDown()) {
                            tile.updateTile(Tile.getEmptyTile());
                        }

                });
                grid[i][j] = tile;
                add(tile, i, j);
            }
        }

    }

    public int getGridWidth() {
        return width;
    }

    public int getGridHeight() {
        return height;
    }

    public void clearGrid() {
        grid = new Tile[width][height];
        getChildren().clear();
        drawGrid();
    }

    public WritableImage snapshot(int height, int width) {
        ImageView imageView = new ImageView(this.snapshot(new SnapshotParameters(),null));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        return imageView.snapshot(null, null);
    }

    public Tile tileAtPos(int x, int y) {
        Node result = null;

        for (Node node : getChildren()) {
            if(getRowIndex(node) == x && getColumnIndex(node) == y) {
                result = node;
                break;
            }
        }

        return (Tile) result;
    }
}
