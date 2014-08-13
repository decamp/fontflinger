/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.tex;

/**
 * @author Philip DeCamp
 */
public enum TexFamily {
    
    ROMAN           ( "rmdefault" ),
    SAN_SERIF       ( "sfdefault" ),
    TYPE_WRITER     ( "ttdefault" );
    
    
    private final String mVariable;
    
    
    TexFamily( String variable ) {
        mVariable = variable;
    }
    
    
    public String defaultVariable() {
        return mVariable;
    }

}
