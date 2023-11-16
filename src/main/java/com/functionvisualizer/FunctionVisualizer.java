package com.functionvisualizer;

import com.functionvisualizer.attributs.Coordinate;
import com.functionvisualizer.attributs.Line;
import com.functionvisualizer.functions.LinearFunction;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.gillius.jfxutils.chart.AxisConstraint;
import org.gillius.jfxutils.chart.AxisConstraintStrategy;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*******************************************************************************************
  ______                _   _              __      ___                 _ _
 |  ____|              | | (_)             \ \    / (_)               | (_)
 | |__ _   _ _ __   ___| |_ _  ___  _ __    \ \  / / _ ___ _   _  __ _| |_ _______ _ __
 |  __| | | | '_ \ / __| __| |/ _ \| '_ \    \ \/ / | / __| | | |/ _` | | |_  / _ \ '__|
 | |  | |_| | | | | (__| |_| | (_) | | | |    \  /  | \__ \ |_| | (_| | | |/ /  __/ |
 |_|   \__,_|_| |_|\___|\__|_|\___/|_| |_|     \/   |_|___/\__,_|\__,_|_|_/___\___|_|

 *******************************************************************************************/

public class FunctionVisualizer extends Application {
    public static XYChart.Series<Number, Number> SERIES;
    private LineChart<Number, Number> COORDINATE_SYSTEM;
    private NumberAxis X_AXIS;
    public static NumberAxis Y_AXIS;
    public static TableView<Coordinate> COORDINATE_TABLE;
    private Scene SCENE;
    public static HashMap<String, String> FUNCTIONS;
    public static MenuItem SELECTED_ITEM;
    public static double M;
    public static double B;
    public static double RANGE;
    private final Button createFunctionButton = new Button("Erstellen");
    private final Button backButton = new Button("Zurück");
    private final Button applyButton = new Button("Bestätigen");
    private final TextField mField = new TextField();
    private final TextField bField = new TextField();

    @Override
    public void start(Stage stage) throws Exception {
        functionVisualizerGUI(stage);
    }

    private void functionVisualizerGUI(Stage stage) {
        var root = new BorderPane();
        SCENE = new Scene(root, 1000, 600);
        SCENE.getStylesheets().add("stylesheet.css");

        // MenuBar erstellen, um verschiedene Berechnungen durchzuführen
        var bar = new MenuBar();
        var optionMenu = new Menu("Optionen");

        var caluclateLineareFunctionItem = new MenuItem("Lineare Funktion berechnen");
        var intersectionPointItem = new MenuItem("Schnittpunkt berechnen");
        var pointTestItem = new MenuItem("Punktprobe");

        optionMenu.getItems().addAll(caluclateLineareFunctionItem, intersectionPointItem, pointTestItem);
        bar.getMenus().add(optionMenu);

        caluclateLineareFunctionItem.setOnAction(e -> calculateFuncGUI());
        intersectionPointItem.setOnAction(e -> intersectionPointGUI(stage));
        pointTestItem.setOnAction(e -> pointTestGUI());

        // Erstellung des Koordinatensystems
        X_AXIS = new NumberAxis();
        X_AXIS.setLabel("x");
        X_AXIS.setAutoRanging(false);

        Y_AXIS = new NumberAxis();
        Y_AXIS.setLabel("y");
        Y_AXIS.setAutoRanging(false);

        SERIES = new XYChart.Series<>();
        COORDINATE_SYSTEM = new LineChart<>(X_AXIS, Y_AXIS);
        COORDINATE_SYSTEM.getData().add(SERIES);
        COORDINATE_SYSTEM.setAnimated(true);

        COORDINATE_SYSTEM.prefHeightProperty().bind(root.heightProperty());
        COORDINATE_SYSTEM.prefWidthProperty().bind(root.widthProperty());

        // TextField wird erstellt, um die Steigung einzutragen
        mField.setPromptText("Steigung m");

        var xField = new TextField();
        xField.setPromptText("Punkt x");

        bField.setPromptText("y-Achsenabschnitt");

        // Die Methode zoomIn() wird aufgerufen, um in das Koordinatensytem zu zoomen
        COORDINATE_SYSTEM.setOnMouseClicked(this::zoomIn);

        // Hier werden die Formelnamen mit den entsprechenden Formeln gespeichert
        final var functions = functionCollection();
        FUNCTIONS = functions;

        // SplitMenuButton wird erstellt, um zwischen den Funktionstypen asuzuwählen
        var functionTypButton = new SplitMenuButton();

        var formelArea = new Label("Formel: Noch keine ausgewählt!");

        // Button der die Funktion, mit den entsprechenden Daten, erstellt
        VBox.setMargin(createFunctionButton, new Insets(20));

        for (String func : functions.keySet()) {
            var menu = new MenuItem();
            menu.setText(func);
            functionTypButton.getItems().add(menu);

            menu.setOnAction(e -> handleMenuItemSelected(menu, functions, formelArea));
        }

        functionTypButton.setText("Funktionstyp");
        functionTypButton.setPrefWidth(150);
        BorderPane.setMargin(functionTypButton, new Insets(20));

        // Tabelle + Tabellenspalten werden erstellt, die die Inhalte von der Klasse Coordinate enthalten
        COORDINATE_TABLE = new TableView<>();
        COORDINATE_TABLE.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        var xColumn = new TableColumn<Coordinate, Double>("x");
        xColumn.setCellValueFactory(data -> data.getValue().xProperty());

        var yColumn = new TableColumn<Coordinate, Double>("y");
        yColumn.setCellValueFactory(data -> data.getValue().yProperty());
        COORDINATE_TABLE.getColumns().addAll(xColumn, yColumn);

        // Spinner wird erstellt, um die Länge des Graphen einzustellen
        var infoLabel = new Label("Wert für x:");

        var RANGE_SPINNER = new Spinner<Double>();
        RANGE_SPINNER.setEditable(true);

        var valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1, Integer.MAX_VALUE);
        RANGE_SPINNER.setValueFactory(valueFactory);

        COORDINATE_TABLE.setOnMouseClicked(e -> highlightPoints(COORDINATE_TABLE));

        var showCoordinates = new CheckBox("Zeige Koordinaten");

        /*
         * Button um das Optionenfenster zu öffnen
         */
        var showSettings = new Button();
        var view = new ImageView(new Image("com.functionvisualizer/img/menu_icon.png"));
        view.setFitHeight(18);
        showSettings.setGraphic(view);

        /* Hier werden die Nodes der VBox hinzugefügt und angezeigt */
        var settingsSide = new VBox();

        // Das Optionenfenster passt sich automatisch der Breite der Stage an
        stage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldNum, Number newNum) {
                if ((double)oldNum < stage.getWidth() || (double)oldNum > stage.getWidth())
                    settingsSide.setPrefWidth(newNum.doubleValue() / 3);
            }
        });

        settingsSide.setStyle("-fx-background-color: #F4F4F4");
        settingsSide.getChildren().addAll(
                functionTypButton,
                formelArea,
                mField,
                bField,
                infoLabel,
                RANGE_SPINNER,
                showCoordinates,
                createFunctionButton,
                COORDINATE_TABLE);

        var tip = new Tooltip();
        int[] count = {1};
        showCoordinates.setOnMouseClicked(e -> {
            try {
                if (count[0] == 1) {
                    handleCoordinatesSelected(tip);
                    count[0]--;
                } else {
                    tip.hide();
                    count[0]++;
                }

            } catch (AWTException ex) {
                throw new RuntimeException(ex);
            }
        });

        var coordinatePane = new Pane();
        coordinatePane.getChildren().add(COORDINATE_SYSTEM);

        // Erstelle eine Optionleiste oberhalb des Koordinatensystems
        var topBar = new HBox();
        HBox.setHgrow(bar, Priority.ALWAYS);
        HBox.setHgrow(showSettings, Priority.NEVER);
        topBar.getChildren().addAll(bar, showSettings);

        // Lässt entweder die settingssite einblenden oder ausblenden
        setTransition(settingsSide, showSettings, root);

        root.setTop(topBar);
        root.setCenter(coordinatePane);

        stage.setScene(SCENE);
        stage.setTitle("Function Visualisation");
        stage.show();
    }

    private HashMap<String, String> functionCollection() {
        var functions = new HashMap<String, String>();
        functions.put("proportionale Funktion", "f(x)=m*x");
        functions.put("lineare Funktion", "f(x)=m*x+b");
        functions.put("einfache quadratische Funktion", "f(x)=n*x²");

        return functions;
    }

    private void setTransition(Pane pane, Button button, BorderPane root) {
        var openTransition = new TranslateTransition(new Duration(350), pane);
        var closeTransition = new TranslateTransition(new Duration(350), pane);
        openTransition.setToX(0);

        final int[] count = {0};
        button.setOnMouseClicked(e -> {
            if (count[0] == 0) {
                root.setRight(pane);
                openTransition.play();
                count[0] = 1;
            } else {
                closeTransition.setToX(pane.getWidth()*2);
                closeTransition.play();
                count[0] = 0;
            }
        });
    }

    private void handleCoordinatesSelected(Tooltip coordinates) throws AWTException {
        final double COOR_SYSTEM_WIDTH = COORDINATE_SYSTEM.getWidth();
        final double COOR_SYSTEM_HEIGHT = COORDINATE_SYSTEM.getHeight();
        coordinates.show(COORDINATE_SYSTEM.getScene().getWindow());
        COORDINATE_SYSTEM.setOnMouseMoved(e -> {
            double mousePosX = e.getX();
            double mousePosY = e.getY();
            if (e.getX() <= COOR_SYSTEM_WIDTH && e.getY() <= COOR_SYSTEM_HEIGHT) {
                coordinates.setX(mousePosX);
                coordinates.setY(mousePosY);

                var mousePoint = new Point2D(e.getSceneX(), e.getSceneY());
                double x = X_AXIS.sceneToLocal(mousePoint).getX();
                double y = Y_AXIS.sceneToLocal(mousePoint).getY();

                double xValue = (double) X_AXIS.getValueForDisplay(x);
                double yValue = (double) Y_AXIS.getValueForDisplay(y);
                coordinates.setText("( " + xValue + " | " + yValue + " )");
            }
        });
    }

    private void handleMenuItemSelected(MenuItem menuItem, HashMap<String, String> map, Label formelArea) {
        SELECTED_ITEM = menuItem;
        formelArea.setText("Formel: " + map.get(menuItem.getText()));
        createFunctionButton.setOnMouseClicked(e -> handleCoordinateSystemBound());
    }

    private void handleCoordinateSystemBound() {
        try{
            setCoordinateSystemBound();
            CalculationThread thread = new CalculationThread();
            thread.start();
        } catch (NumberFormatException e) {
            handleFormatException(e);
        }
    }

    private void setCoordinateSystemBound() throws NumberFormatException {
        final int bound = 10;
        RANGE = 100;

        if (bField.getText().isEmpty()) {
            bField.setText("0");
        }
        B = Double.parseDouble(bField.getText());
        M = Double.parseDouble(mField.getText());

        // Die max Länge der Achsen am Anfang des Programmes
        X_AXIS.setAutoRanging(false);
        Y_AXIS.setAutoRanging(false);
        X_AXIS.setLowerBound(-bound);
        X_AXIS.setUpperBound(bound);
        Y_AXIS.setLowerBound(-bound);
        Y_AXIS.setUpperBound(bound);

        // Koordinaten werden hinzugefügt oder enfernt
        if (SERIES.getData().size() > 1) {
            XYChart.Data<Number, Number> lastData = SERIES.getData().get(SERIES.getData().size() - 1);
            if (lastData.getXValue() != null || !(Double.isNaN((double) lastData.getXValue()))) {
                if ((double)lastData.getXValue() < X_AXIS.getUpperBound() && (double)lastData.getXValue() > 200) {
                    SERIES.getData().remove(0, 70);
                } else {
                    RANGE += 10;
                }
            } else {
                System.out.println("Warummm???");
            }
        }

        double step = 1;
        double startX = X_AXIS.getLowerBound();
        double endX = X_AXIS.getUpperBound();
        for (double x = startX; x <= endX; x += step) {
            double y = M * x + B;
            if (y >= Y_AXIS.getLowerBound() && y <= Y_AXIS.getUpperBound() && x >= X_AXIS.getLowerBound() && x <= X_AXIS.getUpperBound()) {
                var newData = new XYChart.Data<Number, Number>(x, y);
                SERIES.getData().add(newData);
            }
        }
    }

    private void handleFormatException(Exception e) {
        var owner = createFunctionButton.getScene().getWindow();
        showError(AlertType.ERROR, owner, "Error: " + e, "Die angegebenen Zahlen konnten nicht formatiert werden. " +
                "Bitte überprüfen Sie die Zahlen.");
    }

    private void zoomIn(MouseEvent e) {
        // Das Dependency jfxutils, macht das der LineChart verändert werden kann
        var panner = new ChartPanManager(COORDINATE_SYSTEM);
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
            if (mouseEvent.getButton() != MouseButton.SECONDARY) {// Zoomen wird getriggert, wenn man die secondäre Maustaste gedrückt hat
                mouseEvent.consume();
            } else {
                double x = getFocusedCoordinate().getX();
                if (X_AXIS.getUpperBound() >= x) {
                    handleCoordinateSystemBound();
                    mouseEvent.consume();
                }
            }
        });
    }

    private void calculateFuncGUI() {
        // UI
        var stage = new Stage();

        var pane = new GridPane();
        var box = new VBox();
        SCENE = new Scene(box);

        var visualizeButton = new Button("Visualisieren");

        var formelArea = new TextArea();
        formelArea.setPromptText("Funktionsgleichung: ");
        formelArea.setWrapText(true);

        pane.add(backButton, 0, 0);
        pane.add(applyButton, 0, 3);
        pane.add(formelArea, 1, 4);
        box.getChildren().addAll(pane, formelArea, visualizeButton);

        var inset = new Insets(10);
        VBox.setMargin(formelArea, inset);
        VBox.setMargin(visualizeButton, inset);
        VBox.setMargin(pane, inset);

        // Erstellung der Textfelder, um die Daten des Benutzers in die Coordinate Klasse zu integrieren
        String[] fields = {"x-Koordinate", "y-Koordinate"};
        var textFields = new ArrayList<TextField>();

        makeGrid(fields, textFields, pane, 0, true);

        // Speichert die Funktionsgleichung
        final String[] func = new String[1];
        applyButton.setOnAction(e -> {
            func[0] = visualizeCalculatedFunc(textFields, visualizeButton, stage);
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

    private String visualizeCalculatedFunc(List<TextField> textFields, Button button1, Stage stage) {
        var doubles = new ArrayList<Double>();
        var function = new LinearFunction();
        try {
            for (TextField tf : textFields) {
                double num = Double.parseDouble(tf.getText());
                doubles.add(num);

                if (doubles.size() >= 4) {
                    var coordinate1 = new Coordinate(doubles.get(0), doubles.get(1));
                    var coordinate2 = new Coordinate(doubles.get(2), doubles.get(3));

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
        var pointsToAdd = new ArrayList<XYChart.Data<Number, Number>>();
        var selectedCoor = getFocusedCoordinate();

        // Wenn mehrere Punkte markiert werden, werden diese wieder entfernt, damit immer nur einer angezeigt wird
        SERIES.getData().removeIf(data -> data.getNode() instanceof Circle);

        for (XYChart.Data<Number, Number> value : SERIES.getData()) {
            if (value.getXValue().equals(selectedCoor.getX()) && value.getYValue().equals(selectedCoor.getY())) {
                var circle = new Circle(5);
                circle.setFill(Color.BLACK);
                XYChart.Data<Number, Number> data = new XYChart.Data<>(selectedCoor.getX(), selectedCoor.getY());
                data.setNode(circle);
                pointsToAdd.add(data);
            }
        }
        SERIES.getData().addAll(pointsToAdd);
    }

    private Coordinate getFocusedCoordinate() {
        return COORDINATE_TABLE.getItems().get(COORDINATE_TABLE.focusModelProperty().get().getFocusedIndex());
    }

    private void intersectionPointGUI(Stage stage) {
        var pane = new GridPane();
        var box = new VBox();
        SCENE = new Scene(box);

        var func1Label = new Label("Funktion 1:");
        var func2Label = new Label("Funktion 2:");

        var intersectionArea = new TextArea();
        intersectionArea.setPromptText("Schnittpunkt:");

        var inset = new Insets(5);
        int col = 0;
        Node[] nodes = {backButton, func1Label, func2Label, applyButton};
        for (Node node : nodes) {
            GridPane.setMargin(node, inset);
            pane.add(node, 0, col);
            col += 1;
        }

        String[] prompts = {"Steigung m", "y-Achsenabschnitt b"};
        var textFields = new ArrayList<TextField>();
        makeGrid(prompts, textFields, pane, 1, true);

        VBox.setMargin(intersectionArea, inset);
        box.getChildren().addAll(pane, intersectionArea);

        applyButton.setOnAction(e -> parseInputToIntersectionPoint(textFields, intersectionArea));
        backButton.setOnAction(e -> functionVisualizerGUI(stage));

        stage.setScene(SCENE);
    }

    private void parseInputToIntersectionPoint(List<TextField> textFields, TextArea area) {
        // Konvertiert die Daten aus den Textfeldern, zu Integern und werden, als Werte für zwei lineare Funktionen genutzt
        var data = new ArrayList<Integer>();

        for (TextField tf : textFields) {
            String input = tf.getText();
            int inputInt = Integer.parseInt(input);

            data.add(inputInt);
        }
        var func1 = new LinearFunction(data.get(0), data.get(1));
        var func2 = new LinearFunction(data.get(2), data.get(3));
        var intersectionPoint = Line.INTERSECT(func1, func2);

        area.setText("SP( " + intersectionPoint.getX() + " | " + intersectionPoint.getY() + " )");
    }

    private void pointTestGUI() {
        var stage = new Stage();
        var box = new VBox();
        var scene = new Scene(box, 300, 250);

        var coorField = new TextField();
        coorField.setPromptText("Format: x;y");
        coorField.setFocusTraversable(false);

        var funcField = new TextField();
        funcField.setPromptText("Funktion");

        var resultTxt = "Auswertung: ";
        var checkLabel = new Label(resultTxt);

        var linearFunction = new LinearFunction(10, 4);
        applyButton.setOnAction(e -> {
            var coordinate = new Coordinate();
            List<Double> coordinates = parseToInteger(coorField.getText());
            coordinate.setX(coordinates.get(0));
            coordinate.setY(coordinates.get(1));


            checkLabel.setText(returnTestSteps(coordinate, linearFunction));
        });

        box.getChildren().addAll(coorField, applyButton, checkLabel);

        stage.setScene(scene);
        stage.show();
    }

    public List<Double> parseToInteger(String input) {
        var coorValue = new ArrayList<Double>();
        String[] parts = input.split(";");

        try {
            double num1 = Integer.parseInt(parts[0]);
            double num2 = Integer.parseInt(parts[1]);

            coorValue.add(num1);
            coorValue.add(num2);
        } catch (NumberFormatException e) {
            handleFormatException(e);
        }
        return coorValue;
    }

    private String returnTestSteps(Coordinate coor, LinearFunction func) {
        double y = coor.getY();
        return Line.POINT_TEST(coor, func) ? "Auswertung: ✅\n" + Line.GET_POINT_TEST_STEPS() + "\n\t⇒" + y + " = " + Line.Y
                : "Auswertung: ❌\n" + Line.GET_POINT_TEST_STEPS() + "\n\t⇒" + y + " ≠ " + Line.Y;
    }

    private void showError(AlertType type, Window owner, String title, String message) {
        var alert = new Alert(type);
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