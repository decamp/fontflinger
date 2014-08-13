package bits.font.util;

import java.io.*;
import java.util.*;
import java.awt.*;

import bits.font.*;
import bits.util.Files;


/**
 * Parses, translates, and copies font files from some source location to a
 * destination.
 *
 * @author decamp
 */
public class FontOrganizer {

    private static String INVALID_CHAR_PAT = "[^a-zA-Z0-9#, \\(\\)]";
    private static String SPACE_PAT        = "\\s++";


    /**
     * Recursively searches for all fonts in <code>inDir</code>, translates
     * whichever fonts it finds into a Java friendly format, then copies them
     * into an equivalent location in <code>outDir</code>.
     *
     * @param inDir  Directory tree containing source fonts.
     * @param outDir Directory tree where processed fonts will be placed.
     * @param tester Optional FontTester object that can provided to observe the
     *               font conversion process. May be <code>null</code>.
     */
    public static void organizeFonts( File inDir, File outDir, FontTester tester ) {
        Stack<File> stack = new Stack<File>();
        stack.push( inDir );

        while( !stack.isEmpty() ) {
            File dir = stack.pop();
            File[] files = dir.listFiles();
            // System.out.println("#############" + dir.getPath() +
            // "###########");

            for( File f : files ) {
                if( f.isHidden() ) {
                    continue;
                }

                if( f.isDirectory() ) {
                    stack.push( f );
                    continue;
                }

                transferFont( inDir, f, outDir, tester );
            }
        }
    }

    /**
     * Processes a single font file.
     */
    public static boolean transferFont( File inDir, File inFile, File outDir, FontTester tester ) {
        if( inDir == null ) {
            inDir = inFile.getParentFile();
        }

        String relPath = getRelativePath( inDir, inFile.getParentFile(), true );
        outDir = new File( outDir, relPath );

        FileGarbage kill = new FileGarbage();
        kill.addFile( outDir, false );

        try {
            for( File file : FontUnpacker.unpack( inFile, kill ) ) {
                FontFormat format = FontFormat.forFile( file );
                if( format == FontFormat.NONE ) {
                    continue;
                }

                File newFile = new File( outDir, file.getName() );
                if( newFile.exists() ) {
                    continue;
                }

                try {
                    System.out.println( file.getPath() );
                    Font font = Font.createFont( format.awtType(), file );

                    if( tester != null ) {
                        tester.testFont( font );
                    }

                    if( !outDir.exists() ) {
                        outDir.mkdirs();
                    }

                    NativeFiles.copy( file, newFile );
                    kill.remove( outDir );
                } catch( Exception ex ) {
                }
            }

        } catch( Exception exc ) {
            kill.empty();
        }

        return true;
    }



    private static String getRelativePath( File root, File file, boolean clean ) {
        Stack<String> stack = new Stack<String>();

        while( file != null && !file.equals( root ) ) {
            stack.push( file.getName() );
            file = file.getParentFile();
        }


        StringBuilder s = new StringBuilder();
        while( !stack.isEmpty() ) {
            if( clean ) {
                s.append( cleanPath( stack.pop() ) );
            } else {
                s.append( stack.pop() );
            }

            if( !stack.isEmpty() ) {
                s.append( File.separator );
            }
        }

        return s.toString();
    }


    private static String cleanPath( String s ) {
        s = s.replaceAll( INVALID_CHAR_PAT, " " );
        s = s.replaceAll( SPACE_PAT, " " ).trim();
        return s;
    }

}
