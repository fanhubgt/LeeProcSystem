/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package leesystem;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author appiah
 */
public class VAIFileFilter implements FileFilter {

   
    public boolean accept(File pathname) {
        if (pathname.toString().endsWith(".vai")) {
            return true;
        }
        return false;
          
    }
}
