package customWidgets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTabbedPane;

/**
* MouseListener voor sluiten van een tabblad.
*/
public class TabbedPaneListener extends MouseAdapter {

/**
* Invoked when a mouse button has been released on a component.
*/
public void mouseReleased( MouseEvent e ) {
  super.mouseReleased(e);
  ClosableTabbedPane tabPane = (ClosableTabbedPane) e.getSource();
  if (tabPane.isEnabled()) {
  int tabIndex =
  getTabByCoordinate(
  (JTabbedPane)tabPane, e.getX(), e.getY());
  if (tabIndex >= 0 && tabPane.isEnabledAt(tabIndex)) {
  CloseIcon closeIcon = (CloseIcon)tabPane.getIconAt(tabIndex);
  if (closeIcon.coordinatenInIcon(e.getX(), e.getY())) {
	
	//System.err.println("geklikt");
	/*
new JOptionPane().showMessageDialog(
this,
"Tabblad '"+tabPane.getTitleAt(tabIndex)
+"' werd gesloten",
"Er is in het icoon geklikt",
JOptionPane.INFORMATION_MESSAGE);

*/
}
}
}
}

/**
* Bepaalt a.d.h.v. een coordinaat de index van het gekozen
* tabbblad.
*
* @param pane Het panel met de tabbladen
* @param x De X coordinaat
* @param y De Y coordinaat
* @return De index van het tabblad
*/
public int getTabByCoordinate(JTabbedPane pane, int x, int y) {
Point p = new Point(x, y);

int tabCount = pane.getTabCount();
for (int i = 0; i < tabCount; i++) {
if (pane.getBoundsAt(i).contains(p.x, p.y)) {
return i;
}
}
return -1;
}
}