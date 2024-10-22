package com.example.logicgate;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndGate {

    private final Group gateGroup;
    private final double gateWidth;
    private final double gateHeight;
    private final Pane sceneRoot;

    // Store snapped connection lines for input1 and input2
    private final Map<String, List<Line>> snappedConnections = new HashMap<>();

    public AndGate(double xPosition, double screenHeight, double screenWidth, Pane sceneRoot) {
        this.sceneRoot = sceneRoot;
        this.gateHeight = screenHeight / 10;  // 10% of screen height
        this.gateWidth = screenWidth / 15;    // 15% of screen width
        this.gateGroup = createGate(xPosition, screenHeight, screenWidth);

        // Initialize connection tracking
        snappedConnections.put("input1", new ArrayList<>());
        snappedConnections.put("input2", new ArrayList<>());
    }

    public Group getGateGroup() {
        return gateGroup;
    }

    private Group createGate(double xPosition, double screenHeight, double screenWidth) {
        // Create the andGate arc
        Arc andGateArc = new Arc(xPosition - (gateWidth / 1.55), screenHeight / 5, gateWidth / 3, gateHeight / 3, 270, 180);
        andGateArc.setFill(Color.TRANSPARENT);
        andGateArc.setStroke(Color.WHITE);
        andGateArc.setStrokeWidth(3);
        andGateArc.setType(ArcType.OPEN);

        Line topLine = new Line(xPosition - gateWidth, screenHeight / 5 + gateHeight / 3, xPosition - (gateWidth / 1.5), screenHeight / 5 + gateHeight / 3);
        topLine.setStroke(Color.WHITE);
        topLine.setStrokeWidth(3);

        Line bottomLine = new Line(xPosition - gateWidth, screenHeight / 5 - gateHeight / 3, xPosition - (gateWidth / 1.5), screenHeight / 5 - gateHeight / 3);
        bottomLine.setStroke(Color.WHITE);
        bottomLine.setStrokeWidth(3);

        // Input and Output lines
        Line inputLine1 = new Line(xPosition - gateWidth - (gateWidth / 4), screenHeight / 5 - gateHeight / 5, xPosition - gateWidth, screenHeight / 5 - gateHeight / 5);
        inputLine1.setStroke(Color.WHITE);
        inputLine1.setStrokeWidth(3);
        inputLine1.setUserData("input1");

        Line inputLine2 = new Line(xPosition - gateWidth - (gateWidth / 4), screenHeight / 5 + gateHeight / 5, xPosition - gateWidth, screenHeight / 5 + gateHeight / 5);
        inputLine2.setStroke(Color.WHITE);
        inputLine2.setStrokeWidth(3);
        inputLine2.setUserData("input2");

        Line backLine = new Line(xPosition - gateWidth, screenHeight / 5 + gateHeight / 3, xPosition - gateWidth, screenHeight / 5 - gateHeight / 3);
        backLine.setStroke(Color.WHITE);
        backLine.setStrokeWidth(3);

        Line outputLine = new Line(xPosition - (gateWidth / 1.80) + (gateWidth / 4), screenHeight / 5, xPosition - (gateWidth / 2) + (gateWidth / 2.5), screenHeight / 5);
        outputLine.setStroke(Color.WHITE);
        outputLine.setStrokeWidth(3);
        outputLine.setUserData("output");

        // Hit box
        Rectangle hitBox = new Rectangle(xPosition - gateWidth, screenHeight / 5 - gateHeight / 3, gateWidth / 1.5, gateHeight / 1.5);
        hitBox.setStroke(Color.TRANSPARENT);
        hitBox.setFill(Color.TRANSPARENT);
        hitBox.setStrokeWidth(3);

        // Group the gate elements
        Group andGateGroup = new Group(andGateArc, topLine,bottomLine, inputLine1, inputLine2, backLine, outputLine, hitBox);

        // Add event handlers for interaction
        addEventHandlers(andGateGroup, inputLine1, inputLine2, xPosition, screenHeight, screenWidth);

        return andGateGroup;
    }

    private void addEventHandlers(Group andGateGroup, Line inputLine1, Line inputLine2, double xPosition, double screenHeight, double screenWidth) {
        andGateGroup.setOnMousePressed(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                andGateGroup.toFront();
            }
            if (andGateGroup.getProperties().containsKey("spawned")) {
                if (event.getButton().equals(MouseButton.SECONDARY)) {
                    sceneRoot.getChildren().remove(andGateGroup);
                }
            }
        });

        andGateGroup.setOnMouseDragged(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {

                // Snap any nearby connection lines
                snapNearbyConnections(inputLine1, "input1");
                snapNearbyConnections(inputLine2, "input2");

                // Move the current gate
                andGateGroup.setTranslateX(event.getSceneX() - gateWidth / 1.5);
                andGateGroup.setTranslateY(event.getSceneY() - gateHeight * 2);

                // Update connection lines snapped to inputs during dragging
                updateSnappedConnections(inputLine1, inputLine2);




                // If the gate is dragged past the original position, spawn a new gate
                if (!andGateGroup.getProperties().containsKey("spawned")) {
                    AndGate newAndGate = new AndGate(xPosition, screenHeight, screenWidth, sceneRoot);
                    sceneRoot.getChildren().add(newAndGate.getGateGroup());
                    andGateGroup.getProperties().put("spawned", true);
                }
            }
        });

        // Remove the gate if it's dropped outside the defined area
        andGateGroup.setOnMouseReleased(_ -> {
            double currentX = andGateGroup.getTranslateX() + (xPosition / 10);
            if (andGateGroup.getProperties().containsKey("spawned")) {
                if (currentX < xPosition || currentX > xPosition * 8) {
                    sceneRoot.getChildren().remove(andGateGroup);
                }
            }
        });
    }

    // Call this method when snapping a connection line to an input line
    public void snapConnectionToInput(Line connectionLine, String inputKey) {
        List<Line> connections = snappedConnections.get(inputKey);
        if (connections != null) {
            connections.add(connectionLine);
        }
    }

    // Update snapped connection lines when the gate is dragged
    private void updateSnappedConnections(Line inputLine1, Line inputLine2) {
        // Update all connection lines snapped to inputLine1
        for (Line connectionLine : snappedConnections.get("input1")) {
            connectionLine.setEndX(inputLine1.getStartX() + inputLine1.getTranslateX() + gateGroup.getTranslateX());
            connectionLine.setEndY(inputLine1.getStartY() + inputLine1.getTranslateY() + gateGroup.getTranslateY());
        }

        // Update all connection lines snapped to inputLine2
        for (Line connectionLine : snappedConnections.get("input2")) {
            connectionLine.setEndX(inputLine2.getStartX() + inputLine2.getTranslateX() + gateGroup.getTranslateX());
            connectionLine.setEndY(inputLine2.getStartY() + inputLine2.getTranslateY() + gateGroup.getTranslateY());
        }
    }

    // Check for nearby connections and snap them to the input lines
    private void snapNearbyConnections(Line inputLine, String inputKey) {
        // Iterate through all lines in the scene and check for proximity to the input line
        for (var node : sceneRoot.getChildren()) {
            if (node instanceof Line connectionLine) {

                // Check if the line's end is close to the input line
                if (isCloseToInputLine(connectionLine, inputLine)) {
                    // Snap the connection line to the input line
                    snapConnectionToInput(connectionLine, inputKey);

                    // Set the line's endpoint to match the input line's position
                    connectionLine.setEndX(inputLine.getStartX() + inputLine.getTranslateX() + gateGroup.getTranslateX());
                    connectionLine.setEndY(inputLine.getStartY() + inputLine.getTranslateY() + gateGroup.getTranslateY());
                }
            }
        }
    }

    // Helper method to check if a connection line is close to the input line
    private boolean isCloseToInputLine(Line connectionLine, Line inputLine) {
        double distance = Math.sqrt(
                Math.pow(connectionLine.getEndX() - (inputLine.getStartX() + inputLine.getTranslateX() + gateGroup.getTranslateX()), 2) +
                        Math.pow(connectionLine.getEndY() - (inputLine.getStartY() + inputLine.getTranslateY() + gateGroup.getTranslateY()), 2)
        );
        // Max distance to snap a line to an input
        double SNAP_THRESHOLD = 20.0;
        return distance < SNAP_THRESHOLD;
    }
}

