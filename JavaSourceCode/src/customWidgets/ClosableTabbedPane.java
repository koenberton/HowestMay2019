// Tnaks to https://community.oracle.com/thread/1501884

package customWidgets;

import java.awt.Component;
import javax.swing.JTabbedPane;

/**
* Klasse waarmee een JTabbedPane gemaakt kan worden met tabbladen
* die afzonderlijk afgesloten kunnen worden.
*/
public class ClosableTabbedPane extends JTabbedPane {

/**
* Constructor.
*/
public ClosableTabbedPane() {
super();
super.addMouseListener(new TabbedPaneListener());
}

/**
* Overriding van de methode. Er wordt een icon getoond waarmee
* het tabblad gesloten kan worden.
*
* @param title the title to be displayed in this tab
* @param component the component to be displayed when this tab is clicked
* @return the component
*/
public Component add(int index, String title,Component component) {
 super.add(title, component);
 super.setIconAt(index, new CloseIcon());
 return component;
 }
}




 