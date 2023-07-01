package com.functionvisualizer;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.gillius.jfxutils.chart.AxisConstraint;
import org.gillius.jfxutils.chart.AxisConstraintStrategy;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;

import java.util.HashMap;

public class FunctionVisualizer extends Application {

    public static XYChart.Series series;
    private LineChart<Number, Number> coordinateSystem;

    private Window owner;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private Spinner<Double> rangeSpinner;
    public static TableView<Coordinate> coordinateTable;
    public static boolean isPressed;
    public static double m;
    public static double b;
    public static double range;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 840, 450);

        // Erstellung des Koordinatensystems
        xAxis = new NumberAxis();
        xAxis.setLabel("x");
        xAxis.setAutoRanging(false);

        yAxis = new NumberAxis();
        yAxis.setLabel("y");
        yAxis.setUpperBound(100);
        yAxis.setLowerBound(100);

        series = new XYChart.Series<>();
        series.setName("Punkte");
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

        // Die Methode zoomIn() wird aufgerufen, um in das Koordinatensytem zu zoomen
        coordinateSystem.setOnMouseClicked(this::zoomIn);

        // Hier werden die Formelnamen mit den entsprechenden Formeln gespeichert
        HashMap<String, String> funktionen = new HashMap<>();
        funktionen.put("proportionale Funktion", "f(x)=m*x");
        funktionen.put("lineare Funktion", "f(x)=m*x+b");

        // SplitMenuButton wird erstellt, um zwiscchen den Funktionstypen asuzuwählen
        SplitMenuButton functionTypButton = new SplitMenuButton();

        var formelLabel = new Label("Formel: Noch keine ausgewählt!");
        // Button der die Funktion, mit den entsprechenden Daten, erstellt
        var createFunctionButton = new Button("Erstellen");
        VBox.setMargin(createFunctionButton, new Insets(20));

        for (String func : funktionen.keySet()) {
            var menu = new MenuItem();
            menu.setText(func);
            functionTypButton.getItems().add(menu);

            menu.setOnAction(e -> handleMenuItemSelected(menu, funktionen, formelLabel,
                    createFunctionButton, mField, bField));
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
        var infoLabel = new Label("Welcher Ausschnitt (x-Achse) des Graphen soll gezeigt werden?");

        rangeSpinner = new Spinner<>();
        rangeSpinner.setEditable(true);

        var valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 10000000);
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
                    range = rangeSpinner.getValue();
                    m = Double.parseDouble(mField.getText());

                    xAxis.setLowerBound(-range);
                    xAxis.setUpperBound(range);
                    yAxis.setLowerBound(-range);
                    yAxis.setUpperBound(range);

                    isPressed = false;
                    CalculationThread thread = new CalculationThread();
                    thread.start();
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
                    range = rangeSpinner.getValue();
                    m = Double.parseDouble(mField.getText());
                    b = Double.parseDouble(bField.getText());

                    xAxis.setLowerBound(-range);
                    xAxis.setUpperBound(range);
                    yAxis.setLowerBound(-range);
                    yAxis.setUpperBound(range);

                    isPressed = true;
                    CalculationThread thread = new CalculationThread();
                    thread.start();
                } catch (NumberFormatException exception) {
                    owner = button.getScene().getWindow();
                    showError(AlertType.ERROR, owner, "Error: " + exception,
                            "Die angegebenen Zahlen konnten nicht formatiert werden. " +
                                    "Bitte überprüfen Sie die Zahlen.");
                }
            });
        }
    }

    private void zoomIn(MouseEvent e) {
        // Das Dependency jfxutils, macht das der LineChart verändert werden kann
        ChartPanManager panner = new ChartPanManager(coordinateSystem);
        //Wenn der rechte Mousebutton gedrückt wurde, verschiebt man das Sichtfeld des Koordinatensytsems
        panner.setMouseFilter(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {//set your custom combination to trigger navigation
                // let it through
            } else {
                mouseEvent.consume();
            }
        });
        AxisConstraintStrategy strategies = chartInputContext -> AxisConstraint.Both;
        panner.setAxisConstraintStrategy(strategies);
        panner.start();

        //holding the right mouse button will draw a rectangle to zoom to desired location
        JFXChartUtil.setupZooming(coordinateSystem, mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.SECONDARY)//set your custom combination to trigger rectangle zooming
                mouseEvent.consume();
        });
    }
    /*
    @Override
    public void createLinearFunction(double m, double b, double range, TableView<Coordinate> pointTable, XYChart.Series series) {
        ObservableList<Coordinate> dataPoints = FXCollections.observableArrayList();
        series.getData().clear();
        for (double x = -range; x <= range; x++) { //Variable range definiert die Länge des Graphen
            double y = m * x + b;
            dataPoints.add(new Coordinate(x, y));
            series.getData().add(new XYChart.Data<>(x, y));
        }
        pointTable.setItems(dataPoints);
    }

    @Override
    public void createProportionaleFunction(double m, double range, TableView<Coordinate> pointTable, XYChart.Series series) {
        ObservableList<Coordinate> dataPoints = FXCollections.observableArrayList();
        series.getData().clear();
        for (double x = -range; x <= range; x++) {
            double y = m * x;
            dataPoints.add(new Coordinate(x, y));
            series.getData().add(new XYChart.Data<>(x, y));
        }
        pointTable.setItems(dataPoints);
    }
     */

    private void showError(AlertType type, Window owner, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.show();
    }

}

