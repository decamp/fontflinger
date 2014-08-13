/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font;

import java.io.*;
import java.util.*;

import bits.font.util.*;
import bits.util.Files;


/**
 * Unpacks a single file to create a single file for each font.
 * 
 * @author Philip DeCamp
 */
public final class FontUnpacker {

    public static List<File> unpack( File file, FileGarbage garbage ) throws IOException {
        if( !file.exists() || file.isHidden() || !file.isFile() ) {
            return new ArrayList<File>();
        }
        
        FontFormat format = FontFormat.forFile( file );
        if( format.awtSupported() ) {
            return Arrays.asList( file );
        }
        
        List<File> files = null;
        switch( format ) {
        case TTC:
            files = execTtc2Ttf( file, garbage );
            break;
        case WOFF:
            files = unpackWoff( file, garbage );
            break;
        default:
            files = execFondu( file, garbage );
            break;
        }

        Iterator<File> iter = files.iterator();
        while( iter.hasNext() ) {
            File f = iter.next();
            format = FontFormat.forFile( f );

            if( format == FontFormat.NONE ) {
                iter.remove();
            }
        }

        return files;
    }


    private static List<File> execFondu( File file, FileGarbage kill ) throws IOException {
        File dir = File.createTempFile( "fontdir", ".tmp" );
        dir.delete();
        dir.mkdirs();

        kill.addFile( dir, true );

        File tempFile = new File( dir, file.getName() + ".tmp" );
        kill.addFile( tempFile, true );
        NativeFiles.copy( file, tempFile );
        File execFile = new File( "bin/fondu" );
        String[] cmd = new String[]{ execFile.getAbsolutePath(), "-force", tempFile.getName() };
        
        try {
            Process p = Runtime.getRuntime().exec( cmd, null, dir );
            int err = p.waitFor();

            if( err != 0 ) {
                throw new IOException( "Fondu failed: " + err );
            }

        } catch( InterruptedException ex ) {
            throw new InterruptedIOException( ex.getMessage() );
        }

        List<File> ret = new ArrayList<File>();
        File[] files = dir.listFiles();

        if( files == null )
            return ret;

        for( File f : files ) {
            if( f.getName().equals( tempFile.getName() ) ) {
                continue;
            }

            ret.add( f );
            kill.addFile( f, true );
        }

        return ret;
    }


    private static List<File> execTtc2Ttf( File file, FileGarbage kill ) throws IOException {
        File dir = File.createTempFile( "fontdir", ".tmp" );
        dir.delete();
        dir.mkdirs();
        kill.addFile( dir, true );

        File tempFile = new File( dir, file.getName() + ".tmp" );
        kill.addFile( tempFile, true );
        NativeFiles.copy( file, tempFile );
        File execFile = new File( "bin/ttc2ttf" );

        String[] cmd = new String[]{ execFile.getAbsolutePath(), "-0", tempFile.getName() };

        try {
            Process proc = Runtime.getRuntime().exec( cmd, null, dir );
            int err = proc.waitFor();
            if( err != 0 )
                throw new IOException( "Failed to convert TTC to TTF: " + err );

        } catch( InterruptedException ex ) {
            throw new InterruptedIOException( ex.getMessage() );
        }

        List<File> ret = new ArrayList<File>();
        File[] files = dir.listFiles();

        if( files == null )
            return ret;

        for( File f : files ) {
            if( f.getName().equals( tempFile.getName() ) )
                continue;

            ret.add( f );
            kill.addFile( f, true );
        }

        return ret;
    }


    private static List<File> unpackWoff( File file, FileGarbage kill ) throws IOException {
        long fileLen = file.length();
        if( fileLen < 44 ) {
            throw new IOException( "Not a WOFF file or corrupted." );
        }
        
        File dir = File.createTempFile( "fontdir", ".tmp" );
        dir.delete();
        dir.mkdirs();
        kill.addFile( dir, true );
        
        File outFile = new File( dir, Files.baseName( file ) + ".otf" );
        kill.addFile( outFile, true );
        WoffToOtf.convert( file, outFile );
        
        List<File> ret = new ArrayList<File>( 1 );
        ret.add( outFile );
        return ret;
    }

    /**
     * @deprecated Use fondu instead.
     */
    private static void convertLfwnToPfb( File inFile, File outFile ) throws IOException {
        String[] s = new String[]{ "/usr/texbin/t1unmac",
                                   "-r",
                                   "-b",
                                   inFile.getAbsolutePath(),
                                   outFile.getAbsolutePath()
        };

        try {
            Process p = Runtime.getRuntime().exec( s );
            int err = p.waitFor();
            p.destroy();

            if( err != 0 )
                throw new IOException( "Conversion failed." );

        } catch( InterruptedException ex ) {
            throw new InterruptedIOException( ex.getMessage() );
        }
    }

    /**
     * @deprecated Fondu manages resource forks.
     */
    private static int copyResourceFork( File in, File out ) throws IOException {
        try {
            File inter = new File( out.getAbsolutePath() + ".intermed" );
            inter.deleteOnExit();

            String[] s = new String[]{ "cp", in.getAbsolutePath(), inter.getAbsolutePath() };
            Process p = Runtime.getRuntime().exec( s );
            int err = p.waitFor();
            p.destroy();

            if( err != 0 ) {
                inter.delete();
                return err;
            }
            
            //s = new String[]{ "cp", inter.getAbsolutePath() + "/rsrc", out.getAbsolutePath() };
            s = new String[]{ "cp", inter.getAbsolutePath() + "/..namedfork/rsrc", out.getAbsolutePath() };
            p = Runtime.getRuntime().exec( s );
            err = p.waitFor();
            p.destroy();

            return err;

        } catch( Exception ex ) {
            ex.printStackTrace();
        }

        return -1;
    }


    private FontUnpacker() {}

}
