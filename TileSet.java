import java.util.ArrayList;

public class TileSet {

    private ArrayList<Tile> tiles;
    private static Tile selected;
    private int selectedIndex;

    public TileSet()  {
        tiles = new ArrayList<>();
        selected = null;
        selectedIndex = -1;
    }

    public TileSet(Tile tile) {
        this();
        tiles.add(tile);
    }

    public void addTile(Tile tile) {
        tiles.add(tile);
    }

    public ArrayList<Tile> getTiles() {
        return tiles;
    }

    public void setSelected(Tile tile) {
        if(tiles.contains(tile)) {
            selectedIndex = tiles.indexOf(tile);
            selected = tile;
        }
    }

    public void setNextSelected() {
        if(selectedIndex < 0) {
            selectedIndex = 0;

        } else if(selectedIndex == tiles.size() - 1){
            selectedIndex = 0;
        } else {
            selectedIndex++;
        }
        selected = tiles.get(selectedIndex);
    }

    public boolean hasSelectedTile() {
        return (selected != null && selectedIndex >= 0);
    }

    public boolean containsTile(Tile tile) {
        return tiles.contains(tile);
    }

    public void removeSelected() {
        if(hasSelectedTile()) {
            tiles.remove(selectedIndex);
            selected = null;
            selectedIndex = -1;
        }
    }

    public void clear() {
        tiles.clear();
    }

    public static Tile getSelected() {
        return selected;
    }

    public boolean isSelected(Tile tile) {
        return (selected != null && selected == tile && selectedIndex == tiles.indexOf(tile));
    }
}
