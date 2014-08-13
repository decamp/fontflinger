/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.tex;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import bits.font.util.PfToAfm;
import bits.font.util.*;


/**
 * @author Philip DeCamp
 */
public class PfTranslator extends TexFontBuilder {

    private final File mSourceFile;

    private double     mExtend    = 1.0;
    private double     mSlant     = 0.0;
    private boolean    mSmallcaps = false;


    public PfTranslator( File file ) {
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


    public List<File> buildFont( String internalName, FileGarbage garbage ) throws IOException {
        File tempDir = File.createTempFile( "fonts", ".tmp" );
        tempDir.delete();
        tempDir.mkdirs();
        garbage.addFile( tempDir, true );

        File binDir    = new File( "bin" );
        File encFile   = new File( "resources/T1-WGL4.enc" );
        File inputFile = new File( tempDir, mSourceFile.getName().replace( ' ', '_' ) );
        File vplFile   = new File( tempDir, internalName + ".vpl" );
        File mapFile   = new File( tempDir, internalName + ".map" );
        File outFile   = new File( tempDir, internalName + ".tfm" );
        
        // Copy input files.
        NativeFiles.copy( mSourceFile, inputFile );
        File tempFile = encFile;
        encFile       = new File( tempDir, encFile.getName() );
        NativeFiles.copy( tempFile, encFile );
        
        // Generate AFM file.
        File afmFile = PfToAfm.pfToAfm( inputFile );

        // Convert AFM file to TFM file.
        File binFile     = new File( binDir, "afm2tfm" );
        List<String> cmd = new ArrayList<String>();
        cmd.add( binFile.getAbsolutePath() );
        cmd.add( afmFile.getName() );
        cmd.add( "-T" );
        cmd.add( encFile.getName() );
        
        if( mExtend != 1.0 ) {
            cmd.add( "-e" );
            cmd.add( String.valueOf( mExtend ) );
        }
        
        if( mSlant != 0.0 ) {
            cmd.add( "-s" );
            cmd.add( String.valueOf( mSlant ) );
        }
        
        if( mSmallcaps ) {
            cmd.add( "-V" );
        } else {
            cmd.add( "-v" );
        }
        
        cmd.add( vplFile.getAbsolutePath() );
        cmd.add( outFile.getAbsolutePath() );
        String mapString;
        
        try {
            mapString = TranslatorUtil.exec( cmd, binDir, tempDir, true, true );
        }catch( InterruptedException ex ) {
            InterruptedIOException e = new InterruptedIOException();
            e.initCause( ex );
            throw e;
        }
        
        //Create MAP file.
        {
            //System.out.println(mapString);
            Pattern pat = Pattern.compile("^(\\S++)\\s++(\\S++)\\s*+(\\\".*?\\\")\\s*+\\<(\\S++)");
            Matcher mat = pat.matcher(mapString);
            
            if(!mat.find())
                throw new IOException("Could not read output of afm2tfm command.");
            
            PrintWriter out = new PrintWriter( mapFile );
            
            
            out.format( "%s %s %s <%s <%s\n", 
                        internalName, 
                        mat.group(2), 
                        mat.group(3),
                        mat.group(4),
                        inputFile.getName());
            
            out.close();
            //FileUtil.copy(mapFile, new File("/tmp/map.txt")); 
        }
        
        // afmFile.delete();

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
