/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.tex;

import java.awt.Font;
import java.io.*;
import java.util.*;

import bits.font.FontFormat;
import bits.font.util.*;
import bits.util.Files;


/**
 * @author Philip DeCamp
 */
public class TtfTranslator extends TexFontBuilder {

    private final File mSourceFile;
    private String mFontName;
    
    private double     mExtend    = 1.0;
    private double     mSlant     = 0.0;
    private boolean    mSmallcaps = false;
    private boolean    mOldStyle  = false;


    public TtfTranslator( File file ) {
        mSourceFile = file;
        mFontName   = Files.baseName( file );
        
        try {
            FontFormat format = FontFormat.forFile( file );
            if( format.awtSupported() ) {
                Font font = Font.createFont( format.awtType(), file );
                mFontName = font.getName();
            }
        } catch( Exception ignore ) {}
    }



    @Override
    public void setExtend( double factor ) {
        mExtend = factor;
    }


    @Override
    public void setSlant( double factor ) {
        mSlant = factor;
    }


    @Override
    public void setSmallcaps( boolean enable ) {
        mSmallcaps = enable;
    }


    @Override
    public void setOldStyleNumerals( boolean enable ) {
        mOldStyle = enable;
    }



    public List<File> buildFont( String internalName, FileGarbage garbage ) throws IOException {
        File tempDir = File.createTempFile( "fonts", ".tmp" );
        tempDir.delete();
        tempDir.mkdirs();
        garbage.addFile( tempDir, true );

        File binDir    = new File( "bin" );
        File binFile   = new File( binDir, "ttf2tfm" );
        File encFile   = new File( "resources/T1-WGL4.enc" );
        //File glyphFile = new File( "resources/glyphlist.rpl" );
        File inputFile = new File( tempDir, mSourceFile.getName().replace( ' ', '_' ) );
        File vplFile   = new File( tempDir, internalName + ".vpl" );
        File mapFile   = new File( tempDir, internalName + ".map" );
        File outFile   = new File( tempDir, internalName + ".tfm" );
        
        NativeFiles.copy( mSourceFile, inputFile );
        File tempFile = encFile;
        encFile       = new File( tempDir, encFile.getName() );
        NativeFiles.copy( tempFile, encFile );
        
        List<String> cmd = new ArrayList<String>();
        cmd.add( binFile.getAbsolutePath() );
        cmd.add( inputFile.getName() );
        cmd.add( "-p"); 
        cmd.add( encFile.getName() );
//        cmd.add( "-R" );
//        cmd.add( glyphFile.getAbsolutePath() );
        
        if( mExtend != 1.0 ) {
            cmd.add( "-e" );
            cmd.add( Double.toString( mExtend ) );
        }
        
        if( mSlant != 0.0 ) {
            cmd.add( "-s" );
            cmd.add( Double.toString( mSlant ) );
        }
        
        if( mSmallcaps ) {
            cmd.add( "-V" );
        }else{
            cmd.add( "-v" );
        }
        
        cmd.add( vplFile.getName() );
        cmd.add( outFile.getName() );
        
        String mapString;
        
        try {
            mapString = TranslatorUtil.exec( cmd, binDir, tempDir, true, true );
        } catch( InterruptedException ex ) {
            InterruptedIOException e = new InterruptedIOException();
            e.initCause( ex );
            throw e;
        }
        
        // Create MAP file.
        {
            PrintWriter out = new PrintWriter( mapFile );
            out.format( "%s %s %s <%s <%s\n",
                        internalName,
                        mFontName,
                        "\" T1Encoding ReEncodeFont \"",
                        encFile.getName(), 
                        inputFile.getName() );
            out.close();
        }
        
        
        File[] files   = tempDir.listFiles();
        List<File> ret = new ArrayList<File>();

        assert files != null;
        for( File f : files ) {
            if( f.isFile() && !f.isHidden() ) {
                ret.add( f );
            }
        }

        return ret;
    }

}
