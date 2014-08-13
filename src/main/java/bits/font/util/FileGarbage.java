package bits.font.util;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

import bits.util.ref.AbstractRefable;


/**
 * Utility class for managing files and directories and ensuring they are deleted appropriated.
 *
 * @author decamp
 */
public class FileGarbage extends AbstractRefable {

    private static Comparator<File> LENGTH_COMP = new Comparator<File>() {
        public int compare( File a, File b ) {
            if( a == b || a.equals( b ) ) {
                return 0;
            }

            String p1 = a.getAbsolutePath();
            String p2 = b.getAbsolutePath();

            if( p1.length() < p2.length() ) {
                return 1;
            }

            if( p1.length() > p2.length() ) {
                return -1;
            }

            return p1.compareTo( p2 );
        }
    };

    private static FileFilter UNHIDDEN_FILTER = new FileFilter() {
        public boolean accept( File file ) {
            return !file.isHidden();
        }
    };


    private List<File> mList = new ArrayList<File>();


    public FileGarbage() {}


    public void addFile( File file, boolean killIfExists ) {
        if( file.exists() && !killIfExists ) {
            return;
        }

        mList.add( file );
        file.deleteOnExit();
    }

    public void remove( File file ) {
        mList.remove( file );
    }

    public void empty() {
        List<File> list = new ArrayList<File>( mList );
        Collections.sort( list, LENGTH_COMP );

        deleteFiles( list );
        deleteDirs( list );
    }


    static void deleteFiles( List<File> files ) {
        for( File f : files ) {
            if( f.isFile() ) {
                f.delete();
            }
        }
    }

    static void deleteDirs( List<File> files ) {
        for( File f : files ) {
            if( f.isDirectory() ) {
                File[] children = f.listFiles( UNHIDDEN_FILTER );

                if( children == null || children.length == 0 ) {
                    f.delete();
                }
            }
        }
    }


    protected void freeObject() {
        empty();
    }

}
