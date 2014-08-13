/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.font;

import bits.util.Files;

import java.awt.Font;
import java.io.File;


/**
 * @author Philip DeCamp
 */
public enum FontFormat {

    NONE( Integer.MIN_VALUE ),
    PFA(  Font.TYPE1_FONT ),
    PFB(  Font.TYPE1_FONT ),
    TTF(  Font.TRUETYPE_FONT ),
    OTF(  Font.TRUETYPE_FONT ),
    TTC(  Integer.MIN_VALUE ),
    WOFF( Integer.MIN_VALUE );


    private int mAwtType;


    FontFormat( int awtType ) {
        mAwtType = awtType;
    }



    public int awtType() {
        return mAwtType;
    }


    public boolean awtSupported() {
        return awtType() != Integer.MIN_VALUE;
    }


    public static FontFormat forFile( File file ) {
        String suf = Files.suffix( file );

        if( suf == "pfa" ) {
            return FontFormat.PFA;
        }

        if( suf == "pfb" ) {
            return FontFormat.PFB;
        }

        if( suf == "ttf" ) {
            return FontFormat.TTF;
        }

        if( suf == "otf" ) {
            return FontFormat.OTF;
        }

        if( suf == "ttc" ) {
            return FontFormat.TTC;
        }

        if( suf == "woff" ) {
            return FontFormat.WOFF;
        }

        return FontFormat.NONE;
    }

}
