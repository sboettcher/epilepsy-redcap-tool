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
import java.util.*;
import java.util.logging.Logger;

import static epilepsy.util.Statics.loglvl;

public class RedcapToolGuiController {
  private static final Logger LOGGER = Logger.getLogger( RedcapToolGuiController.class.getName() );
  static {LOGGER.setLevel(loglvl);}

  private final Properties mProperties = new Properties();

  @FXML private Label leftStatus;
  @FXML private Label rightStatus;

  @FXML private HBox statusHBox;
  @FXML private TreeView dictionaryTree;
  @FXML private TreeTableView<Map<String, String>> dataTreeTable;

  private ArrayList<DictionaryEntry> ddEntries;
  private ArrayList<HashMap<String, String>> dataEntries;

  private boolean currentAllExpanded = false;
  private static final String defaultSortColumn = "patient_code";
  private static final ArrayList<String> defaultDataColumns;
  static {
    defaultDataColumns = new ArrayList<>();
    //defaultDataColumns.add("record_id");
    defaultDataColumns.add("patient_code");
    defaultDataColumns.add("start_date");
    defaultDataColumns.add("recording_end_date");
    defaultDataColumns.add("seizure_recorded");
    defaultDataColumns.add("rep_seizure_type");
    defaultDataColumns.add("rep_seizure_num");
  }


  public RedcapToolGuiController() {
    try {
      mProperties.load(getClass().getResourceAsStream("/project.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    ddEntries = new ArrayList<>();
    dataEntries = new ArrayList<>();
  }

  @FXML public void initialize() {
    leftStatus.setText("");
    rightStatus.setText("");

    dataTreeTable.setPlaceholder(new Label("No data loaded.\n\nTo begin:\n1. Load a Data Dictionary if the default is out-of-date.\n2. Load data from an exported CSV file.\n3. Select columns in the top right (\"+\")."));

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
    if (dictionary.isEmpty() || data.isEmpty()) {
      LOGGER.warning("Empty dictionary or data.");
      return;
    }

    // create single toplevel root item
    final TreeItem<Map<String, String>> root = new TreeItem<>(Collections.singletonMap("patient_code", "ROOT"));
    root.setExpanded(true);
    dataTreeTable.setRoot(root);
    dataTreeTable.setShowRoot(false);
    dataTreeTable.setTableMenuButtonVisible(true);
    dataTreeTable.setSortMode(TreeSortMode.ONLY_FIRST_LEVEL);

    // add patient entries to root
    for (HashMap<String, String> recordMap : data) {
      if (!recordMap.get("patient_code").equals("")) {
        root.getChildren().add(new TreeItem<>(recordMap));
      }
      // add seizure entries to patients
      // TODO: currently assumes seizure entries for a specific patient are immediately preceded by the patient entry (p1,p1s1,p1s2,p1s3,p2,p2s1,p3,p4,p4s1,p4s2,...)
      else if (recordMap.get("redcap_repeat_instrument").equals("seizures")) {
        TreeItem<Map<String, String>> seizureItem = new TreeItem<>(recordMap);
        root.getChildren().get(root.getChildren().size()-1).getChildren().add(seizureItem);
      }
    }

    // create columns
    ArrayList<TreeTableColumn<Map<String, String>, String>> columns = new ArrayList<>();
    for (DictionaryEntry dictionaryColumn : dictionary) {
      TreeTableColumn<Map<String, String>, String> nextColumn = new TreeTableColumn<>(dictionaryColumn.getFieldLabel().equals("") ? dictionaryColumn.getFieldName() : dictionaryColumn.getFieldLabel());
      if (dictionaryColumn.getFieldType().equals("checkbox")) {
        nextColumn.setCellValueFactory(cellData -> {
          // get checked choice indices and collect labels
          ArrayList<String> checkedLabels = new ArrayList<>();
          for (int index : dictionaryColumn.getChoices().keySet()) {
            String cellValue = cellData.getValue().getValue().get(String.format("%s___%d", dictionaryColumn.getFieldName(), index));
            if (cellValue != null && !cellValue.equals("") && !cellValue.equals("0"))
              checkedLabels.add(dictionaryColumn.getChoices().get(index));
          }

          // create string to display
          String cellContent = String.join(" | ", checkedLabels);

          if (cellContent.equals(""))
            return new ReadOnlyStringWrapper("");
          else
            return new ReadOnlyStringWrapper(cellContent);
        });
      } else if (dictionaryColumn.getFieldType().equals("radio")) {
        nextColumn.setCellValueFactory(cellData -> {
          String fieldValue = cellData.getValue().getValue().get(dictionaryColumn.getFieldName());
          if (fieldValue == null || fieldValue.equals(""))
            return new ReadOnlyStringWrapper("");
          else
            return new ReadOnlyStringWrapper(dictionaryColumn.getChoices().get(Integer.parseInt(fieldValue)));
        });
      } else if (dictionaryColumn.getFieldType().equals("yesno")) {
        nextColumn.setCellValueFactory(cellData -> {
          String fieldValue = cellData.getValue().getValue().get(dictionaryColumn.getFieldName());
          if (fieldValue == null || fieldValue.equals(""))
            return new ReadOnlyStringWrapper("");
          else
            return new ReadOnlyStringWrapper(fieldValue.equals("0") ? "no" : "yes");
        });
      } else {
        nextColumn.setCellValueFactory(
            (TreeTableColumn.CellDataFeatures<Map<String, String>, String> param) -> new ReadOnlyStringWrapper(param.getValue().getValue().get(dictionaryColumn.getFieldName()))
        );
      }
      if (!defaultDataColumns.contains(dictionaryColumn.getFieldName()))
        nextColumn.setVisible(false);
      if (dictionaryColumn.getFieldName().equals(defaultSortColumn)) {
        nextColumn.setSortType(TreeTableColumn.SortType.ASCENDING);
        dataTreeTable.getSortOrder().add(nextColumn);
      }
      columns.add(nextColumn);
    }

    dataTreeTable.getColumns().setAll(columns);
    dataTreeTable.sort();

    leftStatus.setText(String.format("Data Size: %d", data.size()));
  }



  /* Button Handler */

  @FXML void handleExpandAllAction(ActionEvent event) {
    if (dataTreeTable.getRoot() == null)
      return;
    currentAllExpanded = !currentAllExpanded;
    for (TreeItem<Map<String, String>> row : dataTreeTable.getRoot().getChildren()) {
      row.setExpanded(currentAllExpanded);
    }
  }



  /* Menu Handler */

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
      alert.showAndWait();
    }
  }

  @FXML public void handleLoadDataAction(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open REDCap Data File");
    File datafile = fileChooser.showOpenDialog(statusHBox.getScene().getWindow());
    try {
      dataEntries = DataLoader.readFromFile(datafile);
      setData(ddEntries, dataEntries);
    } catch (IOException ex) {
      ex.printStackTrace();
      Alert alert = new ExceptionAlert(ex);
      alert.showAndWait();
    }
  }

  @FXML void handleExitAction(ActionEvent event) {
    Platform.exit();
  }

  @FXML void handleAboutAction(ActionEvent event) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("About " + mProperties.getProperty("name"));
    alert.setHeaderText(mProperties.getProperty("name"));

    VBox vb = new VBox();
    Label lbl = new Label(String.format("\nAs part of the european research programme RADAR-CNS\nWP4 - Epilepsy\n\nJava: %s\nJavaFX: %s\n\nLog Level: %s\nBuild: %s @ %s",
        Runtime.class.getPackage().getImplementationVersion(), com.sun.javafx.runtime.VersionInfo.getRuntimeVersion(), loglvl, mProperties.getProperty("buildversion"), mProperties.getProperty("buildstamp")));
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
    alert.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/radarcns-logo.png"), 150, 100, true, true)));

    alert.showAndWait();
  }

  @FXML void handleKCLLinkAction(ActionEvent event) {
    openBrowser("https://radar-redcap.rosalind.kcl.ac.uk/redcap/redcap_v7.4.10/DataEntry/record_status_dashboard.php?pid=15");
  }
  @FXML void handleUKLFRLinkAction(ActionEvent event) {
    openBrowser("https://stuz-redcap.ukl.uni-freiburg.de/redcap_v8.5.5/DataEntry/record_status_dashboard.php?pid=36");
  }


  /* Miscellaneous */
  private void openBrowser(String url) {
    LOGGER.info("Opening webbrowser at " + url);
    try {
      if (System.getProperty("os.name").toLowerCase().contains("win")) {
        Runtime rt = Runtime.getRuntime();
        rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
      } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
        Runtime rt = Runtime.getRuntime();
        rt.exec("open " + url);
      } else {
        new ProcessBuilder("x-www-browser", url).start();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
      Alert alert = new ExceptionAlert(ex);
      alert.showAndWait();
    }
  }

}
