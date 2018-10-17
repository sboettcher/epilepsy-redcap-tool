package epilepsy;

import epilepsy.util.ExceptionAlert;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.logging.Logger;

public class RedcapToolGuiController {
  private static final Logger LOGGER = Logger.getLogger( RedcapToolGuiController.class.getName() );
  private HostServices mHostServices;

  public RedcapToolGuiController() {
  }

  @FXML public void initialize() {
    LOGGER.info("Tool GUI initialized");
  }


  /* Button Handler */
  @FXML public void handleLoadDictAction(ActionEvent event) {
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

    alert.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("res/radarcns-logo.png"), 150, 100, true, true)));

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
