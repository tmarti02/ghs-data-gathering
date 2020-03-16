package gov.epa.ghs_data_gathering.Utilities;

import java.io.File;


/**
* Java Program to delete directory with sub directories and files on it 
* In this example, we have a directory tree as one/ abc.txt
* two/ cde.txt and we will try to delete top level directory one here.
*
* @author Javin Paul
*/
public class FolderDelete {


    /*
     * Right way to delete a non empty directory in Java
    */
    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i]);
                if (!success) {
                    return false;
                }
            }
        }

        // either file or an empty directory
        System.out.println(dir.getAbsolutePath());
        return dir.delete();
    }

    
    
    public static void main(String args[]) {
        
        deleteDirectory(new File("L:\\Priv\\Cin\\NRMRL\\CompTox\\MyToxicity")); //right way to remove directory in Java                              

    }

}