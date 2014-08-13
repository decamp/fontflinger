/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font;

import java.io.*;

import bits.font.tex.*;
import bits.font.util.FileGarbage;


/**
 * @author Philip DeCamp
 */
public class LatexTest {

    public static void main( String[] args ) {
        try {
            //testTtfBuilder();
            //testPfBuilder();
            //testTypefaceBuilder();
            testTypefaceBuilder2();
        } catch( Exception ex ) {
            ex.printStackTrace();
        }

        System.exit( 0 );
    }


    static void testTtfBuilder() throws Exception {
        File file = new File( "resources_test/test_set/Perpetua.ttf" );
        System.out.println( "Exists: " + file.getPath() + ":  " + file.exists() );

        OtfTranslator metrics = new OtfTranslator( file );
        FileGarbage kill = new FileGarbage();
        metrics.buildFont( "whatever", kill );

        kill.empty();
    }


    static void testPfBuilder() throws Exception {
        File file = new File( "resources_test/test_set/GillSans.pfb" );
        System.out.println( "Exists: " + file.getPath() + ":  " + file.exists() );

        TexFontBuilder fb = TexFontBuilder.getInstance( file );
        FileGarbage kill = new FileGarbage();
        for( File f : fb.buildFont( "whatever", kill ) ) {
            System.out.println( f.getPath() );
        }
        kill.empty();
    }


    static void testTypefaceBuilder() throws Exception {
        TexTypefaceBuilder b = new TexTypefaceBuilder( "Perpetua" );

        File file = new File( "resources_test/test_set/Perpetua.ttf" );
        b.setFont( TexSeries.MEDIUM.code(), TexShapes.NORMAL.code(), file );

        file = new File( "resources_test/test_set/Perpetua Bold.ttf" );
        b.setFont( TexSeries.BOLD_EXTENDED.code(), TexShapes.NORMAL.code(), file );

        b.makeThisTheDefaultTypefaceFor( TexFamily.ROMAN );
        b.buildExampleTexProject( new File( "/tmp/testdoc" ), null );
    }


    static void testTypefaceBuilder2() throws Exception {
        TexTypefaceBuilder b = new TexTypefaceBuilder( "GillSans" );

        File file = new File( "resources_test/test_set/GillSans.pfb" );
        TexFontBuilder fb = TexFontBuilder.getInstance( file );
        fb.setSmallcaps( false );
        fb.setExtend( 2.0 );
        // fb.setSlant(0.25);

        b.setFont( TexSeries.MEDIUM.code(), TexShapes.NORMAL.code(), fb );

        file = new File( "resources_test/test_set/GillSans-Bold.pfb" );
        fb = TexFontBuilder.getInstance( file );
        b.setFont( TexSeries.BOLD_EXTENDED.code(), TexShapes.NORMAL.code(), fb );

        file = new File( "resources_test/test_set/GillSans-Italic.pfb" );
        fb = TexFontBuilder.getInstance( file );
        b.setFont( TexSeries.MEDIUM.code(), TexShapes.ITALIC.code(), fb );

        b.makeThisTheDefaultTypefaceFor( TexFamily.ROMAN, TexFamily.SAN_SERIF );
        b.buildExampleTexProject( new File( "/tmp/testdoc2" ), null );
    }

}
