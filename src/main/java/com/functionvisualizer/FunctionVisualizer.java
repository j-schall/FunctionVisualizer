package com.functionvisualizer;

import com.functionvisualizer.attributs.Coordinate;
import com.functionvisualizer.attributs.Line;
import com.functionvisualizer.functions.LinearFunction;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
    public static XYChart.Series<Number, Number> SERIES;
    private LineChart<Number, Number> COORDINATE_SYSTEM;
    private NumberAxis X_AXIS;
    private NumberAxis Y_AXIS;
    private Spinner<Double> RANGE_SPINNER;
    public static TableView<Coordinate> COORDINATE_TABLE;
    private Scene SCENE;
    public static int FUNCTION_INDEX;
    public static double M;
    public static double B;
    public static double RANGE;
    private static final Button createFunctionButton = new Button("Erstellen");
    private final Button backButton = new Button("Zurück");
    private final Button applyButton = new Button("Bestätigen");

    @Override
    public void start(Stage stage) {
        functionVisualizerGUI(stage);
    }

    private void functionVisualizerGUI(Stage stage) {
        BorderPane root = new BorderPane();
        SCENE = new Scene(root, 840, 450);

        // MenuBar erstellen, um verschiedene Berechnungen durchzuführen
        MenuBar bar = new MenuBar();
        Menu optionMenu = new Menu("Optionen");

        MenuItem caluclateLineareFunctionItem = new MenuItem("Lineare Funktion berechnen");
        MenuItem intersectionPointItem = new MenuItem("Schnittpunkt berechnen");

        optionMenu.getItems().addAll(caluclateLineareFunctionItem, intersectionPointItem);
        bar.getMenus().add(optionMenu);

        caluclateLineareFunctionItem.setOnAction(e -> calculateFuncGUI(stage));
        intersectionPointItem.setOnAction(e -> intersectionPointGUI(stage));

        // Erstellung des Koordinatensystems
        X_AXIS = new NumberAxis();
        X_AXIS.setLabel("x");
        X_AXIS.setAutoRanging(false);

        Y_AXIS = new NumberAxis();
        Y_AXIS.setLabel("y");
        Y_AXIS.setUpperBound(100);
        Y_AXIS.setLowerBound(100);

        SERIES = new XYChart.Series<>();
        SERIES.setName("Punkte");
        COORDINATE_SYSTEM = new LineChart<>(X_AXIS, Y_AXIS);
        COORDINATE_SYSTEM.getData().add(SERIES);
        COORDINATE_SYSTEM.autosize();

        // TextField wird erstellt, um die Steigung einzutragen
        var mField = new TextField();
        mField.setPromptText("Steigung m");

        var xField = new TextField();
        xField.setPromptText("Punkt x");

        var bField = new TextField();
        bField.setPromptText("y-Achsenabschnitt");
        bField.setDisable(true);

        // Die Methode zoomIn() wird aufgerufen, um in das Koordinatensytem zu zoomen
        COORDINATE_SYSTEM.setOnMouseClicked(this::zoomIn);

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

            menu.setOnAction(e -> handleMenuItemSelected(menu, funktionen, formelArea, mField, bField));
        }

        functionTypButton.setText("Funktionstyp");
        functionTypButton.setPrefWidth(150);
        BorderPane.setMargin(functionTypButton, new Insets(20));

        // Tabelle + Tabellenspalten werden erstellt, die die Inhalte von der Klasse Coordinate enthalten
        COORDINATE_TABLE = new TableView<>();
        COORDINATE_TABLE.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Coordinate, Double> xColumn = new TableColumn<>("x");
        xColumn.setCellValueFactory(data -> data.getValue().xProperty());

        TableColumn<Coordinate, Double> yColumn = new TableColumn<>("y");
        yColumn.setCellValueFactory(data -> data.getValue().yProperty());
        COORDINATE_TABLE.getColumns().addAll(xColumn, yColumn);

        // Spinner wird erstellt, um die Länge des Graphen einzustellen
        var infoLabel = new Label("Wert für x:");

        RANGE_SPINNER = new Spinner<>();
        RANGE_SPINNER.setEditable(true);

        var valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1, Integer.MAX_VALUE);
        RANGE_SPINNER.setValueFactory(valueFactory);

        COORDINATE_TABLE.setOnMouseClicked(e -> highlightPoints(COORDINATE_TABLE));

        /* Hier werden die Nodes der VBox hinzugefügt und angezeigt */
        VBox settingsSide = new VBox();
        settingsSide.getChildren().addAll(
                functionTypButton,
                formelArea,
                mField,
                bField,
                infoLabel,
                RANGE_SPINNER,
                createFunctionButton,
                COORDINATE_TABLE);

        root.setTop(bar);
        root.setLeft(COORDINATE_SYSTEM);
        root.setRight(settingsSide);

        stage.setScene(SCENE);
        stage.setTitle("Function Visualisation");
        stage.show();
    }

    private void handleMenuItemSelected(MenuItem menuItem, HashMap<String, String> map, Label formelArea, TextField mField, TextField bField) {
        String selectedItem = menuItem.getText();
        formelArea.setText("Formel: " + map.get(selectedItem));

        boolean linFuncSel = selectedItem.equals(map.keySet().toArray()[2]);
        boolean propFuncSel = selectedItem.equals(map.keySet().toArray()[1]);
        boolean quaFuncSel = selectedItem.equals(map.keySet().toArray()[0]);

        if (linFuncSel) {
            Platform.runLater(() -> bField.setDisable(false));
        } else {
            Platform.runLater(() -> bField.setDisable(true));
        }

        createFunctionButton.setOnMouseClicked(e -> {
            try {
                if (propFuncSel) {
                    setCoordinateSystemBound(mField, 1);
                } else if (linFuncSel) {
                    setCoordinateSystemBound(mField, 2, Double.parseDouble(bField.getText()));
                } else if (quaFuncSel) {
                    setCoordinateSystemBound(mField, 3);
                }
            } catch (NumberFormatException ex) {
                handleFormatException(ex);
            }
        });
    }

    private void setCoordinateSystemBound(TextField mField, int index, double... bValue) throws NumberFormatException {
        RANGE = RANGE_SPINNER.getValue();

        // Überprüft, ob es sich um eine quadratische Funktion handelt, sodass statt Steigung m n benutzt wird
        if (index == 2) B = bValue[0];

        M = Double.parseDouble(mField.getText());

        X_AXIS.setLowerBound(-RANGE);
        X_AXIS.setUpperBound(RANGE);
        Y_AXIS.setLowerBound(-RANGE);
        Y_AXIS.setUpperBound(RANGE);

        FUNCTION_INDEX = index;
        CalculationThread thread = new CalculationThread();
        thread.start();
    }

    private void handleFormatException(Exception e) {
        Window owner = createFunctionButton.getScene().getWindow();
        showError(AlertType.ERROR, owner, "Error: " + e, "Die angegebenen Zahlen konnten nicht formatiert werden. " +
                "Bitte überprüfen Sie die Zahlen.");
    }

    private void zoomIn(MouseEvent e) {
        // Das Dependency jfxutils, macht das der LineChart verändert werden kann
        ChartPanManager panner = new ChartPanManager(COORDINATE_SYSTEM);
        //Wenn der rechte Mousebutton gedrückt wurde, verschiebt man das Sichtfeld des Koordinatensytsems
        panner.setMouseFilter(mouseEvent -> {
            boolean isPressed = mouseEvent.getButton() == MouseButton.PRIMARY || mouseEvent.getButton() == MouseButton.SECONDARY;
            if (!isPressed) {
                mouseEvent.consume();
            }
        });

        AxisConstraintStrategy strategies = chartInputContext -> AxisConstraint.Both;
        panner.setAxisConstraintStrategy(strategies);
        panner.start();

        // Wenn man die rechte Maustaste gedrückt hält, wird ein Rechteck gezeichnet, um an die gewünschte Stelle zu zoomen
        JFXChartUtil.setupZooming(COORDINATE_SYSTEM, mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.SECONDARY) // Zoomen wird getriggert, wenn man die secondäre Maustaste gedrückt hat
                mouseEvent.consume();
        });
    }

    private void calculateFuncGUI(Stage stage) {
        // UI
        GridPane pane = new GridPane();
        VBox box = new VBox();
        SCENE = new Scene(box);

        Button visualizeButton = new Button("Visualisieren");

        TextArea formelArea = new TextArea();
        formelArea.setPromptText("Funktionsgleichung: ");
        formelArea.setWrapText(true);

        pane.add(backButton, 0, 0);
        pane.add(applyButton, 0, 3);
        pane.add(formelArea, 1, 4);
        box.getChildren().addAll(pane, formelArea, visualizeButton);

        Insets inset = new Insets(10);
        VBox.setMargin(formelArea, inset);
        VBox.setMargin(visualizeButton, inset);
        VBox.setMargin(pane, inset);

        // Erstellung der Textfelder, um die Daten des Benutzers in die Coordinate Klasse zu integrieren
        String[] fields = {"x-Koordinate", "y-Koordinate"};
        List<TextField> textFields = new ArrayList<>();

        makeGrid(fields, textFields, pane, 0, true);

        // Speichert die Funktionsgleichung
        final String[] func = new String[1];
        applyButton.setOnAction(e -> {
            func[0] = visualizeCalculatedFunc(textFields, applyButton, visualizeButton, stage);
            formelArea.setText(func[0]);
        });

        backButton.setOnAction(e -> functionVisualizerGUI(stage));
        stage.setScene(SCENE);
    }

    // Erstellt automatisch ein Raster aus TextFeldern
    private void makeGrid(Object[] o, List<TextField> fields, GridPane pane, int rowCount, boolean promptText) {
        int l = o.length;

        for (int row = rowCount; row < l + rowCount; row++) {
            for (int col = 1; col < l + 1; col++) {
                TextField field = new TextField();
                if (promptText) {
                    field.setPromptText(o[col / 2].toString());
                }
                pane.add(field, row, col);
                fields.add(field);
            }
        }
    }

    private String visualizeCalculatedFunc(List<TextField> textFields, Button button, Button button1, Stage stage) {
        List<Double> doubles = new ArrayList<>();
        LinearFunction function = new LinearFunction();
        try {
            for (TextField tf : textFields) {
                double num = Double.parseDouble(tf.getText());
                doubles.add(num);

                if (doubles.size() >= 4) {
                    Coordinate coordinate1 = new Coordinate(doubles.get(0), doubles.get(1));
                    Coordinate coordinate2 = new Coordinate(doubles.get(2), doubles.get(3));

                    // Die Funktion, welche berechnet wurde, wird in dem Koordinatensystem veranschaulicht
                    button1.setOnAction(e -> {
                        double m = function.getM();
                        double b = function.getB();

                        functionVisualizerGUI(stage);
                        function.create(m, b, coordinate1.getX(), COORDINATE_TABLE, SERIES);
                    });

                    return function.calculateLineareFunction(coordinate1, coordinate2);
                }
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            handleFormatException(ex);
        }
        return null;
    }

    private void highlightPoints(TableView<Coordinate> table) {
        // Schreibe Code, sodass wenn selectedCoor in der Tabelle ausgewählt wurde, dass dieser Punkt im Koordinatensystem markiert wird
        List<XYChart.Data<Number, Number>> pointsToAdd = new ArrayList<>();
        Coordinate selectedCoor = table.getItems().get(table.focusModelProperty().get().getFocusedIndex());

        // Wenn mehrere Punkte markiert werden, werden diese wieder entfernt, damit immer nur einer angezeigt wird
        SERIES.getData().removeIf(data -> data.getNode() instanceof Circle);

        for (XYChart.Data<Number, Number> value : SERIES.getData()) {
            if (value.getXValue().equals(selectedCoor.getX()) && value.getYValue().equals(selectedCoor.getY())) {
                Circle circle = new Circle(5);
                circle.setFill(Color.BLACK);
                XYChart.Data<Number, Number> data = new XYChart.Data<>(selectedCoor.getX(), selectedCoor.getY());
                data.setNode(circle);
                pointsToAdd.add(data);
            }
        }
        SERIES.getData().addAll(pointsToAdd);
    }

    private void intersectionPointGUI(Stage stage) {
        GridPane pane = new GridPane();
        VBox box = new VBox();
        SCENE = new Scene(box);

        Label func1Label = new Label("Funktion 1:");
        Label func2Label = new Label("Funktion 2:");

        TextArea intersectionArea = new TextArea();
        intersectionArea.setPromptText("Schnittpunkt:");

        Insets inset = new Insets(5);
        int col = 0;
        Node[] nodes = {backButton, func1Label, func2Label, applyButton};
        for (Node node : nodes) {
            GridPane.setMargin(node, inset);
            pane.add(node, 0, col);
            col += 1;
        }

        String[] prompts = {"Steigung m", "y-Achsenabschnitt b"};
        List<TextField> textFields = new ArrayList<>();
        makeGrid(prompts, textFields, pane, 1,true);

        VBox.setMargin(intersectionArea, inset);
        box.getChildren().addAll(pane, intersectionArea);

        applyButton.setOnAction(e -> parseInputToIntersectionPoint(textFields, intersectionArea));
        backButton.setOnAction(e -> functionVisualizerGUI(stage));

        stage.setScene(SCENE);
    }

    private void parseInputToIntersectionPoint(List<TextField> textFields, TextArea area) {
        // Konvertiert die Daten aus den Textfeldern, zu Integern und werden, als Werte für zwei lineare Funktionen genutzt
        List<Integer> data = new ArrayList<>();

        for (TextField tf : textFields) {
            String input = tf.getText();
            int inputInt = Integer.parseInt(input);

            data.add(inputInt);
        }
        LinearFunction func1 = new LinearFunction(data.get(0), data.get(1));
        LinearFunction func2 = new LinearFunction(data.get(2), data.get(3));
        Coordinate intersectionPoint = Line.INTERSECT(func1, func2);

        area.setText("SP( " + intersectionPoint.getX() + " | " + intersectionPoint.getY() + " )");
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