/*******************************************************************************************
 ______                _   _              __      ___                 _ _
 |  ____|              | | (_)             \ \    / (_)               | (_)
 | |__ _   _ _ __   ___| |_ _  ___  _ __    \ \  / / _ ___ _   _  __ _| |_ _______ _ __
 |  __| | | | '_ \ / __| __| |/ _ \| '_ \    \ \/ / | / __| | | |/ _` | | |_  / _ \ '__|
 | |  | |_| | | | | (__| |_| | (_) | | | |    \  /  | \__ \ |_| | (_| | | |/ /  __/ |
 |_|   \__,_|_| |_|\___|\__|_|\___/|_| |_|     \/   |_|___/\__,_|\__,_|_|_/___\___|_|

 @author Jan Schall
 *******************************************************************************************/

package com.functionvisualizer;

import com.functionvisualizer.attributs.Coordinate;
import com.functionvisualizer.attributs.Line;
import com.functionvisualizer.functions.LinearFunction;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionVisualizer extends Application {
    public static XYChart.Series<Number, Number> SERIES;
    private LineChart<Number, Number> COORDINATE_SYSTEM;
    private NumberAxis X_AXIS;
    public static NumberAxis Y_AXIS;
    public static TableView<Coordinate> COORDINATE_TABLE;
    private Scene SCENE;
    public static HashMap<String, String> FUNCTIONS;
    public static double M = 0;
    public static double B = 0;
    public static double RANGE;
    private final Button createFunctionButton = new Button("Create");
    private final Button submitButton = new Button("Submit");
    private final TextField functionField = new TextField();
    private final TextField bField = new TextField();
    private final Menu optionMenu = new Menu("Options");
    public static String SELECTED_FUNC;
    private Stage mainStage;

    public final String PROPORTIONAL_FUNCTION = "proportional Function";
    public final String LINEAR_FUNCTION = "linear Function";
    public final String QUADRATIC_FUNCTION = "quadratic Function";
    public final String SPF_QUADRATIC_FUNCTION = "SPF-Quadratic-Function";


    @Override
    public void start(Stage stage) {
        functionVisualizerGUI(stage);
    }

    private void functionVisualizerGUI(Stage stage) {
        mainStage = stage;
        mainStage.setMaximized(true);
        mainStage.setMinHeight(600);
        mainStage.setMinWidth(1000);

        var root = new BorderPane();
        SCENE = new Scene(root);

        // MenuBar erstellen, um verschiedene Berechnungen durchzuführen
        var bar = new MenuBar();

        var caluclateLineareFunctionItem = new MenuItem("Calculate linear function");
        var intersectionPointItem = new MenuItem("calculate intersection points");

        optionMenu.getItems().addAll(caluclateLineareFunctionItem, intersectionPointItem);
        bar.getMenus().add(optionMenu);

        caluclateLineareFunctionItem.setOnAction(e -> calculateFuncGUI());
        intersectionPointItem.setOnAction(e -> intersectionPointGUI());

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
        functionField.setPromptText("Funktion");

        var xField = new TextField();
        xField.setPromptText("Point x");

        bField.setPromptText("y-axes intersection point");

        // Die Methode zoomIn() wird aufgerufen, um in das Koordinatensytem zu zoomen
        COORDINATE_SYSTEM.setOnScroll(this::zoom);

        // Button der die Funktion, mit den entsprechenden Daten, erstellt
        VBox.setMargin(createFunctionButton, new Insets(20));

        createFunctionButton.setOnAction(e -> {
            Window errorWindow = createFunctionButton.getScene().getWindow();
            SELECTED_FUNC = identifyFunctionType(functionField.getText(), errorWindow);
            handleCoordinateSystemBound(100);
        });

        // Tabelle + Tabellenspalten werden erstellt, die die Inhalte von der Klasse Coordinate enthalten
        COORDINATE_TABLE = new TableView<>();
        COORDINATE_TABLE.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        var xColumn = new TableColumn<Coordinate, Double>("x");
        xColumn.setCellValueFactory(data -> data.getValue().xProperty());

        var yColumn = new TableColumn<Coordinate, Double>("y");
        yColumn.setCellValueFactory(data -> data.getValue().yProperty());
        COORDINATE_TABLE.getColumns().addAll(xColumn, yColumn);

        COORDINATE_TABLE.setOnMouseClicked(e -> highlightPoints());

        var showCoordinates = new CheckBox("Show coordinates");


        // Button um das Optionenfenster zu öffnen
        var showSettings = new Button();
        var view = new ImageView(new Image("com.functionvisualizer/img/menu_icon.png"));
        view.setFitHeight(18);
        showSettings.setGraphic(view);

        // Hier werden die Nodes der VBox hinzugefügt und angezeigt
        var settingsSide = new VBox();

        // Das Optionenfenster passt sich automatisch der Breite der Stage an
        stage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldNum, Number newNum) {
                if ((double) oldNum < stage.getWidth() || (double) oldNum > stage.getWidth())
                    settingsSide.setPrefWidth(newNum.doubleValue() / 3);
            }
        });

        FUNCTIONS = functionCollection();

        // Die TableView wird je nach Höhe der settingsSide angepasst
        settingsSide.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldNum, Number newNum) {
                if ((double) oldNum < settingsSide.getHeight())
                    COORDINATE_TABLE.setPrefHeight(newNum.doubleValue());
            }
        });

        settingsSide.setStyle("-fx-background-color: #F4F4F4");
        settingsSide.getChildren().addAll(
                functionField,
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

            } catch (AWTException ignored) {
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
        functions.put(PROPORTIONAL_FUNCTION, "f(x)=m*x");
        functions.put(LINEAR_FUNCTION, "f(x)=m*x+b");
        functions.put(QUADRATIC_FUNCTION, "f(x)=a*x^2");
        functions.put(SPF_QUADRATIC_FUNCTION, "f(x)=a*(x-d)^2+e");
        return functions;
    }

    private String identifyFunctionType(String input, Window errorWindow) {
        // Remove any leading or trailing whitespaces
        if (input == null) return null;
        input = input.trim();

        // Check if the input starts with "f(x)=" or other letters
        Pattern functionExpression = Pattern.compile("[a-z]+\\([a-z]+\\)=");
        Matcher expressionMatcher = functionExpression.matcher(input);
        if (expressionMatcher.find()) {
            String functionBody = input.substring(5);

            // Patterns to get the right results, when parsing them to the specific function class
            Pattern linearPattern = Pattern.compile("^([+-]?\\d*\\.?\\d*)\\*?x([+-]?\\d*\\.?\\d*)$"); // f(x)=m*x+b
            Pattern quadraticPattern = Pattern.compile("^([+-]?\\d*\\.?\\d*)\\*?x\\^2$"); // f(x)=a+x^2
            Pattern bPattern = Pattern.compile("([-]?\\d+)+[\\D]+([-]?\\d+)?"); // find m and b in a function and parse them
            Matcher matcher = bPattern.matcher(functionBody);

            if (matcher.matches()) {
                if (functionBody.matches(linearPattern.pattern())) {
                    M = Double.parseDouble(matcher.group(1));
                    // If b (in matcher.group(2)) is NOT null, it's a linear function, else it's a proportional function
                    if (matcher.group(2) != null) {
                        B = Double.parseDouble(matcher.group(2));
                        return LINEAR_FUNCTION;
                    } else {
                        return PROPORTIONAL_FUNCTION;
                    }
                } else if (functionBody.matches(quadraticPattern.pattern())) {
                    M = Double.parseDouble(matcher.group(1));
                    return QUADRATIC_FUNCTION;
                }
            }
        } else {
            showError(AlertType.ERROR, errorWindow, "", "Invalid input. Please check your function and retry");
        }
        return null;
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
                closeTransition.setToX(pane.getWidth() * 2);
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
                if (coordinates.getX() < SCENE.getWidth() && coordinates.getY() < SCENE.getHeight()) {
                    coordinates.setAnchorX(mousePosX);
                    coordinates.setAnchorY(mousePosY);

                    // Koordinaten der Mouse auf dem Koordinatensystem werden angezeigt
                    Point2D position = mousePosition(e);
                    coordinates.setText("( " + position.getX() + " | " + position.getY() + " )");
                }
            }
        });
    }

    private Point2D mousePosition(MouseEvent e) {
        var mousePoint = new Point2D(e.getSceneX(), e.getSceneY());
        double x = X_AXIS.sceneToLocal(mousePoint).getX();
        double y = Y_AXIS.sceneToLocal(mousePoint).getY();

        double xValue = (double) X_AXIS.getValueForDisplay(x);
        double yValue = (double) Y_AXIS.getValueForDisplay(y);
        return new Point2D(xValue, yValue);
    }

    private void handleCoordinateSystemBound(int range) {
        try {
            setCoordinateSystemBound((int) (RANGE + range));
            CalculationThread thread = new CalculationThread();
            thread.start();
        } catch (NumberFormatException e) {
            handleFormatException(e);
        }
    }

    private void setCoordinateSystemBound(int range) throws NumberFormatException {
        final int bound = 10;
        final int MAX_DATA_SIZE = 60;

        RANGE = range;
        // Die max Länge der Achsen am Anfang des Programmes
        X_AXIS.setAutoRanging(false);
        Y_AXIS.setAutoRanging(false);
        X_AXIS.setLowerBound(-bound);
        X_AXIS.setUpperBound(bound);
        Y_AXIS.setLowerBound(-bound);
        Y_AXIS.setUpperBound(bound);

        // Koordinaten werden je nachdem gelöscht, um die Performance zu verbessern
        if (SERIES.getData().size() > 1) {
            XYChart.Data<Number, Number> lastData = SERIES.getData().get(SERIES.getData().size() - 1);
            if (lastData.getXValue() != null || !(Double.isNaN((double) lastData.getXValue()))) {
                double dataSize = SERIES.getData().size();
                if (dataSize > MAX_DATA_SIZE) {
                    Platform.runLater(() -> {
                        SERIES.getData().remove(0, SERIES.getData().size() / 5);
                        COORDINATE_TABLE.getItems().remove(0, SERIES.getData().size() / 5);
                    });
                }
            }
        }
    }

    private void handleFormatException(Exception e) {
        var owner = createFunctionButton.getScene().getWindow();
        showError(AlertType.ERROR, owner, "Error: " + e, "Die angegebenen Zahlen konnten nicht formatiert werden. " +
                "Bitte überprüfen Sie die Zahlen.");
    }

    private void zoom(ScrollEvent e) {
        var panner = new ChartPanManager(COORDINATE_SYSTEM);
        panner.setMouseFilter(mouseEvent -> {
            boolean isPressed = mouseEvent.getButton() == MouseButton.PRIMARY || mouseEvent.getButton() == MouseButton.SECONDARY;
            if (!isPressed)
                mouseEvent.consume();
        });

        AxisConstraintStrategy strategies = chartInputContext -> AxisConstraint.Both;
        panner.setAxisConstraintStrategy(strategies);
        panner.start();

        JFXChartUtil.setupZooming(COORDINATE_SYSTEM, mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.SECONDARY) {
                mouseEvent.consume();

                if (!COORDINATE_TABLE.getItems().isEmpty()) {
                    double x = getFocusedCoordinate().getX();

                    if (X_AXIS.getUpperBound() >= x) {

                        COORDINATE_SYSTEM.setOnMouseMoved(mouseMovedEv -> {
                            // Wenn die Mausposition größer als der größte (letzte) Punkt auf dem Graph ist,
                            // erhöht sich die Range um den Abstand von dem Ursprung zur Mausposition
                            double lastY = SERIES.getData().get(SERIES.getData().size() - 1).getYValue().doubleValue();
                            double lastX = SERIES.getData().get(SERIES.getData().size() - 1).getXValue().doubleValue();

                            if (lastY <= mousePosition(mouseMovedEv).getY() ||
                                    SERIES.getData().get(0).getYValue().doubleValue() > mousePosition(mouseMovedEv).getY()) {

                                Coordinate lastPoint = new Coordinate(lastX, lastY);

                                Point2D mousePos = mousePosition(mouseMovedEv);
                                if (mousePos.getX() > lastPoint.getX()) {
                                    handleCoordinateSystemBound((int) distanceToMouse(mousePos));
                                    navigateToPoint(lastPoint);
                                }
                            }
                        });
                        mouseEvent.consume();
                    }
                }
            }
        });
    }

    public double distanceToMouse(Point2D mousePos) {
        // Satz des Pythagoras um den Vektor zwischen dem Ursprung und der Mausposition zu berechnen
        double xPow = Math.pow(mousePos.getX(), 2);
        double yPow = Math.pow(mousePos.getY(), 2);

        double sum = xPow + yPow;
        return Math.sqrt(sum);
    }

    private void calculateFuncGUI() {
        // UI
        var stage = new Stage();

        var pane = new GridPane();
        var box = new VBox();
        var scene = new Scene(box);

        var visualizeButton = new Button("Visualize");

        var formelArea = new TextArea();
        formelArea.setPromptText("Function equation: ");
        formelArea.setWrapText(true);

        var inset = new Insets(10);
        VBox.setMargin(formelArea, inset);
        VBox.setMargin(visualizeButton, inset);
        VBox.setMargin(pane, inset);

        // Erstellung der Textfelder, um die Daten des Benutzers in die Coordinate Klasse zu integrieren
        String[] fields = {"x-coordinate", "y-coordinate"};
        var textFields = new ArrayList<TextField>();

        makeGrid(fields, textFields, pane, 0, true);

        // Speichert die Funktionsgleichung
        final String[] func = new String[1];

        submitButton.setOnAction(e -> {
            try {
                func[0] = visualizeCalculatedFunc(textFields, visualizeButton, mainStage);
            } catch (NumberFormatException ex) {    formelArea.setText(func[0]);

                showError(AlertType.ERROR, submitButton.getScene().getWindow(), ex.getMessage(), "Die angegebenen Zahlen konnten nicht formatiert werden. " +
                        "Überprüfe deine Eingabe");
            }
        });

        pane.add(submitButton, 0, 3);
        pane.add(formelArea, 1, 4);
        box.getChildren().addAll(pane, formelArea, visualizeButton);

        stage.setScene(scene);
        stage.initOwner(mainStage);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Calculate Linear Function");
        stage.show();
    }

    // Erstellt automatisch ein Raster aus TextFeldern
    private void makeGrid(Object[] o, List<TextField> fields, GridPane pane, int rowCount, boolean promptText) {
        int l = o.length;

        for (int row = rowCount; row < l + rowCount; row++) {
            for (int col = 1; col < l + 1; col++) {
                TextField field = new TextField();
                if (promptText) field.setPromptText(o[col / 2].toString());
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
                    Window errorWindow = submitButton.getScene().getWindow();
                    button1.setOnAction(e -> {
                        functionVisualizerGUI(stage);
                        identifyFunctionType(function.toString(), errorWindow);
                    });

                    return function.calculateLineareFunction(coordinate1, coordinate2);
                }
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            handleFormatException(ex);
        }
        return null;
    }

    private void highlightPoints() {
        // Schreibe Code, sodass wenn selectedCoor in der Tabelle ausgewählt wurde, dass dieser Punkt im Koordinatensystem markiert wird
        var pointsToAdd = new ArrayList<XYChart.Data<Number, Number>>();

        if (!COORDINATE_TABLE.getItems().isEmpty()) {
            var selectedCoor = getFocusedCoordinate();

            // Wenn mehrere Punkte markiert werden, werden diese wieder entfernt, damit immer nur einer angezeigt wird
            SERIES.getData().removeIf(data -> data.getNode() instanceof Circle);
            if (!SERIES.getData().isEmpty()) {
                var circle = new Circle(5);
                circle.setFill(Color.BLACK);
                XYChart.Data<Number, Number> data = new XYChart.Data<>(selectedCoor.getX(), selectedCoor.getY());
                data.setNode(circle);
                pointsToAdd.add(data);
            }
            SERIES.getData().addAll(pointsToAdd);
            navigateToPoint(selectedCoor);
        }

    }

    private void navigateToPoint(Coordinate coor) {
        double xValue = coor.getX();
        double yValue = coor.getY();
        for (XYChart.Data<Number, Number> data : SERIES.getData()) {
            if (data.getXValue().doubleValue() == xValue && data.getYValue().doubleValue() == yValue) {
                X_AXIS.setAutoRanging(false);
                Y_AXIS.setAutoRanging(false);
                X_AXIS.setLowerBound(xValue - 1);
                X_AXIS.setUpperBound(xValue + 1);
                Y_AXIS.setLowerBound(yValue - 1);
                Y_AXIS.setUpperBound(yValue + 1);
                return;
            }
        }
    }

    private Coordinate getFocusedCoordinate() {
        return COORDINATE_TABLE.getItems().get(COORDINATE_TABLE.focusModelProperty().get().getFocusedIndex());
    }

    private void intersectionPointGUI() {
        var stage = new Stage();
        var box = new VBox();
        var scene = new Scene(box);

        var intersectionArea = new TextArea();
        intersectionArea.setPromptText("Intersection Point:");

        var inset = new Insets(5);
        var function1Field = new TextField();
        var function2Field = new TextField();
        function1Field.setPromptText("Function 1");
        function2Field.setPromptText("Function 2");

        VBox.setMargin(intersectionArea, inset);
        box.getChildren().addAll(function1Field, function2Field, submitButton, intersectionArea);

        submitButton.setOnAction(e -> parseInputToIntersectionPoint(function1Field.getText(), function2Field.getText(), intersectionArea));

        stage.setScene(scene);
        stage.initOwner(mainStage);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Calculate Intersection Point");
        stage.show();
    }
    
    private void parseInputToIntersectionPoint(String func1, String func2, TextArea area) {
        Window errorWindow = submitButton.getScene().getWindow();
        String functionType1 = identifyFunctionType(func1, errorWindow);
        String functionType2;

        if (functionType1 != null && functionType1.equals(LINEAR_FUNCTION)) {
            LinearFunction function1 = new LinearFunction(M, B);

            functionType2 = identifyFunctionType(func2, errorWindow);
            if (functionType2 != null && functionType2.equals(LINEAR_FUNCTION)) {
                LinearFunction function2 = new LinearFunction(M, B);

                Coordinate intersectionPoint = Line.INTERSECT(function1, function2);
                area.setText("SP" + "( " + intersectionPoint.getX() + " | " + intersectionPoint.getY() + " )");
            }
        }
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