
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.*;

public class LayerManager extends BorderPane {

    private static int PANEL_WIDTH = 40 + Tile.LARGE_TILE_SIZE;
    private ListView<String> layerView;
    private Map<String, TileGridPane> layers;
    private Button addLayer;

    private int gridHeight;
    private int gridWidth;

    public LayerManager(TileGridPane startLayer, int height, int width) {
        layers = new HashMap<>();
        layers.put("layer 1", startLayer);
        layerView = new ListView<>();
        addLayer = new Button("Add Layer");
        gridHeight = height;
        gridWidth = width;
        init();
    }


    public void init() {
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
        });

        updateLayerView();

        setTop(buttonHolder);
        setCenter(layerView);
        setMaxWidth(PANEL_WIDTH);
        setMinHeight(PANEL_WIDTH);
        setPrefSize(PANEL_WIDTH, PANEL_WIDTH * 2);
        layerView.setPrefSize(PANEL_WIDTH, getHeight());
        setStyle("-fx-background-color: #eaeaea;" +
                "-fx-border-color: #000000;" +
                "-fx-border-width: 2px;");
    }

    private void updateLayerView() {
        ObservableList<String> titles = FXCollections.observableArrayList(layers.keySet());
        layerView.setItems(titles);
    }
}
