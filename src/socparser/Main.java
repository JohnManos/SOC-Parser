package socparser;
	
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
//import javafx.scene.control.TextField;
//import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
//import javafx.scene.layout.StackPane;


public class Main extends Application {
	private Desktop desktop = Desktop.getDesktop();
	private File file = null;
	private String fileName = null;
	private String filePath = null;
	private final String dbUrl = "jdbc:mysql://localhost:3306/";
	private ExcelParser parser = null;
	private DBRenderer renderer = null;
	private ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
    private TableView table = new TableView();

	@Override
	public void start(Stage primaryStage) {
		try {
			createFileInputStage(primaryStage);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}	
	private void createFileInputStage(Stage stage) {
		stage.setTitle("Home");
		GridPane root = new GridPane();
		//TextField fileInput = new TextField();
		final FileChooser fileChooser = new FileChooser();
        final Button openButton = new Button("Open a Spreadsheet");
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    //openFile(file);
                    fileName = FilenameUtils.getBaseName(file.getName());
                    filePath = file.getPath();
                }
				//fileName = fileInput.getText();
				//System.out.println(fileName);
				parseAndRender();
    			populateTable();
			}
		};
		openButton.setOnAction(event);
		parser = new ExcelParser();
		renderer = new DBRenderer(dbUrl, "SOC");
		GridPane.setConstraints(openButton, 0, 1);
		GridPane.setConstraints(table, 0, 2);
		GridPane.setVgrow(table, Priority.ALWAYS);
		root.getChildren().addAll(openButton, table);
		Scene scene = new Scene(root, 600, 600);
		scene.getStylesheets().add(getClass().getResource("socparser.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
	}
	
	/*private void openFile(File file) {
        try {
            desktop.open(file);
        } catch (IOException ex) {
            Logger.getLogger(
                Main.class.getName()).log(
                    Level.SEVERE, null, ex
                );
        }
    }*/
	
	private void populateTable(){
        try{        	
        	// TODO: refactor the following into a DBRenderer method that takes the filename and 'keys' as params (in this case, Sect and Class Nbr)
        	// TODO: fix critical bug in which some rows do not get concatenated properly
        	ResultSet rs = renderer.getTableEntries(fileName);
        	// Construct the SQL query used to reorganize the soc data
        	String label = null;
			String sql = "SELECT";
        	for(int i = 0 ; i < rs.getMetaData().getColumnCount(); i++){
        		label = (rs.getMetaData().getColumnName(i+1));
				//sql += ("uPDATE " + fileName + " t1, " + fileName + " t2 SET label = CONCAT_WS(t1.`" + label + "`, t2.`" + label + "`) WHERE t1.Sect <> t2.`" + label + "`");
        		sql += (" IF(t1.`" + label + "` = t2.`" + label + "`, t1.`" + label + "`, GROuP_CONCAT(DISTINCT t1.`" + label + "` SEPARATOR ' ')),");				
        		//sql += (" IF(`" + label + "` = Sect OR `" + label + "` = `Class Nbr`, `" + label + "`, GROuP_CONCAT(DISTINCT t1.`" + label + "` SEPARATOR ' ')),");				
				// sql += (" CASE WHEN `t1." + label + "` = `t2." + label + "` THEN `t1." + label + "` WHEN `t1." + label + "` <> `t2." + label + "` THEN CONCAT_WS(`t1." + label + "`,`t2." + label + "`) END,");				
        	}
        	sql = sql.substring(0, sql.length() - 1); // chop trailing comma
        	sql += (" FROM `" + fileName + "` t1 JOIN `" + fileName + "` t2 ON t1.Sect = t2.Sect AND t1.`Class Nbr` = t2.`Class Nbr` GROUP BY t1.Sect, t1.`Class Nbr`");
        	System.out.println(sql);
        	try {
				rs = renderer.getConnection().createStatement().executeQuery(sql);
			} catch (SQLException se) {
				se.printStackTrace();
			}
        	// Create columns based on sql table and add to tableView
        	String[] colNames = renderer.getColumnNames(fileName);
        	for(int i = 0 ; i < colNames.length; i++){
        		final int j = i; // The factory below requires a final (or effectively final) variable to be used
        		TableColumn col = new TableColumn(colNames[i]);
        		// Since we are retrieving data dynamically from a sql table, we cannot use PropertyValueFactory, so we use a Callback.
        		// All cell value factories take a CellDataFeatures instance and return an ObservableValue.
        		// Basically this stuff is stupid complicated so just follow the documentation and/or stackoverflow.
        		// Filling a table using observables never looks clean but man does JavaFX take the cake.
        		col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList,String>, ObservableValue<String>>(){
        			public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
        				return new SimpleStringProperty(param.getValue().get(j).toString());
        			}
        		});		
        		table.getColumns().addAll(col); 
        		//System.out.println("Column [" + i +"] ");
        	}

        	// Gather data into ObservableList to add to populate table
        	while(rs.next()){
        		ObservableList<String> row = FXCollections.observableArrayList();
        		for(int i=1 ; i<=rs.getMetaData().getColumnCount(); i++){
        			row.add(rs.getString(i));
        		}
        		//System.out.println("Row added " + row );
        		data.add(row);
        	}
        	table.setItems(data);
        }catch (Exception e) {
        	e.printStackTrace();
        	System.err.println("Error populating table.");  
        	if (renderer == null) {
        		System.err.println("Renderer not initialized.");  
        	}
        }
    }
	
	private void parseAndRender() {
		if (!renderer.beenParsed(fileName)) {
			String[][] dataArray = parser.parseFile(filePath, renderer);
			//TODO: this should be a DBRenderer method. DBRenderer should take a 2D data array in its constructor
			String[] labels = new String[dataArray[0].length]; // The first nonempty row is the column labels
			int rowIndex = 0; // keeps track of the row number, so that row 0 is only used for column names
			for (String[] row : dataArray) {
				Class rowObject = new Class();
				// Retrieve the column labels from the parse result and store to use as key in rowObject
				if (rowIndex == 0) {
					for (int i = 0; i < row.length; i++) {
						labels[i] = row[i];
					}
					// This is called here because it needs an instance of the row's object to determine column names, but need only be called once
					renderer.createTable(fileName, labels);
				}
				else {
					// create key/value pair for each cell
					for (int i = 0; i < row.length; i++) {
						rowObject.add(labels[i], row[i]);;
					}
					// Only add row into database if it is not the label row
					renderer.insertRowInDB(fileName, rowObject);
				}
				++rowIndex;				
			}
			renderer.registerParsed(fileName);
		} else System.out.println("File already in database.");
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
