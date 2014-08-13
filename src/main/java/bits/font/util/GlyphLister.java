/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.util;

import java.awt.Font;

/**
 * @author Philip DeCamp
 */
class GlyphLister {
    
    public static String supportedGlyphs( Font font, int cols ) {
        StringBuilder s = new StringBuilder();
        int count = 0;
        
        for( int i = 0; i < 0xFFFF; i++ ) {
            if( i == '\n' || i == '\r' ) {
                continue;
            }
            
            if( font.canDisplay( i ) ) {
                s.append( (char)i );
                
                if( cols > 0 && ++count >= cols ) {
                    s.append( '\n' );
                    count = 0;
                }
            }
        }
        
        return s.toString();
    }
    
}
