package socparser;
	
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
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
    private boolean displayed = false;
    private boolean viaParseButton = false; /// TODO: the need for this bool is likely indicative of design flaw

	@Override
	public void start(Stage primaryStage) {
		try {
			createHomeStage(primaryStage);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}	
	
	private void createHomeStage(Stage stage) {
		stage.setTitle("Home");
		GridPane root = new GridPane();
		
		parser = new ExcelParser();
		renderer = new DBRenderer(dbUrl, "SOC");
		ObservableList<String> tableNames = FXCollections.observableArrayList(renderer.getTableNames());
		
		ChoiceBox choiceBox = new ChoiceBox();
	    choiceBox.setItems(tableNames);
	    // Label to display the selected menuitem 
        //Label selected = new Label("default item selected"); 
        choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
        	public void changed(ObservableValue ov, Number value, Number newValue) { 
            	fileName = tableNames.get(newValue.intValue());
            	//if (!viaParseButton) { // this is necessary so that setting a defalt choicebox does not trigger table poplation
            		displayed = false;
            		populateTable();
            	//}
            	//viaParseButton = false;
            }  
        });
        
		final FileChooser fileChooser = new FileChooser();
        final Button openButton = new Button("Open a Spreadsheet");
		EventHandler<ActionEvent> buttonEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    //openFile(file);
                    fileName = FilenameUtils.getBaseName(file.getName());
                    filePath = file.getPath();
    				//fileName = fileInput.getText();
    				//System.out.println(fileName);
            		if (!renderer.tableExists(fileName)) {
	                    parseAndRender();
	        			//populateTable();
	                	tableNames.add(fileName);
	                	//viaParseButton = true;
	                	choiceBox.getSelectionModel().select(fileName);
            		}
            		else System.out.println("File already in database.");
                }
			}		
		};
		openButton.setOnAction(buttonEvent);

		final Button resetButton = new Button("Unhide all columns");
		EventHandler<ActionEvent> resetEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				for (Object col : table.getColumns())
					((TableColumn) col).setVisible(true);
			}
		};
		resetButton.setOnAction(resetEvent);
		
		openButton.setMaxWidth(Double.MAX_VALUE);
		choiceBox.setMaxWidth(Double.MAX_VALUE);		
		GridPane.setConstraints(openButton, 0, 0, 2, 1);
		GridPane.setConstraints(resetButton, 2, 0, 1, 1);
		GridPane.setConstraints(choiceBox, 3, 0, 3, 1);
		GridPane.setConstraints(table, 0, 1);
		GridPane.setColumnSpan(table, 6);
		GridPane.setVgrow(table, Priority.ALWAYS);
		GridPane.setHgrow(table, Priority.ALWAYS);
		root.getChildren().addAll(openButton, resetButton, choiceBox, table);
		ColumnConstraints col1Constraints = new ColumnConstraints();
		col1Constraints.setPercentWidth(16.66);
		root.getColumnConstraints().addAll(col1Constraints, col1Constraints, col1Constraints, col1Constraints, col1Constraints, col1Constraints);
		root.setVgap(25);
		root.setHgap(20);
		Scene scene = new Scene(root, 600, 600);
		scene.getStylesheets().add(getClass().getResource("socparser.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
	}
	
	private void populateTable(){
		ResultSet rs;
        if (!displayed) {      	
        	// TODO: refactor the following into a DBRenderer method that takes the filename and 'keys' as params (in this case, Sect and Class Nbr)
        	// TODO: fix critical bug in which some rows do not get concatenated properly
        	// Construct the SQL query used to reorganize the soc data
    		data.removeAll(data);
        	String[] labels = renderer.getColumnNames(fileName);
			String sql = "SELECT";
        	for(int i = 0 ; i < labels.length; i++){
        		String label = labels[i];
				//sql += ("uPDATE " + fileName + " t1, " + fileName + " t2 SET label = CONCAT_WS(t1.`" + label + "`, t2.`" + label + "`) WHERE t1.Sect <> t2.`" + label + "`");
        		if (label.equals("Id")) {
        			continue;
        		}
        		else if (label.equals("`Class Nbr`") || label.equals("Sect")) {
        			sql += (" `" + label + "`,");
        		}
        		else if (label.equals("Day/s")) {
        			sql += (" GROUP_CONCAT(`" + label + "` ORDER BY Id  SEPARATOR ' ') `" + label + "`,");
        		}
        		else {
        			sql += (" GROUP_CONCAT(DISTINCT `" + label + "` ORDER BY Id SEPARATOR ' ') `" + label + "`,");
        		}
        		// THIS ONE sql += (" IF(t1.`" + label + "` = t2.`" + label + "`, t1.`" + label + "`, GROuP_CONCAT(DISTINCT t1.`" + label + "` SEPARATOR ' ')),");				
        		//sql += (" IF(`" + label + "` = Sect OR `" + label + "` = `Class Nbr`, `" + label + "`, GROuP_CONCAT(DISTINCT t1.`" + label + "` SEPARATOR ' ')),");				
				// sql += (" CASE WHEN `t1." + label + "` = `t2." + label + "` THEN `t1." + label + "` WHEN `t1." + label + "` <> `t2." + label + "` THEN CONCAT_WS(`t1." + label + "`,`t2." + label + "`) END,");				
        	}
        	sql = sql.substring(0, sql.length() - 1); // chop trailing comma
        	sql += (" FROM `" + fileName + "` GROUP BY `Class Nbr`, Sect");
        	//sql += (" FROM `" + fileName + "` t1 JOIN `" + fileName + "` t2 ON t1.Sect = t2.Sect AND t1.`Class Nbr` = t2.`Class Nbr` GROUP BY t1.Sect, t1.`Class Nbr`");
        	System.out.println(sql);
        	try {
				rs = renderer.getConnection().createStatement().executeQuery(sql);
			} catch (SQLException se) {
				se.printStackTrace();
	        	System.err.println("Error populating table.");  
	        	if (renderer == null) {
	        		System.err.println("Renderer not initialized.");  
	        	}
	        	return;
			}

	    	// Create columns based on sql table and add to tableView
        	try {
	        	ResultSetMetaData rsmd = rs.getMetaData();
		    	for(int i = 1; i <= rsmd.getColumnCount(); i++){
		    		final int j = i - 1; // The factory below requires a final (or effectively final) variable to be used
		    		TableColumn col = new TableColumn();
		    		EventHandler<ActionEvent> hideEvent = new EventHandler<ActionEvent>() {
		    			public void handle(ActionEvent e) {
		    				col.setVisible(false);
		    			}
		    		};
		    		Button hide = new Button(rsmd.getColumnLabel(i));
		    		hide.setOnAction(hideEvent);
		    		col.setGraphic(hide);
		    		// Since we are retrieving data dynamically from a sql table, we cannot use PropertyValueFactory, so we use a Callback.
		    		// All cell value factories take a CellDataFeatures instance and return an ObservableValue.
		    		// Basically this stuff is stupid complicated so just follow the documentation and/or stackoverflow.
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
					for(int i = 1 ; i <= rsmd.getColumnCount(); i++){
						row.add(rs.getString(i));
					}
					//System.out.println("Row added " + row );
					data.add(row);
				}
				
			} catch (SQLException se) {
				se.printStackTrace();
	        	System.err.println("Error populating table.");  
	        	System.err.println("Error retrieving from table " + fileName);  
			}
        	
	    	table.setItems(data);
	    	displayed = true;
        }
	}
	
	private void parseAndRender() {
		String[][] dataArray = parser.parseFile(filePath, renderer);
		//TODO: this should be a DBRenderer method. DBRenderer should take a 2D data array in its constructor
		String[] labels = new String[dataArray[0].length + 1]; // The first nonempty row is the column labels
		int rowIndex = 0; // keeps track of the row number, so that row 0 is only used for column names, and to populate Id column
		for (String[] row : dataArray) {
			Class rowObject = new Class();
			// Retrieve the column labels from the parse result and store to use as key in rowObject
			if (rowIndex == 0) {
				labels[0] = "Id"; // We need the first column to be an Id that is not in the parsed spreadsheet
				for (int i = 0; i < row.length; i++) {
					labels[i + 1] = row[i];
				}
				// This is called here because it needs an instance of the row's object to determine column names, but need only be called once
				renderer.createTable(fileName, labels);
			}
			else {
				// create key/value pair for each cell
				rowObject.add(labels[0], String.valueOf(rowIndex)); // add our new Id entry for each row
				for (int i = 0; i < row.length; i++) {
					rowObject.add(labels[i + 1], row[i]); // everything else comes from the parsed sheet
				}
				// Only add row into database if it is not the label row
				renderer.insertRowInDB(fileName, rowObject);
			}
			++rowIndex;				
		}
		//renderer.registerParsed(fileName);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
