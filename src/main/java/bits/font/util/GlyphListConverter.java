package bits.font.util;

import java.io.*;
import java.util.regex.*;

/**
 * Converts txt glyph lists to rpl. I don't remember or even know where these formats come from.
 *
 * @author decamp
 */
public final class GlyphListConverter {
    
    public static void main( String[] args ) throws Exception {
        File in  = new File( "resources/glyphlist.txt" );
        File out = new File( "resources/glyphlist.rpl" );
        convert( in, out );
    }
    
    public static void convert( File in, File out ) throws IOException {
        Pattern mapPat     = Pattern.compile("^([^#;]++);([0-9a-f]++)", Pattern.CASE_INSENSITIVE );
        Pattern commentPat = Pattern.compile("^\\s*+(#++)(.*+)$");
        
        BufferedReader r = new BufferedReader( new FileReader( in ) );
        PrintWriter w    = new PrintWriter( out );
        
        for( String k = r.readLine(); k != null; k = r.readLine() ) {
            Matcher m = mapPat.matcher( k );
            
            if( m.find() ) {
                w.println( m.group(1) + " " + m.group(2) );
                continue;
            }
            
            m = commentPat.matcher( k );
            
            if( m.find() ) {
                w.println( m.group(1).replace( "#", "%" ) + m.group(2) );
                continue;
            }
        }
        
        r.close();
        w.close();
    }
    
}
