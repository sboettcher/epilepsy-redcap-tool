package epilepsy;

import epilepsy.redcap.DictionaryEntry;
import epilepsy.redcap.DictionaryLoader;
import epilepsy.util.ExceptionAlert;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import sun.reflect.generics.tree.Tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.logging.Logger;

import static epilepsy.util.Statics.loglvl;

public class RedcapToolGuiController {
  private static final Logger LOGGER = Logger.getLogger( RedcapToolGuiController.class.getName() );
  static {LOGGER.setLevel(loglvl);}

  @FXML private HBox statusHBox;
  @FXML private TreeView dictionaryTree;
  @FXML private TreeTableView dataTreeTable;

  private ArrayList<DictionaryEntry> ddEntries;


  public RedcapToolGuiController() {
    ddEntries = new ArrayList<>();
  }

  @FXML public void initialize() {
    ddEntries = (ArrayList<DictionaryEntry>) DictionaryLoader.readFromResource("/DICT.csv");
    setDictionary(ddEntries);
    LOGGER.info("Tool GUI initialized");
  }


  /* Dictionary Handler */

  private void setDictionary(ArrayList<DictionaryEntry> entries) {
    dictionaryTree.setRoot(null);
    if (entries == null || entries.size() < 1) return;
    // TODO: set dictionary view
  }



  /* Button Handler */
  @FXML public void handleLoadDictAction(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Data Dictionary File");
    File dictfile = fileChooser.showOpenDialog(statusHBox.getScene().getWindow());
    try {
      ddEntries = (ArrayList<DictionaryEntry>) DictionaryLoader.readFromFile(dictfile);
    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
      Alert alert = new ExceptionAlert(ex);
      alert.setContentText("Exception while trying to open a dictionary file at\n" + dictfile.getPath());
      alert.showAndWait();
    }
  }

  @FXML public void handleLoadDataAction(ActionEvent event) {
  }

  @FXML void handleExitAction(ActionEvent event) {
    Platform.exit();
  }

  @FXML void handleAboutAction(ActionEvent event) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("About Epilepsy REDCap Tool");
    alert.setHeaderText("Epilepsy REDCap Tool");

    VBox vb = new VBox();
    Label lbl = new Label(String.format("\nAs part of the european research programme RADAR-CNS\nWP4 - Epilepsy\n\nJava: %s\nJavaFX: %s", Runtime.class.getPackage().getImplementationVersion(), com.sun.javafx.runtime.VersionInfo.getRuntimeVersion()));
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
