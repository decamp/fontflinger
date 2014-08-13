/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.tex;

/**
 * @author Philip DeCamp
 */
public enum TexShapes {
    
    NORMAL           ( "n"  ),
    ITALIC           ( "it" ),
    SLANTED          ( "sl" ),
    SMALL_CAPS       ( "sc" );
    
    
    
    private final String mCode;

    TexShapes( String code ) {
        mCode = code;
    }

    
    public String code() {
        return mCode;
    }
    
}
