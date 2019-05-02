package customWidgets;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.UIManager;

/**
* Deze klasse tekent een kruisje en houdt de absolute coordinaten
* van de afbeelding vast.
*/
public class CloseIcon implements Icon {
/**
* Grootte van het kruis.
*/
private int SIZE = 8;
/**
* De X coordinaat van de linkerbovenhoek van het
* icoontje waarin
* het kruis getoond wordt.
*/
private int x_coordinaat;
/**
* De Y coordinaat van de linkerbovenhoek van het icoontje
* waarin
* het kruis getoond wordt.
*/
private int y_coordinaat;
/**
* Het vierkant waarbinnen het icoontje getoond is en met de
* muis geklikt kan worden om het tabblad te sluiten.
*/
private Rectangle iconRect;

/**
* De opvul kleur voor het pijltje.
*/
private Color COLOR_CROSS = Color.RED;  //UIManager.getColor("BLUE");


/**
* Constructor.
*/
public CloseIcon() {
}

/**
* Tekent het icon.
* De links-boven hoek van het icoon is het punt
* (<code>x</code>, <code>y</code>)
*
* @param c Het component waar dit icoon op geplaatst wordt
* @param g De graphics
* @param x De X coordinaat van de links-boven hoek van het
* icoon
* @param y De Y coordinaat van de links-boven hoek van het
* icoon
*/
public void paintIcon(Component c, Graphics g, int x, int y) {
this.x_coordinaat = x;
this.y_coordinaat = y;
drawCross(g, x, y);
iconRect = new Rectangle(x, y, SIZE, SIZE);
}

/**
* Tekent het kruisje.
*
* @param g Het graphics component
* @param xo De x-coordinaat van het begin punt
* @param yo De y-coordinaat van het begin punt
*/
private void drawCross(Graphics g, int xo, int yo) {
// default kleur
g.setColor(COLOR_CROSS);
g.drawLine(xo, yo, xo+SIZE, yo+SIZE); // =\\.\
g.drawLine(xo, yo+1, xo+(SIZE-1), yo+SIZE); // =\.\\
g.drawLine(xo, yo+SIZE, xo+SIZE, yo); // = /.//
g.drawLine(xo, yo+(SIZE-1), xo+(SIZE-1), yo); // = .///
g.drawLine(xo+1, yo, xo+SIZE, yo+(SIZE-1)); // =\\\.
g.drawLine(xo+1, yo+SIZE, xo+SIZE, yo+1); // = ///.

// wit
g.setColor(Color.WHITE);
g.drawLine(xo+2, yo, xo+4, yo+2); // =\\\.
g.drawLine(xo+6, yo+4, xo+SIZE, yo+6); // =\\\.
g.drawLine(xo+2, yo+SIZE, xo+4, yo+6); // = //./
g.drawLine(xo+6, yo+4, xo+SIZE, yo+2); // = //./
//System.out.println("getekend");
}

/**
* Bepaald of 2 coordinaten zich in de afbeelding van het icoon
* bevinden.
*
* @param x Het te controleren x coordinaat
* @param y Het te controleren y coordinaat
* @return True indien de coordinaten zich binnen het icoon
* bevinden
*/
public boolean coordinatenInIcon(int x, int y) {
boolean isInIcon = false;
if (iconRect != null) {
isInIcon = iconRect.contains(x,y);
}
return isInIcon;
}

/**
* Vraagt de breedte van het pijltje op.
* @return De breedte van het pijltje;
*/
public int getIconWidth() {
return SIZE;
}

/**
* Vraagt de hoogte van het pijltje op.
* @return De hoogte van het pijltje;
*/
public int getIconHeight() {
return SIZE;
}
}