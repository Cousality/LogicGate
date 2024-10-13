package com.example.logicgate;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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

        // Add event handler to the left vertical line for adding nodes
        verticalLine.setOnMousePressed(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                // Get the Y position where the mouse was pressed on the line
                double yPosition = event.getY();
                // Create a node (Circle) at the clicked position on the line
                Circle node = createNode(xPosition, yPosition, sceneRoot);
                // Add the node to the scene
                sceneRoot.getChildren().add(node);
            }
        });

        // Add the Assets
        sceneRoot.getChildren().addAll(verticalLine, verticalLine2, andGate);

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

    private Circle createNode(double x, double y, Pane sceneRoot) {
        Circle node = new Circle(x, y, 10); // Creates a circle with a radius of 10
        node.setFill(Color.WHITE); // Initial color is white

        // Store nodeState inside the node's properties
        node.getProperties().put("nodeState", false); // false represents the initial "off" state

        node.setOnMouseReleased(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                // Toggle nodeState
                boolean nodeState = (boolean) node.getProperties().get("nodeState");
                nodeState = !nodeState;
                node.getProperties().put("nodeState", nodeState);

                // Change color based on the new state
                if (nodeState) {
                    node.setFill(Color.RED); // Node is "on"
                } else {
                    node.setFill(Color.WHITE); // Node is "off"
                }
            }

            if (event.getButton().equals(MouseButton.SECONDARY)) {
                sceneRoot.getChildren().remove(node); // Remove the node from the scene
            }
        });

        return node;
    }
}