/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package shareonclient;

/**
 *
 * @author Bandita
 */
import java.io.File;
import javax.swing.filechooser.FileFilter;

// class to filter files
// currently it only accepts .shareon files (and directorys of course)
class ShareOnFileFilter extends FileFilter
    {
    private String extension;
    private String description;
    
    public ShareOnFileFilter()
        {
        extension = ".shareon";
        description = "(*.shareon) ShareOn client files";
        }

    public boolean accept(File f)
        {
        // We always allow directories, regardless of their extension
        if (f.isDirectory()) { return true; }
        // Ok, its a regular file, so check the extension
        String name = f.getName().toLowerCase();
        if (name.endsWith(extension))
            { return true; }
        return false;
        }
    
    public String getDescription() { return description; }
    }
