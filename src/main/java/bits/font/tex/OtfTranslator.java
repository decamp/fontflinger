/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.tex;

import java.io.*;
import java.util.*;

import bits.font.FontFormat;
import bits.font.util.*;
import bits.util.Files;


/**
 * @author Philip DeCamp
 */
public class OtfTranslator extends TexFontBuilder {

    private final File mSourceFile;

    private double     mExtend    = 1.0;
    private double     mSlant     = 0.0;
    private boolean    mSmallcaps = false;
    private boolean    mOldStyle  = false;


    public OtfTranslator( File file ) {
        mSourceFile = file;
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
        File binFile   = new File( binDir, "otftotfm" );
        File encFile   = new File( "resources/T1-WGL4.enc" );
        File glyphFile = new File( "resources/glyphlist.txt" );
        File inputFile = new File( tempDir, mSourceFile.getName().replace( ' ', '_' ) );
        File mapFile   = new File( tempDir, internalName + ".map" );
        
        NativeFiles.copy( mSourceFile, inputFile );
        
        List<String> cmd = new ArrayList<String>();
        cmd.add( binFile.getAbsolutePath() );
        cmd.add( "--no-updmap" );
        cmd.add( "--encoding=" + encFile.getAbsolutePath() );
        cmd.add( "--glyphlist=" + glyphFile.getAbsolutePath() );
        cmd.add( "-fkern" );
        cmd.add( "-fliga" );
        cmd.add( "--map-file=" + mapFile.getAbsolutePath() );

        if( mExtend != 1.0 ) {
            cmd.add( "--extend=" + mExtend );
        }

        if( mSlant != 0.0 ) {
            cmd.add( "--slant=" + mSlant );
        }

        if( mSmallcaps ) {
            cmd.add( "-fsmcp" );
        }

        if( mOldStyle ) {
            cmd.add( "-fonum" );
        }

        cmd.add( inputFile.getName() );
        cmd.add( internalName );

        try {
            TranslatorUtil.exec( cmd, binDir, tempDir, false, false );
        }catch( InterruptedException ex ) {
            InterruptedIOException e = new InterruptedIOException();
            e.initCause( ex );
            throw e;
        }catch( IOException ex ) {
            FontFormat format = FontFormat.forFile( mSourceFile );
            if( format == FontFormat.TTF ) {
                return tryTtfTranslator( internalName, garbage );
            }
        }
        
        File[] files   = tempDir.listFiles();
        List<File> ret = new ArrayList<File>();

        for( File f : files ) {
            if( f.isFile() && !f.isHidden() ) {
                FontFormat format = FontFormat.forFile( f );
                if( format == FontFormat.OTF ) {
                    continue;
                }
                
                ret.add( f );
            }
        }

        return ret;
    }

    
    private List<File> tryTtfTranslator( String internalName, FileGarbage garbage ) throws IOException {
        System.err.println( "Attempting ttf2tfm translator instead of otftotfm..." ); 
        TtfTranslator trans = new TtfTranslator( mSourceFile );
        trans.setExtend( mExtend );
        trans.setSlant( mSlant );
        trans.setSmallcaps( mSmallcaps );
        trans.setOldStyleNumerals( mOldStyle );
        
        return trans.buildFont( internalName, garbage );
    }
    

}
