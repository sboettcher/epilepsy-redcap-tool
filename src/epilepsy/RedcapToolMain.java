package epilepsy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.logging.Logger;



public class RedcapToolMain extends Application {
  private static final Logger LOGGER = Logger.getLogger( RedcapToolMain.class.getName() );

  private RedcapToolGuiController mUIController;

  private static RedcapToolMain instance;
  public RedcapToolMain() {
    instance = this;
  }
  public static RedcapToolMain getInstance() {
    return instance;
  }

  static {
    Locale.setDefault(new Locale("en","EN"));
    System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS] [%4$s %2$s] %5$s%6$s%n");
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader tool_ui_loader = new FXMLLoader(getClass().getResource("redcap_tool_gui.fxml"));
    Parent tool_ui_root = tool_ui_loader.load();
    mUIController = tool_ui_loader.getController();

    primaryStage.setTitle("Epilepsy REDCap Tool");
    primaryStage.setScene(new Scene(tool_ui_root, 1280, 720));
    primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("res/radarcns-logo-small-bg.png")));
    primaryStage.show();
  }


  public static void main(String[] args) {
    launch(args);
  }
}

