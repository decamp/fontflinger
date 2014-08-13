/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.util;

import bits.util.Files;

import java.io.*;

/**
 * @author Philip DeCamp
 */
public class PfToAfm {

    /**
     * Attempts to generate an AFM (Adobe File Metrics) file from a PFB/PFA font
     * file. If the input file has an accompaying PFM file (that is, if your
     * font file is "font.pfb", the PFM file would be named "font.pfm" and be
     * located in the same directory), this file will also be used and will
     * augment the metrics data of the PFB/PFA font.
     * <p>
     * Uses "gs_partial", which is a partial build of GhostScript, to perform
     * conversion.
     * <p>
     * If a corresponding AFM file already exists, this method will simply
     * return the existing file, rather than attempt to generate a new one.
     * 
     * @param pfFontFile A pfa or pfb font file.
     * @return A afm file containing the font metrics.
     */
    public static File pfToAfm( File pfFontFile ) throws IOException {
        File outFile = Files.setSuffix( pfFontFile, "afm" );
        if( outFile.exists() ) {
            return outFile;
        }

        File binDir = new File( "bin" );
        String[] exec = {
                new File( binDir, "gs_partial" ).getAbsolutePath(),
                "-dNODISIPLAY",
                "--",
                new File( binDir, "pf2afm.ps" ).getAbsolutePath(),
                pfFontFile.getAbsolutePath()
        };

        int err;

        try {
            Process p = Runtime.getRuntime().exec( exec );
            err = p.waitFor();
        } catch( InterruptedException ex ) {
            InterruptedIOException e = new InterruptedIOException();
            e.initCause( ex );
            throw e;
        }

        if( outFile.exists() ) {
            return outFile;
        }

        throw new IOException( "Failed to generate afm file. Error: " + err );
    }

}
