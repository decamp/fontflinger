/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.gui;

import java.io.File;
import javax.swing.*;

import bits.util.platform.AppleAppHandler;


/**
 * @author Philip DeCamp
 */
public class AppMain {


    public static void main( String[] args ) throws Exception {
        AppMain app = new AppMain();
    }


    private AppMain() throws ClassNotFoundException {
        AppleAppHandler app = new AppHandler();
        AppleAppHandler.setQuitHandler( app );
        
        JFrame frame = new JFrame( "Font Flinger" );
        frame.setContentPane( new FontFlinger() );
        frame.setSize( 700, 1224 );
        frame.setLocationRelativeTo( null );
        
        frame.setVisible( true );
    }


    private final class AppHandler extends AppleAppHandler {

        public boolean handleOpenFile( File file ) {
            // System.out.println("^^^ OpenFile : " + file.getPath());
            return true;
        }

        public boolean handleQuit() {
            // System.out.println("^^^ Quit");
            System.exit( 0 );
            return true;
        }

        public boolean handleOpenApplication() {
            // System.out.println("^^^ OpenApplication");
            return true;
        }

        public boolean handlePreferences() {
            // System.out.println("^^^ Preferences");
            return true;
        }

        public boolean handleAbout() {
            // System.out.println("^^^ About");
            return true;
        }

        public boolean handlePrintFile() {
            // System.out.println("^^^ PrintFile");
            return true;
        }

        public boolean handleReOpenApplication() {
            // System.out.println("^^^ ReOpenApplication");
            return true;
        }

    }

}
