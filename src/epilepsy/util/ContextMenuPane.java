package epilepsy.util;

import javafx.scene.control.ContextMenu;
import javafx.scene.layout.Pane;

public class ContextMenuPane extends Pane {

  private ContextMenu contextMenu;

  public ContextMenu getContextMenu() {
    return contextMenu;
  }

  public void setContextMenu(ContextMenu contextMenu) {
    this.contextMenu = contextMenu;
  }
}
