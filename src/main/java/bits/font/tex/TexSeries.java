/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.tex;

/**
 * @author Philip DeCamp
 */
public enum TexSeries {
    
    MEDIUM          ( "m" ),
    BOLD            ( "b" ),
    BOLD_EXTENDED   ( "bx" ),
    SEMI_BOLD       ( "sb" ),
    CONDENSED       ( "c" );
    
    
    private final String mCode;
    
    TexSeries( String code ) {
        mCode = code;
    }
    
    
    public String code() {
        return mCode;
    }
    
}
