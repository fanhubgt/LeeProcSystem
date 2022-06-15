/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package leesystem;

import java.awt.Graphics;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 *
 * @author appiah
 */
public class VarHighLighter implements Highlighter{

    public void install(JTextComponent c) {
           
    }

    public void deinstall(JTextComponent c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void paint(Graphics g) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object addHighlight(int p0, int p1, HighlightPainter p) throws BadLocationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeHighlight(Object tag) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAllHighlights() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void changeHighlight(Object tag, int p0, int p1) throws BadLocationException {
       
    }
    
    public Highlight[] getHighlights() {
        return null;
    }

}
