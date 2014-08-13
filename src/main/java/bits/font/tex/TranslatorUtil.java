/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.tex;

import java.io.*;
import java.util.List;


/**
 * @author Philip DeCamp
 */
class TranslatorUtil {

    static String exec( List<String> cmd, 
                        File binDir, 
                        File workDir, 
                        boolean parseOutput,
                        boolean tossErrorOutput )
                        throws IOException, InterruptedException 
    {
        String[] cmdp = cmd.toArray( new String[cmd.size()] );
        String[] envp = { "PATH=" + binDir.getAbsolutePath() };
        Process proc  = Runtime.getRuntime().exec( cmdp, envp, workDir ); 
        
        if( tossErrorOutput ) {
            pullStream( proc.getErrorStream() );
        }
        
        try {
            int err    = proc.waitFor();
            String msg = null;
            
            if( err != 0 ) {
                StringBuilder sb = new StringBuilder();
                for( String c : cmd ) {
                    if( sb.length() + c.length() > 50 ) {
                        sb.append( "..." );
                        break;
                    }
                    
                    sb.append( c );
                    sb.append( " " );
                }
                
                msg = "Conversion failed: " + sb.toString();
                System.err.println( msg );
                
                BufferedReader in = new BufferedReader( new InputStreamReader( proc.getErrorStream() ) );
                for( String k = in.readLine(); k != null; k = in.readLine() ) {
                    System.err.println( k );
                }
                
                in.close();
            }
        } catch( InterruptedException ex ) {
            throw new InterruptedIOException( ex.getMessage() );
        }
        
        StringBuilder s = new StringBuilder();

        if( parseOutput ) {
            BufferedReader in = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            for( String k = in.readLine(); k != null; k = in.readLine() ) {
                s.append( k );
                s.append( "\n" );
            }

            in.close();
        }

        return s.toString();
    }

    
    private static void pullStream( final InputStream s ) {
        Thread t = new Thread( "Reader thread" ) {
            byte[] buff = new byte[1024*4];
            public void run() {
                try {
                    while( true ) {
                        int n = s.read( buff );
                        if( n <= 0 ) {
                            break;
                        }
                        //System.out.print( new String( buff, 0, n, "UTF-8" ) ); 
                    }
                }catch( Exception ignore ) {}
            }
        };
        
        t.setDaemon( true );
        t.setPriority( Thread.MIN_PRIORITY );
        t.start();
    }

}
