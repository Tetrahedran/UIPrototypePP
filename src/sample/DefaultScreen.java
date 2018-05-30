package sample;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DefaultScreen {

  @FXML
  private TextField numberIntermediatePoints;
  @FXML
  private ScrollPane intermediatePointsPane;
  @FXML
  private StackPane terrainMapView;
  @FXML
  private TextField startingPoint;
  @FXML
  private TextField endingPoint;
  @FXML
  private ToggleGroup optionCalculation;
  @FXML
  private Label errorLabel;
  @FXML
  private TextField costsTextField;
  @FXML
  private CheckBox obstacleCheckBox;
  @FXML
  private Button okButton;

  private Pane pathPane;

  private static final int REC_SIZE = 25;
  private static final int GAP = 5;

  private int maxHeight;
  private int maxWidth;
  private List<TextField> intermediatePoints = new LinkedList<>();
  private Rectangle currentRectangle;
  private TerrainMap terrainMap;
  private boolean currentPointIsObstacle;
  private Queue<Coordinate> path = new LinkedList<>();
  private PathFinder pathFinder = new PathFinderAlgorithm();

  @FXML
  private void calculatePath(){
    errorLabel.setText("");
    Coordinate startingPoint = validCoordinate(this.startingPoint.getText());
    Coordinate endingPoint = validCoordinate(this.endingPoint.getText());
    List<Coordinate> intermediatePoints = new LinkedList<>();
    boolean validIntermediatePoints = true;
    for (TextField textField : this.intermediatePoints){
      Coordinate coordinate = validCoordinate(textField.getText());
      if (coordinate == null){
        validIntermediatePoints = false;
        break;
      }
      intermediatePoints.add(coordinate);
    }
    RadioButton radioButton = (RadioButton) optionCalculation.getSelectedToggle();
    String optionID = radioButton.getId();
    Options option = null;
    if (optionID.equals("costsOnly")){
      option = Options.COSTS_ONLY;
    }else if (optionID.equals("costsAndNumberPoints")){
      option = Options.COSTS_AND_NUMBER_POINTS;
    }
    if (startingPoint != null && endingPoint != null && validIntermediatePoints){
      if (this.intermediatePoints.isEmpty()){
        try {
          path = pathFinder.getPath(terrainMap,startingPoint,endingPoint,option);
        } catch (Exception e) {
          showAlert(e.getMessage());
        }
        System.out.println("NO INTERMEDIA");
      }else {
        try {
          path = pathFinder.getPath(terrainMap,startingPoint,endingPoint,intermediatePoints,option);
        } catch (Exception e) {
          showAlert(e.getMessage());
        }
        System.out.println("WIth Intermedia");
      }
      if(!path.isEmpty()) {
        printPath();
      }
      else{
        showAlert("No Path Found");
      }
    }
    System.out.println(optionID);
  }

  private void printPath(){
    pathPane.getChildren().removeAll(pathPane.getChildren());
    Coordinate start = path.poll();
    int scaling = REC_SIZE + GAP + 1;
    int padding = REC_SIZE/2;
    for (Coordinate point : path) {
      Line line = new Line(padding+start.getXvalue()*scaling, padding+start.getYvalue()*scaling,
          padding+point.getXvalue()*scaling, padding+point.getYvalue()*scaling);
      line.setStrokeWidth(5);
      line.setStroke(new Color(1.0,0.0,0.0,0.5));
      start = point;
      pathPane.getChildren().add(line);
    }
  }

  public void setUpDefaultScreen(int height, int width, Stage stage){
    this.maxHeight = height;
    this.maxWidth = width;
    Stage primaryStage = stage;
    Parent root = null;
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("defaultScreen.fxml"));
      fxmlLoader.setController(this);
      root = fxmlLoader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
    primaryStage.setTitle("Hello World");
    primaryStage.setScene(new Scene(root, 800, 400));
    primaryStage.show();
    setUpDefaultTerrainMap(height,width);
    pathPane = new Pane();
    pathPane.setMouseTransparent(true);
    terrainMapView.getChildren().add(pathPane);
  }

  private void setUpDefaultTerrainMap(int height, int width){
    terrainMap = new TerrainMap(width, height);
    GridPane terrainPane = new GridPane();
    terrainPane.setHgap(GAP);
    terrainPane.setVgap(GAP);
    for (int i = 0; i < height; i++){
      for (int j = 0; j < width; j++){
        StackPane stackPane = new StackPane();
        Label label = new Label("1");
        Rectangle rectangle = new Rectangle(REC_SIZE,REC_SIZE);
        rectangle.setStroke(Paint.valueOf("Black"));
        rectangle.setStrokeWidth(1);
        rectangle.setFill(Paint.valueOf("White"));
        stackPane.getChildren().addAll(rectangle,label);
        stackPane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>(){
          @Override
          public void handle(MouseEvent event) {
            if (currentRectangle != null){
              currentRectangle.setFill(currentPointIsObstacle ? Color.BLACK : Color.WHITE);
            }
            currentRectangle = rectangle;
            System.out.println("REC Clicked");
            currentRectangle.setFill(Color.CORNFLOWERBLUE);
            updateCostAndObstacle(GridPane.getColumnIndex(currentRectangle.getParent()),
                    GridPane.getRowIndex(currentRectangle.getParent()));
          }
        });
        terrainPane.add(stackPane,j,i);
      }
    }
    terrainMapView.getChildren().add(terrainPane);
  }

  @FXML
  private void setListIntermediatePoints(){
    intermediatePoints = new LinkedList<>();
    int number = 0;
    try {
      number = Integer.parseInt(numberIntermediatePoints.getText().trim());
    }catch (Exception e){
    }
    GridPane gridPane = new GridPane();
    gridPane.setVgap(5);
    gridPane.setHgap(10);
    for (int i = 0; i < number; i++){
      Label intermediatePoint = new Label("Point "+(i+1));
      TextField textField = new TextField();
      intermediatePoints.add(textField);
      gridPane.add(intermediatePoint,0,i);
      gridPane.add(textField,1,i);
    }
    intermediatePointsPane.setContent(gridPane);
  }

  @FXML
  private void updateMap(){
    if (currentRectangle != null) {
      Label label = (Label) ((StackPane) currentRectangle.getParent()).getChildren().get(1);
      int x = GridPane.getColumnIndex(currentRectangle.getParent());
      int y = GridPane.getRowIndex(currentRectangle.getParent());
      String costsString = costsTextField.getText();
      try {
        int costs = Integer.parseInt(costsString);
        terrainMap.getMaterialAtPoint(new Coordinate(x, y)).setCosts(costs);
        label.setText(costs > 0 ? costsString : "0");
      } catch (Exception e) {
        errorLabel.setText("Invalid input in costs");
        return;
      }
      currentPointIsObstacle = obstacleCheckBox.isSelected();
      if (currentPointIsObstacle) {
        currentRectangle.setFill(Color.BLACK);
      }
      terrainMap.getMaterialAtPoint(new Coordinate(x, y)).setObstacle(currentPointIsObstacle);
    }
  }

  private Coordinate validCoordinate(String values){
    if (values.contains(",")){
      String [] subStrings = values.trim().split(",");
      if (subStrings.length == 2){
        int x = 0;
        int y = 0;
        try {
          x = Integer.parseInt(subStrings[0]);
          y = Integer.parseInt(subStrings[1]);
        }catch (Exception e){
          errorLabel.setText("Invalid Input");
          return null;
        }
        if (x >= 0 && y >= 0 && x < maxWidth && y <maxHeight){
          return new Coordinate(x,y);
        }else{
          errorLabel.setText(values+" is not inside the map");
          return null;
        }
      }else {
        errorLabel.setText("Please type only two coordinates per field");
        return null;
      }
    }else {
      errorLabel.setText("Please use ',' to separate coordinates");
      return null;
    }
  }

  private void updateCostAndObstacle(int x, int y){
    Material material = terrainMap.getMaterialAtPoint(new Coordinate(x,y));
    costsTextField.setText("" + material.getCosts());
    obstacleCheckBox.setSelected(material.isObstacle());
    currentPointIsObstacle = material.isObstacle();
  }

  private void showAlert(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Fehler");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.show();
  }
}
