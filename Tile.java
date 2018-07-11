import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.Serializable;

public class Tile extends ImageView {

    public static int TILE_SIZE = 37;
    public static int LARGE_TILE_SIZE = TILE_SIZE * 5;

    private ImageView tile;
    private Image image;
    private String tileName;
    private double height;
    private double width;
    private String imagePath;
    private static boolean emptyTile;
    private int x;
    private int y;

    private Tile() {
        imagePath = "/images/empty.png";
        image = new Image(imagePath, TILE_SIZE, TILE_SIZE, false, false );
        setImage(image);
        tile = new ImageView(image);
        tileName = "emptyTile";
        this.height = tile.getImage().getHeight();
        this.width = tile.getImage().getWidth();
        emptyTile = true;
        init();
    }

    public Tile (File file) {
        imagePath = file.toURI().toString();
        image = new Image(imagePath, TILE_SIZE, TILE_SIZE, false, false );
        setImage(image);
        tile = new ImageView(image);

        tileName = file.getName().substring(0, file.getName().indexOf('.'));
        this.height = tile.getImage().getHeight();
        this.width = tile.getImage().getWidth();
        emptyTile = false;
        init();
    }

    private void init() {
        setOnMouseEntered((MouseEvent t) -> {
            setCursor(Cursor.HAND);
        });

        setOnMouseExited((MouseEvent t) -> {
            setCursor(Cursor.DEFAULT);
        });
    }

    public static Tile getEmptyTile() {
        Tile emptyTile = new Tile();
        return emptyTile;
    }

    public void updateTile(Tile other) {
        imagePath = other.imagePath;
        image = other.image;
        setImage(image);
        this.tile = other.tile;
        tileName = other.tileName;
        height = tile.getImage().getHeight();
        width = tile.getImage().getWidth();
        emptyTile = false;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public String getTileName() {
        return tileName;
    }

    public static boolean isEmptyTile() {
        return emptyTile;
    }

    public int compareTo(Tile other) {
        return this.tileName.compareTo(other.tileName);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  Tile) {
            Tile other = (Tile) obj;
            return compareTo(other) == 0;
        }else {
            return false;
        }
    }

    public ImageView getEnlargedView() {
        Image imageLarge = new Image(imagePath, LARGE_TILE_SIZE, LARGE_TILE_SIZE, false, false );
        ImageView viewLarge = new ImageView(imageLarge);
        viewLarge.setEffect(new DropShadow(20, Color.BLACK));
        return viewLarge;
    }

    public void setCoords(int i, int j) {
        x = i;
        y = j;
    }

    public String toString() {
        return tileName + ": " + imagePath;
    }

    public void getCoords() {
       System.out.println(x + ", " + y);
    }
}
