package com.example.logicgate;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class AndGate {

    private final Group gateGroup;
    private final double gateWidth;
    private final double gateHeight;
    private final Pane sceneRoot;

    public AndGate(double xPosition, double screenHeight, double screenWidth, Pane sceneRoot) {
        this.sceneRoot = sceneRoot;
        this.gateHeight = screenHeight / 10;  // 10% of screen height
        this.gateWidth = screenWidth / 15;    // 15% of screen width
        this.gateGroup = createGate(xPosition, screenHeight, screenWidth);
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

        Line backLine = new Line(xPosition - gateWidth, screenHeight / 5 + gateHeight / 3, xPosition - gateWidth, screenHeight / 5 - gateHeight / 3);
        backLine.setStroke(Color.WHITE);
        backLine.setStrokeWidth(3);

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
        Group andGateGroup = new Group(andGateArc, backLine, topLine, inputLine1, inputLine2,bottomLine, outputLine, hitBox);

        // Add event handlers for interaction
        addEventHandlers(andGateGroup, xPosition, screenHeight, screenWidth);

        return andGateGroup;
    }

    private void addEventHandlers(Group andGateGroup, double xPosition, double screenHeight, double screenWidth) {
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
                // Move the current gate
                andGateGroup.setTranslateX(event.getSceneX() - gateWidth / 1.5);
                andGateGroup.setTranslateY(event.getSceneY() - gateHeight * 2);

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
}
