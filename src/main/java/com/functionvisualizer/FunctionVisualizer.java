package com.functionvisualizer;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.HashMap;

public class FunctionVisualizer extends Application {

    private XYChart.Series series;
    private LineChart<Number, Number> coordinateSystem;
    private static Stage mainStage;

    private Window owner;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private Spinner<Integer> rangeSpinner;
    private TableView<Coordinate> coordinateTable;

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 800, 400);

        // Erstellung des Koordinatensystems
        xAxis = new NumberAxis();
        xAxis.setLabel("x");
        xAxis.setAutoRanging(false);

        yAxis = new NumberAxis();
        yAxis.setLabel("y");
        yAxis.setUpperBound(100);
        yAxis.setLowerBound(100);

        series = new XYChart.Series<>();
        coordinateSystem = new LineChart<>(xAxis, yAxis);
        coordinateSystem.getData().add(series);
        coordinateSystem.autosize();

        // TextField wird erstellt, um die Steigung einzutragen
        var mField = new TextField();
        mField.setPromptText("Steigung m");

        var xField = new TextField();
        xField.setPromptText("Punkt x");

        var bField = new TextField();
        bField.setPromptText("y-Achsenabschnitt");
        bField.setDisable(true);

        // Zoom-Buttons werden erstellt, die in einer HBox neben dem Button, der die Graphen erstellt, plaziert werden
        coordinateSystem.setOnScroll(this::zoomIn);

        //var label = new Label("Funktionstypen auswählen:");

        // Hier werden die Formelnamen mit den entsprechenden Formeln gespeichert
        HashMap<String, String> funktionen = new HashMap<>();
        funktionen.put("proportionale Funktion", "f(x)=m*x");
        funktionen.put("lineare Funktion", "f(x)=m*x+b");

        // SplitMenuButton wird erstellt, um zwiscchen den Funktionstypen asuzuwählen
        SplitMenuButton functionTypButton = new SplitMenuButton();

        var formelLabel = new Label();
        // Button der die Funktion, mit den entsprechenden Daten, erstellt
        var createFunctionButton = new Button("Erstellen");
        VBox.setMargin(createFunctionButton, new Insets(20));

        for (String func : funktionen.keySet()) {
            var menu = new MenuItem();
            menu.setText(func);
            functionTypButton.getItems().add(menu);

            menu.setOnAction(e -> handleMenuItemSelected(menu, funktionen, formelLabel, createFunctionButton, mField, bField));
        }

        functionTypButton.setText("Funktionstyp");
        functionTypButton.setPrefWidth(150);
        BorderPane.setMargin(functionTypButton, new Insets(20));

        // Tabelle + Tabellenspalten werden erstellt, die die Inhalte von der Klasse Coordinate enthalten
        coordinateTable = new TableView<>();
        coordinateTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Coordinate, Double> xColumn = new TableColumn<>("x");
        xColumn.setCellValueFactory(data -> data.getValue().xProperty());

        TableColumn<Coordinate, Double> yColumn = new TableColumn<>("y");
        yColumn.setCellValueFactory(data -> data.getValue().yProperty());
        coordinateTable.getColumns().addAll(xColumn, yColumn);


        // Spinner wird erstellt, um die Länge des Graphen einzustellen
        var infoLabel = new Label("Welcher Ausschnitt des Graphen soll gezeigt werden?");

        rangeSpinner = new Spinner<>();
        rangeSpinner.setEditable(true);

        var valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100);
        rangeSpinner.setValueFactory(valueFactory);

        /* Hier werden die Nodes der VBox hinzugefügt und angezeigt */
        VBox settingsSide = new VBox();
        settingsSide.getChildren().addAll(
                functionTypButton,
                formelLabel,
                mField,
                bField,
                infoLabel,
                rangeSpinner,
                createFunctionButton,
                coordinateTable);

        root.setLeft(coordinateSystem);
        root.setRight(settingsSide);

        stage.setScene(scene);
        stage.setTitle("Function Visualisation");
        stage.show();
    }

    private void handleMenuItemSelected(MenuItem menuItem, HashMap<String, String> map, Label formelLabel, Button button,
                                        TextField mField, TextField bField) {

        String selectedItem = menuItem.getText();
        formelLabel.setText("Formel: " + map.get(selectedItem));

        if (map.containsKey(selectedItem) && selectedItem.contentEquals("proportionale Funktion")) {
            bField.setDisable(true);
            button.setOnMouseClicked(e -> {
                try {
                    int range = rangeSpinner.getValue();
                    double m = Double.parseDouble(mField.getText());

                    xAxis.setLowerBound(-range);
                    xAxis.setUpperBound(range);
                    yAxis.setLowerBound(-range);
                    yAxis.setUpperBound(range);

                    createProportionaleFunction(m, range, coordinateTable);
                } catch (NumberFormatException exception) {
                    owner = button.getScene().getWindow();
                    showError(AlertType.ERROR, owner, "Error: " + exception,
                            "Die angegebenen Zahlen konnten nicht formatiert werden. " +
                                    "Bitte überprüfen Sie die Zahlen.");
                }
            });
        } else if (map.containsKey(selectedItem) && selectedItem.contentEquals("lineare Funktion")) {
            bField.setDisable(false);
            button.setOnMouseClicked(e -> {
                try {
                    int range = rangeSpinner.getValue();
                    double m = Double.parseDouble(mField.getText());
                    double b = Double.parseDouble(bField.getText());

                    xAxis.setLowerBound(-range);
                    xAxis.setUpperBound(range);
                    yAxis.setLowerBound(-range);
                    yAxis.setUpperBound(range);

                    createLinearFunction(m, b, range, coordinateTable);
                } catch (NumberFormatException exception) {
                    owner = button.getScene().getWindow();
                    showError(AlertType.ERROR, owner, "Error: " + exception,
                            "Die angegebenen Zahlen konnten nicht formatiert werden. " +
                                    "Bitte überprüfen Sie die Zahlen.");
                }
            });
        }
    }

    private void zoomIn(ScrollEvent e) {
        final double lowerX = xAxis.getLowerBound();
        final double upperX = xAxis.getUpperBound();

        final double minX = xAxis.getLowerBound();
        final double maxX = xAxis.getUpperBound();
        double threshold = minX + (maxX - minX) / 2;
        double x = e.getX();
        double value = xAxis.getValueForDisplay(x).doubleValue();
        double direction = e.getDeltaY();

        if (direction > 0) {
            if (maxX - minX <= 1) {
                return;
            }
            if (value > threshold) {
                xAxis.setLowerBound(minX + 1);
            } else {
                xAxis.setUpperBound(maxX - 1);
            }
        } else {
            if (value < threshold) {
                double nextBound = Math.max(lowerX, minX - 1);
                xAxis.setLowerBound(nextBound);
            } else {
                double nextBound = Math.min(upperX, maxX + 1);
                xAxis.setUpperBound(nextBound);
            }
        }
    }

    private void createLinearFunction(double m, double b, int range, TableView<Coordinate> pointTable) {
        ObservableList<Coordinate> dataPoints = FXCollections.observableArrayList();
        series.getData().clear();
        for (double x = -range; x <= range; x++) { //Variable range definiert die Länge des Graphen
            double y = m * x + b;
            dataPoints.add(new Coordinate(x, y));
            series.getData().add(new XYChart.Data<>(x, y));
        }
        pointTable.setItems(dataPoints);
    }

    private void createProportionaleFunction(double m, int range, TableView<Coordinate> pointTable) {
        ObservableList<Coordinate> dataPoints = FXCollections.observableArrayList();
        series.getData().clear();
        for (double x = -range; x <= range; x++) {
            double y = m * x;
            dataPoints.add(new Coordinate(x, y));
            series.getData().add(new XYChart.Data<>(x, y));
        }
        pointTable.setItems(dataPoints);
    }

    private void showError(AlertType type, Window owner, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

