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

  private DictionaryLoader mDictionary;
  private DataLoader mData;

  private boolean mAllExpanded = false;
  private static final String mDefaultSortColumn = "patient_code";
  private static final ArrayList<String> mDefaultDataColumns;
  static {
    mDefaultDataColumns = new ArrayList<>();
    //mDefaultDataColumns.add("record_id");
    mDefaultDataColumns.add("patient_code");
    mDefaultDataColumns.add("start_date");
    mDefaultDataColumns.add("recording_end_date");
    mDefaultDataColumns.add("seizure_recorded");
    mDefaultDataColumns.add("rep_seizure_type");
    mDefaultDataColumns.add("rep_seizure_num");
  }


  public RedcapToolGuiController() {
    try {
      mProperties.load(getClass().getResourceAsStream("/project.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML public void initialize() {
    leftStatus.setText("");
    rightStatus.setText("");

    dataTreeTable.setPlaceholder(new Label("No data loaded.\n\nTo begin:\n1. Load a Data Dictionary if the default is out-of-date.\n2. Load data from an exported CSV file.\n3. Select columns in the top right (\"+\")."));

    try {
      mDictionary = new DictionaryLoader("/DICT.csv");
      setDictionary();
    } catch (NullPointerException ex) {
      LOGGER.warning("Error loading dictionary from resource file during initialization.");
    }

    try {
      mData = new DataLoader("/DATA.csv");
      setData();
    } catch (IOException | NullPointerException ex) {
      LOGGER.warning("Error loading data from resource file during initialization.");
    }

    LOGGER.info("Tool GUI initialized");
  }


  /* Dictionary Handler */

  private void setDictionary() {
    // single toplevel root item
    TreeItem<String> rootItem = new TreeItem<>("Data Dictionary");
    rootItem.setExpanded(true);
    dictionaryTree.setRoot(rootItem);

    if (mDictionary.getEntries() == null || mDictionary.getEntries().size() < 1) return;

    // create instrument nodes and add children
    for (String instrument : mDictionary.getInstrumentOrder()) {
      TreeItem<String> instrumentRoot = new TreeItem<>(instrument);
      int instrumentIndex = rootItem.getChildren().indexOf(instrumentRoot);
      if (instrumentIndex >= 0) {
        instrumentRoot = rootItem.getChildren().get(instrumentIndex);
      }

      instrumentRoot.getChildren().clear();
      for (DictionaryEntry de : mDictionary.getInstruments().get(instrument)) {
        instrumentRoot.getChildren().add(new TreeItem<>(de.getFieldName()));
      }

      rootItem.getChildren().add(instrumentRoot);
    }

    rightStatus.setText(String.format("Dictionary Size: %d", mDictionary.getEntries().size()));
  }


  /* Data Handler */

  private void setData() {
    if (mDictionary.getEntries().isEmpty() || mData.getData().isEmpty()) {
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
    for (HashMap<String, String> recordMap : mData.getData()) {
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
    for (DictionaryEntry dictionaryColumn : mDictionary.getEntries()) {
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
      if (!mDefaultDataColumns.contains(dictionaryColumn.getFieldName()))
        nextColumn.setVisible(false);
      if (dictionaryColumn.getFieldName().equals(mDefaultSortColumn)) {
        nextColumn.setSortType(TreeTableColumn.SortType.ASCENDING);
        dataTreeTable.getSortOrder().add(nextColumn);
      }
      columns.add(nextColumn);
    }

    dataTreeTable.getColumns().setAll(columns);
    dataTreeTable.sort();

    leftStatus.setText(String.format("Data Size: %d", mData.getData().size()));
  }



  /* Button Handler */

  @FXML void handleExpandAllAction(ActionEvent event) {
    if (dataTreeTable.getRoot() == null)
      return;
    mAllExpanded = !mAllExpanded;
    for (TreeItem<Map<String, String>> row : dataTreeTable.getRoot().getChildren()) {
      row.setExpanded(mAllExpanded);
    }
  }



  /* Menu Handler */

  @FXML public void handleLoadDictAction(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Data Dictionary File");
    File dictfile = fileChooser.showOpenDialog(statusHBox.getScene().getWindow());
    try {
      mDictionary = new DictionaryLoader(dictfile);
      setDictionary();
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
      mData = new DataLoader(datafile);
      setData();
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
