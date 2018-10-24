package epilepsy;

import epilepsy.redcap.DataLoader;
import epilepsy.redcap.DictionaryEntry;
import epilepsy.redcap.DictionaryLoader;
import epilepsy.util.ExceptionAlert;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static epilepsy.util.Statics.*;

public class RedcapToolGuiController {
  private static final Logger LOGGER = Logger.getLogger( RedcapToolGuiController.class.getName() );
  static {LOGGER.setLevel(loglvl);}

  @FXML private Label leftStatus;
  @FXML private Label rightStatus;

  @FXML private HBox statusHBox;
  @FXML private TreeView dictionaryTree;
  @FXML private TreeTableView<Map<String, String>> dataTreeTable;

  private ArrayList<DictionaryEntry> ddEntries;
  private ArrayList<HashMap<String, String>> dataEntries;

  private static ArrayList<String> defaultDataColumns;
  static {
    defaultDataColumns = new ArrayList<>();
    //defaultDataColumns.add("record_id");
    defaultDataColumns.add("patient_code");
    defaultDataColumns.add("start_date");
    defaultDataColumns.add("recording_end_date");
    defaultDataColumns.add("epi_type");
    defaultDataColumns.add("seizure_recorded");
    defaultDataColumns.add("rep_seizure_type");
  }


  public RedcapToolGuiController() {
    ddEntries = new ArrayList<>();
    dataEntries = new ArrayList<>();
  }

  @FXML public void initialize() {
    leftStatus.setText("");
    rightStatus.setText("");

    try {
      ddEntries = (ArrayList<DictionaryEntry>) DictionaryLoader.readFromResource("/DICT.csv");
      setDictionary(ddEntries);
    } catch (NullPointerException ex) {
      LOGGER.warning("Error loading dictionary from resource file during initialization.");
    }

    try {
      dataEntries = DataLoader.readFromResource("/DATA.csv");
      setData(ddEntries, dataEntries);
    } catch (IOException | NullPointerException ex) {
      LOGGER.warning("Error loading data from resource file during initialization.");
    }

    leftStatus.setText(String.format("Dictionary Size: %d", ddEntries.size()));
    leftStatus.setText(String.format("Data Size: %d", dataEntries.size()));

    LOGGER.info("Tool GUI initialized");
  }


  /* Dictionary Handler */

  private void setDictionary(ArrayList<DictionaryEntry> entries) {
    // single toplevel root item
    TreeItem<String> rootItem = new TreeItem<>("Data Dictionary");
    rootItem.setExpanded(true);
    dictionaryTree.setRoot(rootItem);

    if (entries == null || entries.size() < 1) return;

    // sort dictionary entries into instruments map
    ArrayList<String> instrumentOrder = new ArrayList<>();
    HashMap<String, ArrayList<DictionaryEntry>> instruments = new HashMap<>();
    for (DictionaryEntry de : entries) {
      if (!instruments.containsKey(de.getFormName())) {
        instruments.put(de.getFormName(), new ArrayList<>());
        instrumentOrder.add(de.getFormName());
      }
      instruments.get(de.getFormName()).add(de);
    }

    // create instrument nodes and add children
    for (String instrument : instrumentOrder) {
      TreeItem<String> instrumentRoot = new TreeItem<>(instrument);
      int instrumentIndex = rootItem.getChildren().indexOf(instrumentRoot);
      if (instrumentIndex >= 0) {
        instrumentRoot = rootItem.getChildren().get(instrumentIndex);
      }

      instrumentRoot.getChildren().clear();
      for (DictionaryEntry de : instruments.get(instrument)) {
        instrumentRoot.getChildren().add(new TreeItem<>(de.getFieldName()));
      }

      rootItem.getChildren().add(instrumentRoot);
    }

    rightStatus.setText(String.format("Dictionary Size: %d", entries.size()));
  }


  /* Data Handler */

  private void setData(ArrayList<DictionaryEntry> dictionary, ArrayList<HashMap<String, String>> data) {
    // create single toplevel root item
    final TreeItem<Map<String, String>> root = new TreeItem<>(Collections.singletonMap("patient_code", "PATIENTS"));
    root.setExpanded(true);
    dataTreeTable.setRoot(root);

    // add patient entries to root
    for (HashMap<String, String> recordMap : data) {
      if (!recordMap.get("patient_code").equals("")) {
        root.getChildren().add(new TreeItem<>(recordMap));
      }
      // add seizure entries to patients TODO: currently assumes seizure entries for a specific patient are immediately preceded by the patient entry (p1,p1s1,p1s2,p1s3,p2,p2s1,p3,p4,p4s1,p4s2,...)
      else if (recordMap.get("redcap_repeat_instrument").equals("seizures")) {
        root.getChildren().get(root.getChildren().size()-1).getChildren().add(new TreeItem<>(recordMap));
      }
    }

    // create columns
    ArrayList<TreeTableColumn<Map<String, String>, String>> columns = new ArrayList<>();
    for (DictionaryEntry dictionaryColumn : dictionary) {
      TreeTableColumn<Map<String, String>, String> nextColumn = new TreeTableColumn<>(dictionaryColumn.getFieldLabel());
      nextColumn.setCellValueFactory(
          (TreeTableColumn.CellDataFeatures<Map<String, String>, String> param) -> new ReadOnlyStringWrapper(param.getValue().getValue().get(dictionaryColumn.getFieldName()))
      );
      if (!defaultDataColumns.contains(dictionaryColumn.getFieldName()))
        nextColumn.setVisible(false);
      columns.add(nextColumn);
    }

    dataTreeTable.getColumns().setAll(columns);
    dataTreeTable.setShowRoot(false);
    dataTreeTable.setTableMenuButtonVisible(true);

    leftStatus.setText(String.format("Data Size: %d", data.size()));
  }



  /* Button Handler */
  @FXML public void handleLoadDictAction(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Data Dictionary File");
    File dictfile = fileChooser.showOpenDialog(statusHBox.getScene().getWindow());
    try {
      ddEntries = (ArrayList<DictionaryEntry>) DictionaryLoader.readFromFile(dictfile);
      setDictionary(ddEntries);
    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
      Alert alert = new ExceptionAlert(ex);
      alert.setContentText("Exception while trying to open a dictionary file at\n" + dictfile.getPath());
      alert.showAndWait();
    }
  }

  @FXML public void handleLoadDataAction(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open REDCap Data File");
    File datafile = fileChooser.showOpenDialog(statusHBox.getScene().getWindow());
    try {
      dataEntries = DataLoader.readFromFile(datafile);
      setDictionary(ddEntries);
    } catch (IOException ex) {
      ex.printStackTrace();
      Alert alert = new ExceptionAlert(ex);
      alert.setContentText("Exception while trying to open a data file at\n" + datafile.getPath());
      alert.showAndWait();
    }
  }

  @FXML void handleExitAction(ActionEvent event) {
    Platform.exit();
  }

  @FXML void handleAboutAction(ActionEvent event) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("About Epilepsy REDCap Tool");
    alert.setHeaderText("Epilepsy REDCap Tool");

    VBox vb = new VBox();
    Label lbl = new Label(String.format("\nAs part of the european research programme RADAR-CNS\nWP4 - Epilepsy\n\nJava: %s\nJavaFX: %s\n\nLog Level: %s\nBuild: %s@%s", Runtime.class.getPackage().getImplementationVersion(), com.sun.javafx.runtime.VersionInfo.getRuntimeVersion(), loglvl, buildversion, buildtime));
    Hyperlink link1 = new Hyperlink("https://radar-cns.org");
    Hyperlink link2 = new Hyperlink("https://radar-base.org");
    vb.getChildren().addAll( link1, link2, lbl );

    link1.setOnAction( (evt) -> {
      openBrowser(link1.getText());
    } );
    link2.setOnAction( (evt) -> {
      openBrowser(link2.getText());
    } );

    alert.getDialogPane().contentProperty().set( vb );
    alert.setContentText(String.format("RADAR-CNS\n\nJava: %s\nJavaFX: %s\nradar-cns.org\nradar-base.org", Runtime.class.getPackage().getImplementationVersion(), com.sun.javafx.runtime.VersionInfo.getRuntimeVersion()));

    alert.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/radarcns-logo.png"), 150, 100, true, true)));

    alert.showAndWait();
  }


  /* Miscellaneous */
  private void openBrowser(String url) {
    LOGGER.info("Opening webbrowser at " + url);
    try {
      if (System.getProperty("os.name").toLowerCase().contains("win")) {
        // TODO: open correct process for windows
        throw new UnsupportedOperationException("Trying to open browser in windows, not implemented yet.");
      } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
        // TODO: open correct process for mac
        throw new UnsupportedOperationException("Trying to open browser on mac, not implemented yet.");
      } else {
        new ProcessBuilder("x-www-browser", url).start();
      }
    } catch (IOException | UnsupportedOperationException ex) {
      ex.printStackTrace();
      Alert alert = new ExceptionAlert(ex);
      alert.setContentText("Exception while trying to open a webbrowser at\n" + url);
      alert.showAndWait();
    }
  }
}
