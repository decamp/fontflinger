/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.gui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.*;
import java.util.*;

import bits.font.*;
import bits.font.tex.*;
import bits.font.util.FileGarbage;
import bits.util.OutputFileNamer;


/**
 * Manages some collection of fonts, including loading fonts from files.
 * 
 * 
 * @author Philip DeCamp
 */
public class FontController extends Observable {


    private final Set<FontHandle> mFonts = new LinkedHashSet<FontHandle>();



    public boolean addFontFile( File file ) throws IOException {
        return doAddFontFile( file, 0 );
    }


    public boolean removeFont( FontHandle font ) {
        synchronized( this ) {
            if( !mFonts.remove( font ) ) {
                return false;
            }
            font.deref();
            setChanged();
        }

        notifyObservers();
        return true;
    }


    public boolean addFont( FontHandle font ) {
        synchronized( this ) {
            if( !mFonts.add( font ) )
                return false;

            font.ref();
            setChanged();
        }

        notifyObservers();
        return true;
    }


    public List<FontHandle> getFonts() {
        synchronized( this ) {
            return new ArrayList<FontHandle>( mFonts );
        }
    }
    

    /**
     * For debugging.
     */
    void dumpAll() {
        if( mFonts.isEmpty() ) {
            return;
        }

        TexTypefaceBuilder b = new TexTypefaceBuilder( "Happenstance" );
        FontHandle fh = mFonts.iterator().next();

        try {
            b.setFont( TexSeries.MEDIUM.code(), TexShapes.NORMAL.code(), fh.file() );
            File dir = new OutputFileNamer( new File( "/tmp" ), "test", "" ).next();

            b.makeThisTheDefaultTypefaceFor( TexFamily.ROMAN );
            b.buildExampleTexProject( dir, dir.getName() );
            System.out.println( "TEX IN: " + dir.getPath() );
            
        } catch( Exception ex ) {
            ex.printStackTrace();
        }

    }
    

    private boolean doAddFontFile( File file, int depth ) throws IOException {
        if( file == null )
            return false;

        // If directory, find all files. Will only recurse once.
        if( file.isDirectory() ) {
            if( depth > 0 )
                return false;

            File[] subFiles = file.listFiles();
            if( subFiles == null )
                return false;

            boolean ret = false;

            for( File f : subFiles ) {
                ret |= doAddFontFile( f, 1 );
            }

            return ret;
        }

        // Check if this source file has already been processed.
        for( FontHandle font : mFonts ) {
            File f = font.sourceFile();
            if( f != null && f.equals( file ) ) {
                return false;
            }
        }


        FileGarbage garbage = new FileGarbage();
        List<File> list = FontUnpacker.unpack( file, garbage );
        boolean ret = false;

        synchronized( this ) {
            for( File f : list ) {
                FontFormat format = FontFormat.forFile( f );
                Font javaFont = null;

                if( format == FontFormat.NONE )
                    continue;

                if( format.awtSupported() ) {
                    try {
                        javaFont = Font.createFont( format.awtType(), f );
                    } catch( FontFormatException ex ) {
                        ex.printStackTrace();
                    }
                }

                FontHandle font = new FontHandle( f, format, file, javaFont, garbage );
                mFonts.add( new FontHandle( f, format, file, javaFont, garbage ) );
                setChanged();
            }
        }

        garbage.deref();
        notifyObservers();
        return ret;
    }

}
