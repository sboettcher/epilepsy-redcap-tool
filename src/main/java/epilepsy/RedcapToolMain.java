package epilepsy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static epilepsy.util.Statics.loglvl;


public class RedcapToolMain extends Application {
  private static final Logger LOGGER = Logger.getLogger( RedcapToolMain.class.getName() );
  static {LOGGER.setLevel(loglvl);}

  private final Properties mProperties = new Properties();

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
    Arrays.stream(LogManager.getLogManager().getLogger("").getHandlers()).forEach(h -> h.setLevel(loglvl));
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    mProperties.load(getClass().getResourceAsStream("/project.properties"));

    FXMLLoader tool_ui_loader = new FXMLLoader(getClass().getResource("/redcap_tool_gui.fxml"));
    Parent tool_ui_root = tool_ui_loader.load();
    mUIController = tool_ui_loader.getController();

    primaryStage.setTitle(mProperties.getProperty("name") + " | " + mProperties.getProperty("buildversion"));
    primaryStage.setMaximized(true);
    primaryStage.setScene(new Scene(tool_ui_root, 1920, 1080));
    primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/radarcns-logo-small-bg.png")));
    primaryStage.show();
  }


  public static void main(String[] args) {
    launch(args);
  }
}

