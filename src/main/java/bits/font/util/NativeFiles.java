package bits.font.util;

import java.io.*;


/**
 * @author Philip DeCamp
 */
public class NativeFiles {

    /**
     * Copies a file using the "cp" command, which will preserve the resource fork on OS X machines.
     */
    public static void copy( File src, File dst ) throws IOException {
        String[] s = new String[]{ "cp", src.getAbsolutePath(), dst.getAbsolutePath() };
        Process p = Runtime.getRuntime().exec( s );

        try {
            int err = p.waitFor();
            p.destroy();
            if( err != 0 ) {
                throw new IOException( "Failed to copy file: " + err );
            }
        } catch( InterruptedException ex ) {
            InterruptedIOException e = new InterruptedIOException();
            e.initCause( ex );
            throw e;
        }

    }

}
