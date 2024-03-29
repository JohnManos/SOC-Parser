package socparser;
	
import java.awt.Desktop;
import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;


public class Main extends Application {
	
	GridPane root = new GridPane();
	private Desktop desktop = Desktop.getDesktop();
	private File file = null;
	private String fileName = null;
	private String filePath = null;
	private final String dbUrl = "jdbc:h2:./socparser;create=true;user=me;password=mine";
	private ExcelParser parser = null;
	private DBRenderer renderer = null;
	private ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
    private TableView<ObservableList<String>> table = new TableView<ObservableList<String>>();
    private TextField searchField = new TextField();
    private final Button dropButton = new Button("Drop this spreadsheet");
    private boolean displayed = false;
    private boolean isCompoundMode = true;

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
		
		parser = new ExcelParser();
		renderer = new DBRenderer(dbUrl, "org.h2.Driver");
		ObservableList<String> tableNames = FXCollections.observableArrayList(renderer.getTableNames());
		
		ComboBox choiceBox = new ComboBox();
		choiceBox.setPromptText("Select a table");
	    choiceBox.setItems(tableNames);
        choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
        	public void changed(ObservableValue ov, Number value, Number newValue) { 
            	fileName = tableNames.get(newValue.intValue());
            	displayed = false;
            	populateTable();
            }  
        });
        
		final FileChooser fileChooser = new FileChooser();
        final Button openButton = new Button("Open a Spreadsheet");
		EventHandler<ActionEvent> buttonEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    fileName = FilenameUtils.getBaseName(file.getName());
                    filePath = file.getPath();
            		if (!renderer.tableExists(fileName)) {
	                    parseAndRender();
	                	tableNames.add(fileName);
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
				int i = 0;
				for (Object col : table.getColumns()) {
					if (i++ == 0) continue; // Never set the Id column to visible, because it is an internal implementation detail
					((TableColumn) col).setVisible(true);
				}
			}
		};
		resetButton.setOnAction(resetEvent);
		
		EventHandler<ActionEvent> dropEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				renderer.dropTable(fileName);
	    		data.removeAll(data);
	    		table.getColumns().clear();
	    		int i = choiceBox.getItems().indexOf(fileName);
	    		choiceBox.getSelectionModel().clearSelection();
	    		choiceBox.getItems().remove(i);
			}		
		};
		dropButton.setOnAction(dropEvent);
			   
		searchField.setPromptText("Search table...");

		final ToggleGroup group = new ToggleGroup();
	    RadioButton compoundView = new RadioButton("Compound View");
	    RadioButton meetingView = new RadioButton("Meeting View");
	    compoundView.setToggleGroup(group);
	    meetingView.setToggleGroup(group);
	    compoundView.setSelected(true);
	    group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
	        public void changed(ObservableValue<? extends Toggle> ov,
	            Toggle old_toggle, Toggle new_toggle) {
	                if (group.getSelectedToggle() != null) {
	                    isCompoundMode = !isCompoundMode;
	                    displayed = false;
	                    populateTable();
	                }                
	            }
	    });

		openButton.setMaxWidth(Double.MAX_VALUE);
		choiceBox.setMaxWidth(Double.MAX_VALUE);		
		GridPane.setConstraints(openButton, 0, 0, 2, 1);
		GridPane.setConstraints(resetButton, 2, 0, 1, 1);
		GridPane.setConstraints(dropButton, 0, 1, 2, 1);
		GridPane.setConstraints(choiceBox, 3, 0, 3, 1);
		GridPane.setConstraints(compoundView, 2, 1, 1, 1);
		GridPane.setConstraints(meetingView, 3, 1, 1, 1);
		GridPane.setConstraints(searchField, 0, 2, 5, 1);
		GridPane.setConstraints(table, 0, 3);
		GridPane.setColumnSpan(table, 6);
		GridPane.setVgrow(table, Priority.ALWAYS);
		table.setEditable(true);
		root.getChildren().addAll(openButton, resetButton, choiceBox, searchField, compoundView, meetingView, table);
		ColumnConstraints col1Constraints = new ColumnConstraints();
		col1Constraints.setPercentWidth(16.66);
		root.getColumnConstraints().addAll(col1Constraints, col1Constraints, col1Constraints, col1Constraints, col1Constraints, col1Constraints);
		root.setVgap(25);
		root.setHgap(20);
		Scene scene = new Scene(root, 800, 800);
		scene.getStylesheets().add(getClass().getResource("socparser.css").toExternalForm());
		stage.setScene(scene);
		stage.setOnCloseRequest(e -> renderer.disconnect());
		stage.show();
	}
	
	private void populateTable(){
		ResultSet rs;
        if (!displayed) {
        	
        	// Gather list of hidden columns so they can be set to hidden again after repopulating table (at end of this function)
        	ArrayList<TableColumn> hiddenColumns = new ArrayList<TableColumn>();
        	for (int i = 0; i < table.getColumns().toArray().length; i++) {
        		if (table.getColumns().get(i).visibleProperty().getValue() == false) {
        			hiddenColumns.add(table.getColumns().get(i));
        		}
        	}
        	
        	// Construct the SQL query used to reorganize the soc data
    		data.removeAll(data);
    		table.getColumns().clear();
        	String[] labels = renderer.getColumnNames(fileName);
			String sql = "SELECT";
	
	        	for(int i = 0 ; i < labels.length; i++){
	        		String label = labels[i];
	        		if (label.equals("Class Nbr") || label.equals("Sect")) {
	        			sql += (" \"" + label + "\",");
	        		}
	        		else if (label.equals("Day/s")) {
	        			sql += (" GROUP_CONCAT(\"" + label + "\" ORDER BY \"Id\" SEPARATOR ' ') \"" + label + "\",");
	        		}
	        		else {
	        			sql += (" GROUP_CONCAT(DISTINCT \"" + label + "\" ORDER BY \"Id\" SEPARATOR ' ') \"" + label + "\",");
	        		}
	        	}
	        	sql = sql.substring(0, sql.length() - 1); // chop trailing comma
	        if (isCompoundMode) {
	        	sql += (" FROM \"" + fileName + "\" GROUP BY \"Class Nbr\", \"Sect\"");
	        }
	        else {
    			sql += " FROM (SELECT";
    			for(int i = 0 ; i < labels.length; i++){
	        		String label = labels[i];
	        		if (label.equals("Join")) {
	        			sql += (" \"" + label + "\",");
	        		}
	        		else if (label.equals("Day/s")) {
	        			sql += (" \"" + label + "\",");
	        		}
	        		else if (label.equals("Facility")) {
	        			sql += (" \"" + label + "\",");
	        		}
	        		else if (label.equals("Time")) {
	        			sql += (" \"" + label + "\",");
	        		}	        		
	        		else {
	        			sql += (" GROUP_CONCAT(DISTINCT \"" + label + "\" ORDER BY \"Id\" SEPARATOR ' ') AS \"" + label + "\",");
	        		}
	        	}
	        	sql = sql.substring(0, sql.length() - 1); // chop trailing comma
	        	sql += (" FROM \"" + fileName + "\" WHERE \"Join\" <> '' AND  \"Facility\" <> '' GROUP BY \"Day/s\", \"Facility\", \"Time\", \"Join\"");
    			sql += (" UNION ALL SELECT * FROM \"" + fileName + "\" WHERE \"Join\" = ''");
    			sql += (" UNION SELECT * FROM \"" + fileName + "\" WHERE \"Facility\" = ''");
    			sql += (") GROUP BY \"Class Nbr\", \"Sect\"");
    		}
        	System.out.println(sql);
        	try {
				rs = renderer.getConnection().createStatement().executeQuery(sql);
				if (!isCompoundMode) {
					
				}
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
		    		TableColumn<ObservableList<String>, String> col = new TableColumn<ObservableList<String>, String>();
		    		// Create the Event Handler for each column's header button (used to hide columns)
		    		EventHandler<ActionEvent> hideEvent = new EventHandler<ActionEvent>() {
		    			public void handle(ActionEvent e) {
		    				col.setVisible(false);
		    			}
		    		};
		    		
		    		// Create the button, set its text to the column label, attach the handler, then set its width so that the label text is never truncated
		    		Button hide = new Button(rsmd.getColumnLabel(i));
		    		hide.setOnAction(hideEvent);
		    		col.setGraphic(hide);
			        hide.setMinWidth(Button.USE_PREF_SIZE);
			        
			        // Create event handler for committing edit of Comment cell
			        EventHandler<CellEditEvent<ObservableList<String>, String>> commitComment = new EventHandler<CellEditEvent<ObservableList<String>, String>>() {
			        	public void handle(CellEditEvent<ObservableList<String>, String> e) {
			        		ObservableList<String> row = e.getRowValue();
			        		String idString = row.get(0);
			        		String[] ids = idString.split(" ");
			        		String[] comment = {e.getNewValue()};
			        		String[] column = {"Comments"};
			        		for (String id : ids) {
				        		try {
				        			renderer.update(fileName, id, column, comment);
				        		} catch(SQLException se) {
				        			se.printStackTrace();
				        		}		
			        		}
			        		displayed = false;
			        		populateTable();
			        	}
			        };
			        // Make Comment cells editable with textfield, and attach commit handler
			        if (((Button)col.getGraphic()).getText().equals("Comments")) {
			        	col.setCellFactory(TextFieldTableCell.forTableColumn());
			        	col.addEventHandler(TableColumn.editCommitEvent(), commitComment);
			        }
			        // Create event handler for committing edit of Instructor cell
			        EventHandler<CellEditEvent<ObservableList<String>, String>> commitInstructor = new EventHandler<CellEditEvent<ObservableList<String>, String>>() {
			        	public void handle(CellEditEvent<ObservableList<String>, String> e) {
			        		ObservableList<String> row = e.getRowValue();
			        		String idString = row.get(0);
			        		String[] ids = idString.split(" ");
			        		String[] instructor = {e.getNewValue()};
			        		String[] column = {"Instructor"};
			        		for (String id : ids) {
				        		try {
				        			renderer.update(fileName, id, column, instructor);
				        		} catch(SQLException se) {
				        			se.printStackTrace();
				        		}		
			        		}
			        		displayed = false;
			        		populateTable();
			        	}
			        };
			        // Make Instructor cells editable with textfield, and attach commit handler
			        if (((Button)col.getGraphic()).getText().equals("Instructor")) {
			        	col.setCellFactory(TextFieldTableCell.forTableColumn());
			        	col.addEventHandler(TableColumn.editCommitEvent(), commitInstructor);
			        }
			        
			        // Make our implementation-specific Id column always hidden
			        if (((Button)col.getGraphic()).getText().equals("Id")) {
			        	col.setVisible(false);
			        }
			        
		    		// Since we are retrieving data dynamically from a sql table, we cannot use PropertyValueFactory, so we use a Callback.
		    		// All cell value factories take a CellDataFeatures instance and return an ObservableValue.
		    		// Basically this stuff is stupid complicated so just follow the documentation and/or stackoverflow.
		    		col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList<String>,String>, ObservableValue<String>>(){
		    			public ObservableValue<String> call(CellDataFeatures<ObservableList<String>, String> param) {
		    				return new SimpleStringProperty(param.getValue().get(j).toString());
		    			}
		    		});	
		    		table.getColumns().addAll(col); 
		    	}
		
		    	// Gather data into ObservableList to add to populate table	    	
				while(rs.next()){
					ObservableList<String> row = FXCollections.observableArrayList();
					for(int i = 1 ; i <= rsmd.getColumnCount(); i++){
						row.add(rs.getString(i));
						if(rsmd.getColumnName(i) == "Comments") {
						}
					}
					//System.out.println("Row added " + row );
					data.add(row);
					for(int i = 1 ; i <= rsmd.getColumnCount(); i++){
						if(rsmd.getColumnName(i) == "Comments") {
							//data[i][].set
						}
					}					
				}
				
			} catch (SQLException se) {
				se.printStackTrace();
	        	System.err.println("Error populating table.");  
	        	System.err.println("Error retrieving from table " + fileName);  
			}        	
        	
        	bindSearchField();
	    	displayed = true;
	    	if (!root.getChildren().contains(dropButton)) {
		    	root.getChildren().add(dropButton);
	    	}
	    	autoResizeColumns();
	    	
        	// Ensure previously hidden columns remain hidden when table is repopulated (within same session ONLY)
        	for (int i = 0; i < table.getColumns().toArray().length; i++) {
        		for (TableColumn col : hiddenColumns) {
        		if (((Button) col.getGraphic()).getText().equals(((Button) table.getColumns().get(i).getGraphic()).getText())) { // I hate java
        			table.getColumns().get(i).setVisible(false);
        		}
        		}
        	}
        }
	}
	
	private void bindSearchField() {
		final List<TableColumn<ObservableList<String>, ?>> columns = table.getColumns();
	    FilteredList<ObservableList<String>> filteredData = new FilteredList<>(data);
	    filteredData.predicateProperty().bind(Bindings.createObjectBinding(() -> {
	        String text = searchField.getText();
	        if (text == null || text.isEmpty()) {
	            return null;
	        }
	        final String filterText = text.toLowerCase();
	        return o -> {
	            for (TableColumn<ObservableList<String>, ?> col : columns) {
	                ObservableValue<?> observable = col.getCellObservableValue(o);
	                if (observable != null) {
	                    Object value = observable.getValue();
	                    if (value != null && value.toString().toLowerCase().contains(filterText)) {
	                        return true;
	                    }
	                }
	            }
	            return false;
	        };
	    }, searchField.textProperty()));
	    SortedList<ObservableList<String>> sortedData = new SortedList<>(filteredData);
	    sortedData.comparatorProperty().bind(table.comparatorProperty());
	    table.setItems(sortedData);
	}
	
	private void autoResizeColumns() {
	    //Set the right policy
	    table.setColumnResizePolicy( TableView.UNCONSTRAINED_RESIZE_POLICY);
	    table.getColumns().stream().forEach( (column) -> {
	        //Minimal width = column header , which since it is a button, is considered a Graphic
	        Node n = column.getGraphic();
	        Text t = new Text(((Button)n).getText().toString());
	        final double max1 = t.getLayoutBounds().getWidth();
	        double max2 = max1;
	        for ( int i = 0; i < table.getItems().size(); i++ ) {
	            //cell must not be empty
	            if (column.getCellData(i) != null) {
	                t = new Text(column.getCellData( i ).toString());
	                double calcwidth = t.getLayoutBounds().getWidth();
	                //remember new max-width
	                if (calcwidth > max2) {
	                    max2 = calcwidth;
	                }
	            }
	        }
	        //set the new max-width with some extra space
	        column.setMinWidth(max1 + 30.0d); // min width is that of column header button
	        column.setPrefWidth(max2 + 30.0d); // default width is that of largest body cell content
	    });
	}
	
	private void parseAndRender() {
		String[][] dataArray = parser.parseFile(filePath);
		String[] labels = new String[dataArray[0].length + 2]; // plus two because we are adding an id column and a comments column
		int rowIndex = 0; // keeps track of the row number, so that row 0 is only used for column names, and to populate Id column
		for (String[] row : dataArray) {
			RowObject rowObject = new RowObject();
			// Retrieve the column labels from the parse result and store to use as key in rowObject
			if (rowIndex == 0) {
				labels[0] = "Id"; // We need the first column to be an Id (there is no ID in the parsed spreadsheet)
				for (int i = 0; i < row.length; i++) {
					labels[i + 1] = row[i];
				}
				// Add another column label for comments
				labels[labels.length - 1] = "Comments";
				// This is called here because it needs the column names, but need only be called once
				renderer.createTable(fileName, labels);
			}
			else {
				// create key/value pair for each cell
				rowObject.add(labels[0], String.valueOf(rowIndex)); // add our new Id entry for each row
				for (int i = 0; i < row.length; i++) {
					rowObject.add(labels[i + 1], row[i]); // everything else comes from the parsed sheet
				}
				rowObject.add(labels[labels.length - 1], "");
				// Only add row into database if it is not the label row
				renderer.insertRowInDB(fileName, rowObject);
			}
			++rowIndex;				
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
