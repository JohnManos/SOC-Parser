package socparser;
	
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//import javafx.application.Application;
//import javafx.event.ActionEvent;
//import javafx.event.EventHandler;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.layout.StackPane;
//import javafx.stage.Stage;
 
//public class Main extends Application {
//    public static void main(String[] args) {
//        launch(args);
//    }
//    
//    @Override
//    public void start(Stage primaryStage) {
//        primaryStage.setTitle("Hello World!");
//        Button btn = new Button();
//        btn.setText("Say 'Hello World'");
//        btn.setOnAction(new EventHandler<ActionEvent>() {
// 
//            @Override
//            public void handle(ActionEvent event) {
//                System.out.println("Hello World!");
//            }
//        });
//        
//        StackPane root = new StackPane();
//        root.getChildren().add(btn);
//        primaryStage.setScene(new Scene(root, 300, 250));
//        primaryStage.show();
//    }
//}


import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;


public class Main extends Application {
	private Desktop desktop = Desktop.getDesktop();
	private String fileName = null;
	private final String dbUrl = "jdbc:mysql://localhost:3306/";
	@Override
	public void start(Stage primaryStage) {
		try {
			createFileInputStage(primaryStage);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}	
	private void createFileInputStage(Stage stage) {
		stage.setTitle("Enter filename");
		StackPane root = new StackPane();
		//TextField fileInput = new TextField();
		final FileChooser fileChooser = new FileChooser();
        final Button openButton = new Button("Open a Spreadsheet");
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				File file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    //openFile(file);
                    fileName = file.getPath();
                }
				//fileName = fileInput.getText();
				//System.out.println(fileName);
				parseAndRender();
			}
		};
		openButton.setOnAction(event);
		//fileInput.setOnAction(event);
		//root.getChildren().add(fileInput);
		root.getChildren().add(openButton);
		Scene scene = new Scene(root, 600, 600);
		scene.getStylesheets().add(getClass().getResource("socparser.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
	}
	
	private void openFile(File file) {
        try {
            desktop.open(file);
        } catch (IOException ex) {
            Logger.getLogger(
                Main.class.getName()).log(
                    Level.SEVERE, null, ex
                );
        }
    }
	
	private void parseAndRender() {
		ExcelParser parser = new ExcelParser();
		DBRenderer renderer = new DBRenderer();
		renderer.connectToServer(dbUrl);
		renderer.createDatabase("Courses");
		renderer.connectToServer(dbUrl + "Courses");
		if (!renderer.beenParsed("Sample")) {
			String[][] dataArray = parser.parseFile(fileName, renderer);
			String[] labels = new String[dataArray[0].length];
			int rowIndex = 0;
			for (String[] row : dataArray) {
				Class rowObject = new Class();
				// First data row is labels, so retrieve them to use as keys
				if (rowIndex == 0) {
					for (int i = 0; i < row.length; i++) {
						labels[i] = row[i];
					}
					// This is called here because it needs an instance of the row's object to determine column names, but need only be called once
					renderer.createTable("Sample", labels);
				}
				else {
					// create key/vale pair for each cell
					for (int i = 0; i < row.length; i++) {
						rowObject.add(labels[i], row[i]);;
					}
					// Only add row into database if it is not the label row
					renderer.insertRowInDB("Sample", rowObject);
				}
				++rowIndex;
				
			}
			renderer.registerParsed("Sample");
		} else System.out.println("File already in database.");
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
