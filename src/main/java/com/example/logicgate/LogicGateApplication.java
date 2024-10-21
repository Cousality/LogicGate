package com.example.logicgate;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.Node;
import javafx.scene.shape.Line;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class LogicGateApplication extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        Pane sceneRoot = new Pane();

        // Grabs size of the window to use to draw the line
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

        // Sets up screen width height in addition to background color
        Scene scene = new Scene(sceneRoot, screenWidth, screenHeight, Color.rgb(30, 30, 35));

        // Add a vertical white line at 1/10 of the width of the screen (left line)
        double xPosition = screenWidth / 10;
        Line verticalLine = createVerticalLine(xPosition, screenHeight);

        // Add a vertical white line at 9/10 of the width of the screen (right line)
        Line verticalLine2 = createVerticalLine(xPosition * 9, screenHeight);

        // Create the initial AND gate
        Group andGate = new AndGate(xPosition, screenHeight, screenWidth, sceneRoot).getGateGroup();

        sceneRoot.getChildren().addAll(verticalLine, verticalLine2, andGate);

        // Add click handler to vertical lines to create nodes
        addLineClickHandler(verticalLine, sceneRoot);
        addLineClickHandler(verticalLine2, sceneRoot);

        // Set up the stage
        stage.setScene(scene);
        stage.setTitle("Logic Gate Simulator");
        stage.setResizable(false);
        stage.setFullScreen(true);
        stage.show();
    }

    private Line createVerticalLine(double xPosition, double screenHeight) {
        Line line = new Line(xPosition, 0, xPosition, screenHeight);
        line.setStroke(Color.WHITE);
        line.setStrokeWidth(5);
        return line;
    }

    // Add click handler to create a node on the line
    private void addLineClickHandler(Line line, Pane sceneRoot) {
        line.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                // Create a node (small circle) on the line at the clicked position
                double x = line.getStartX();
                double y = event.getY();
                Circle node = new Circle(x, y, 10, Color.WHITE);

                // Add the node to the scene
                sceneRoot.getChildren().add(node);

                // Start the dragging behavior to create a connection line
                startDraggingFromNode(node, sceneRoot);
            }
        });
    }

    private void startDraggingFromNode(Circle node, Pane sceneRoot) {
        Line connectionLine = new Line();
        connectionLine.setStroke(Color.WHITE);
        connectionLine.setStrokeWidth(3);

        // Use boolean arrays to track states
        boolean[] isOn = {false};    // Track if the node is "on" (true for red, false for white)
        boolean[] snapped = {false}; // Track whether the connection line is snapped to an input line
        Line[] snappedInputLine = {null}; // Track which input line the connection line is snapped to

        node.setOnMouseDragged(event -> {
            // Set the start and end points of the connection line
            connectionLine.setStartX(node.getCenterX());
            connectionLine.setStartY(node.getCenterY());
            connectionLine.setEndX(event.getSceneX());
            connectionLine.setEndY(event.getSceneY());

            // Add the connection line to the scene if it's not already present
            if (!sceneRoot.getChildren().contains(connectionLine)) {
                sceneRoot.getChildren().add(connectionLine);
            }

            // Check if the connection line is near an input line of a gate and snap
            snapToGateInput(connectionLine, sceneRoot, snapped, snappedInputLine);
        });

        // Handle mouse release: delete if not snapped
        node.setOnMouseReleased(event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                // Check if the connection line has been snapped
                if (!snapped[0]) {
                    // If not snapped, remove the connection line
                    sceneRoot.getChildren().remove(connectionLine);
                }
            }
        });

        // Handle node clicks for toggling color
        node.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {  // Right-click
                // Remove the node and its connection line
                sceneRoot.getChildren().remove(node);
                sceneRoot.getChildren().remove(connectionLine);
            } else if (event.getButton().equals(MouseButton.PRIMARY)) {  // Left-click
                // Toggle the node state
                isOn[0] = !isOn[0];

                // Change the node's color
                if (isOn[0]) {  // If node is "on" (red)
                    node.setFill(Color.RED);
                    connectionLine.setStroke(Color.RED);
                    if (snappedInputLine[0] != null) {
                        // Also turn the snapped input line red
                        snappedInputLine[0].setStroke(Color.RED);
                    }
                } else {  // If node is "off" (white)
                    node.setFill(Color.WHITE);
                    connectionLine.setStroke(Color.WHITE);
                    if (snappedInputLine[0] != null) {
                        // Also turn the snapped input line white
                        snappedInputLine[0].setStroke(Color.WHITE);
                    }
                }
            }
        });
    }


    private void snapToGateInput(Line connectionLine, Pane sceneRoot, boolean[] snapped, Line[] snappedInputLine) {
        // Loop through all gates in the scene and check proximity to input lines
        for (Node child : sceneRoot.getChildren()) {
            if (child instanceof Group gateGroup) {

                // Skip snapping if the gate is the original (not spawned)
                if (!gateGroup.getProperties().containsKey("spawned")) {
                    continue;
                }

                // Get the translation of the group (if it's moved)
                double translateX = gateGroup.getTranslateX();
                double translateY = gateGroup.getTranslateY();

                // Check if this group contains lines that are input lines of an AND gate
                for (Node gatePart : gateGroup.getChildren()) {
                    if (gatePart instanceof Line inputLine) {

                        // Check if this line has input keys set (e.g., "input1" or "input2")
                        String inputKey = (String) inputLine.getUserData();

                        if (inputKey != null && (inputKey.equals("input1") || inputKey.equals("input2"))) {
                            // Adjust inputLine's position by the translation of the group
                            double adjustedStartX = inputLine.getStartX() + translateX;
                            double adjustedStartY = inputLine.getStartY() + translateY;

                            // Calculate the distance between the connection line's end and the adjusted input line's start
                            double distance = Math.sqrt(Math.pow(adjustedStartX - connectionLine.getEndX(), 2) +
                                    Math.pow(adjustedStartY - connectionLine.getEndY(), 2));

                            if (distance < 20) { // Adjust snapping sensitivity
                                // Snap the connection line's end to the adjusted input line's start
                                connectionLine.setEndX(adjustedStartX);
                                connectionLine.setEndY(adjustedStartY);

                                // Mark the connection as snapped
                                snapped[0] = true;

                                // Store the snapped input line for color updates
                                snappedInputLine[0] = inputLine;
                            }
                        }
                    }
                }
            }
        }
    }

}