package com.functionvisualizer;

import com.functionvisualizer.functions.LineareFunction;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.gillius.jfxutils.chart.AxisConstraint;
import org.gillius.jfxutils.chart.AxisConstraintStrategy;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FunctionVisualizer extends Application {

    public static XYChart.Series<Number, Number> series;
    private LineChart<Number, Number> coordinateSystem;

    private Window owner;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private Spinner<Double> rangeSpinner;
    public static TableView<Coordinate> coordinateTable;
    private Scene scene;
    public static int funcIndex;
    public static double m;
    public static double b;
    public static double n;
    public static double range;
    private static final Button createFunctionButton = new Button("Erstellen");

    @Override
    public void start(Stage stage) {
        functionVisualizerGUI(stage);
    }

    private void functionVisualizerGUI(Stage stage) {
        BorderPane root = new BorderPane();
        scene = new Scene(root, 840, 450);

        // MenuBar erstellen, um verschiedene Berechnungen durchzuführen
        MenuBar bar = new MenuBar();
        Menu optionMenu = new Menu("Optionen");

        MenuItem caluclateLineareFunctionItem = new MenuItem("Lineare Funktion berechnen");
        caluclateLineareFunctionItem.setOnAction(e -> calculateFuncGUI(stage));

        optionMenu.getItems().add(caluclateLineareFunctionItem);
        bar.getMenus().add(optionMenu);


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
        funktionen.put("einfache quadratische Funktion", "f(x)=n*x²");

        // SplitMenuButton wird erstellt, um zwiscchen den Funktionstypen asuzuwählen
        SplitMenuButton functionTypButton = new SplitMenuButton();

        var formelArea = new Label("Formel: Noch keine ausgewählt!");
        // Button der die Funktion, mit den entsprechenden Daten, erstellt
        VBox.setMargin(createFunctionButton, new Insets(20));

        for (String func : funktionen.keySet()) {
            var menu = new MenuItem();
            menu.setText(func);
            functionTypButton.getItems().add(menu);

            menu.setOnAction(e -> handleMenuItemSelected(menu, funktionen, formelArea,
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
        var infoLabel = new Label("Wert für x:");

        rangeSpinner = new Spinner<>();
        rangeSpinner.setEditable(true);

        var valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 1000);
        rangeSpinner.setValueFactory(valueFactory);

        coordinateTable.setOnMouseClicked(e -> markPoints(coordinateTable));

        /* Hier werden die Nodes der VBox hinzugefügt und angezeigt */
        VBox settingsSide = new VBox();
        settingsSide.getChildren().addAll(
                functionTypButton,
                formelArea,
                mField,
                bField,
                infoLabel,
                rangeSpinner,
                createFunctionButton,
                coordinateTable);

        root.setTop(bar);
        root.setLeft(coordinateSystem);
        root.setRight(settingsSide);

        stage.setScene(scene);
        stage.setTitle("Function Visualisation");
        stage.show();
    }

    private void handleMenuItemSelected(MenuItem menuItem, HashMap<String, String> map, Label formelArea, Button button,
                                        TextField mField, TextField bField) {

        String selectedItem = menuItem.getText();
        formelArea.setText("Formel: " + map.get(selectedItem));

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

                    funcIndex = 1;
                    CalculationThread thread = new CalculationThread();
                    //System.out.println(thread.calculateLineareFunction(new Coordinate(-1, -6), new Coordinate(3.5, -1.5)));
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

                    funcIndex = 2;
                    CalculationThread thread = new CalculationThread();
                    thread.start();
                } catch (NumberFormatException exception) {
                    owner = button.getScene().getWindow();
                    showError(AlertType.ERROR, owner, "Error: " + exception,
                            "Die angegebenen Zahlen konnten nicht formatiert werden. " +
                                    "Bitte überprüfen Sie die Zahlen.");
                }
            });
        } else if (map.containsKey(selectedItem) && selectedItem.contentEquals("einfache quadratische Funktion")) {
            bField.setDisable(true);
            mField.setPromptText("n");
            button.setOnMouseClicked(e -> {
                try {
                    range = rangeSpinner.getValue();
                    n = Double.parseDouble(mField.getText());

                    xAxis.setLowerBound(-range);
                    xAxis.setUpperBound(range);
                    yAxis.setLowerBound(-range);
                    yAxis.setUpperBound(range);

                    funcIndex = 3;
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
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                // Lässt es durchgehen
            } else {
                mouseEvent.consume();
            }
        });
        AxisConstraintStrategy strategies = chartInputContext -> AxisConstraint.Both;
        panner.setAxisConstraintStrategy(strategies);
        panner.start();

        // Wenn man die rechte Maustaste gedrückt hält, wird ein Rechteck gezeichnet, um an die gewünschte Stelle zu zoomen
        JFXChartUtil.setupZooming(coordinateSystem, mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.SECONDARY) // Zoomen wird getriggert, wenn man die secondäre Maustaste gedrückt hat
                mouseEvent.consume();
        });
    }

    private void calculateFuncGUI(Stage stage) {
        // UI
        GridPane pane = new GridPane();
        VBox box = new VBox();
        scene = new Scene(box);

        Button applyButton = new Button("Bestätigen");
        Button backButton = new Button("Zurück");
        Button visualizeButton = new Button("Visualisieren");

        TextArea formelArea = new TextArea();
        formelArea.setPromptText("Funktionsgleichung: ");
        formelArea.setWrapText(true);

        pane.add(backButton, 0, 0);
        pane.add(applyButton, 0, 3);
        pane.add(formelArea, 1, 4);
        box.getChildren().addAll(pane, formelArea, visualizeButton);

        int inset = 10;
        VBox.setMargin(formelArea, new Insets(inset));
        VBox.setMargin(visualizeButton, new Insets(inset));
        VBox.setMargin(pane, new Insets(inset));

        // Erstellung der Textfelder, um die Daten des Benutzers in die Coordinate Klasse zu integrieren
        String[] fields = {"x-Koordinate", "y-Koordinate"};
        int l = fields.length;
        List<TextField> textFields = new ArrayList<>();
        for (int row = 0; row < l; row++) {
            for (int col = 1; col < l + 1; col++) {
                TextField field = new TextField();
                field.setPromptText(fields[col/2]);
                pane.add(field, row, col);
                textFields.add(field);
            }
        }

        // Speichert die Funktionsgleichung
        final String[] func = new String[1];
        applyButton.setOnAction(e -> {
            func[0] = visualizeCalculatedFunc(textFields, formelArea, applyButton, visualizeButton, stage);
            formelArea.setText(func[0]);
        });

        backButton.setOnAction(e -> functionVisualizerGUI(stage));

        stage.setScene(scene);
    }

    private String visualizeCalculatedFunc(List<TextField> textFields, TextArea area, Button button, Button button1, Stage stage) {
        List<Double> doubles = new ArrayList<>();
        LineareFunction function = new LineareFunction();
        try {
            for (TextField tf : textFields) {
                double num = Double.parseDouble(tf.getText());
                doubles.add(num);

                if (doubles.size() >= 4) {
                    Coordinate coordinate1 = new Coordinate(doubles.get(0), doubles.get(1));
                    Coordinate coordinate2 = new Coordinate(doubles.get(2), doubles.get(3));

                    String func = function.calculateLineareFunction(coordinate1, coordinate2);

                    // Die Funktion, welche berechnet wurde, wird in dem Koordinatensystem veranschaulicht
                    button1.setOnAction(e -> {
                        double m = function.getM();
                        double b = function.getB();

                        functionVisualizerGUI(stage);
                        function.create(m, b, coordinate1.getX(), coordinateTable, series);
                    });

                    return func;
                }
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            Window owner = button.getScene().getWindow();
            showError(AlertType.ERROR, owner, ex.toString(), "Zahlen konnten nicht formatiert werden!");
        }
        return null;
    }

    private void markPoints(TableView<Coordinate> table) {
        // Schreibe Code, sodass wenn selectedCoor in der Tabelle ausgewählt wurde, dass dieser Punkt im Koordinatensystem markiert wird
        List<XYChart.Data<Number, Number>> pointsToAdd = new ArrayList<>();
        Coordinate selectedCoor = table.getItems().get(table.focusModelProperty().get().getFocusedIndex());

        // Wenn mehrere Punkte markiert werden, werden diese wieder entfernt, damit immer nur einer angezeigt wird
        series.getData().removeIf(data -> data.getNode() instanceof Circle);

        for (XYChart.Data<Number, Number> value : series.getData()) {
            if (value.getXValue().equals(selectedCoor.getX()) && value.getYValue().equals(selectedCoor.getY())) {
                Circle circle = new Circle(5);
                circle.setFill(Color.BLACK);
                XYChart.Data<Number, Number> data = new XYChart.Data<>(selectedCoor.getX(), selectedCoor.getY());
                data.setNode(circle);
                pointsToAdd.add(data);
            }
        }
        series.getData().addAll(pointsToAdd);
        System.out.println(selectedCoor);
    }

    private void showError(AlertType type, Window owner, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.show();
    }

}

