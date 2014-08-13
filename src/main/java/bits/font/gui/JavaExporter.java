/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.gui;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.*;
import java.io.*;
import java.util.List;

import javax.swing.*;

import bits.font.util.NativeFiles;
import bits.layout.LayoutAdapter;
import bits.util.Files;


/**
 * @author Philip DeCamp
 */
class JavaExporter extends JPanel {

    private static final String EX_FILE = "FontExample.java";
    
    
    private final List<FontHandle> mFonts;
    
    private final JButton mNoExampleButton;
    private final JButton mExampleButton;
    private final JButton mCancelButton;
    
    private final JFileChooser mChooser;
    
    private final JDialog mDialog;
    
    
    
    JavaExporter( Component parent, List<FontHandle> fonts, JFileChooser chooser ) {
        if( chooser != null ) {
            mChooser = chooser;
        } else {
            String path = System.getProperty( "user.home" );
            if( path != null ) {
                File file = new File( path, "Desktop" );
                if( !file.exists() ) {
                    file = new File( path );
                }

                mChooser = new JFileChooser( file );
            } else {
                mChooser = new JFileChooser();
            }
        }
        
        while( parent.getParent() != null ) {
            parent = parent.getParent();
        }
        
        mFonts = fonts;
        
        mNoExampleButton = new JButton( "Export Fonts" );
        mExampleButton   = new JButton( "Export Fonts + Example Code" );
        mCancelButton    = new JButton( "Cancel" );
        
        mNoExampleButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doExport( false );
            }
        } );
        
        mExampleButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doExport( true );
            }
        } );
        
        mCancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doCancel();
            }
        } );
        
        Font font = new Font( "Verdana", Font.BOLD, 18 );
        
        for( JButton b: new JButton[] { mNoExampleButton, mExampleButton, mCancelButton } ) {
            b.setFont( font );
            add( b );
        }
        
        setLayout( new Layout() );
        
        
        mDialog = new JDialog( (Window)parent, "Export Options", ModalityType.APPLICATION_MODAL );
        mDialog.setContentPane( this );
        mDialog.setSize( 450, 280 );
        mDialog.setResizable( false );
        mDialog.setLocationRelativeTo( parent );
        mDialog.setVisible( true );
        
    }
    
    
    private void doCancel() {
        mDialog.setVisible( false );
    }
    
    
    private void doExport( boolean genExample ) {
        
        File outDir = null;
        
        while( true ) {
            if( mChooser.showSaveDialog( this ) != JFileChooser.APPROVE_OPTION ) {
                return;
            }
            
            outDir = mChooser.getSelectedFile();
            if( outDir == null ) {
                return;
            }
            
            if( outDir.exists() ) {
                JOptionPane.showMessageDialog( this, "Directory already exists.", "Error", JOptionPane.WARNING_MESSAGE );
                continue;
            }
            
            break;
        }
        
        try {
            export( outDir, genExample );
        } catch( Exception ex ) {
            JOptionPane.showMessageDialog( this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE );
            return;
        }
        
        mDialog.setVisible( false );
    }
    
    
    
    private void export( File outDir, boolean genExample ) throws IOException {
        if( !outDir.mkdirs() ) {
            throw new IOException( "Could not create output directory." );
        }
        for( FontHandle f: mFonts ) {
            NativeFiles.copy( f.file(), outDir );
        }
        if( !genExample ) {
            return;
        }
        
        genExample( outDir );        
    }
    
    
    private void genExample( File outDir ) throws IOException {
        LineWriter out = new LineWriter( new File( outDir, EX_FILE ) );
        out.println( "import java.io.*;" );
        out.println( "import java.awt.*; " );
        out.println( "import javax.swing.*;" );
        out.println();
        out.println();
        out.println( "public class FontExample {");
        out.right();
        out.println();
        out.println( "public static void main( String[] args ) throws Exception {" );
        out.right();
        out.println();
        
        out.println( "File file;" );
        out.println( "Font font;" );
        out.println( "JLabel label;" );
        out.println();
        out.println( "int width = 550;" );
        out.println( "int y     = 0;" );
        out.println( "int h     = 36;" );
        out.println();
        out.println( "JPanel panel = new JPanel();" );
        out.println( "panel.setLayout( null );" );
        out.println( "panel.setBackground( Color.WHITE );" );
        out.println();
        
        for( FontHandle f: mFonts ) {
            out.println( "file =  new File( \"" + f.file().getName() + "\" );" );
            
            StringBuilder sb = new StringBuilder( "font =  Font.createFont( " );
            
            if( f.format().awtType() == Font.TYPE1_FONT ) {
                sb.append( "Font.TYPE1_FONT, " );
            }else{
                sb.append( "Font.TRUETYPE_FONT, " );
            }
            
            sb.append( "file );" );
            out.println( sb.toString() );
            
            out.println( "font  = font.deriveFont( 28.0f );" );
            out.println( "label = new JLabel( \"Font: " + f.font().getName() + "\", JLabel.LEFT );" );
            out.println( "label.setFont( font );" );
            out.println( "label.setBounds( 3, y, width, h );" );
            out.println( "panel.add( label ); ");
            out.println( "y += h + 3;" );
            out.println();
        }
        
        out.println( "panel.setPreferredSize( new Dimension( width + 6, y ) );" );
        
        out.println( "JFrame frame = new JFrame( \"Font Example\" );" );
        out.println( "frame.add( new JScrollPane( panel ) );" );
        out.println( "frame.setSize( width + 10, 600 );" );
        out.println( "frame.setLocationRelativeTo( null ); " );
        out.println( "frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );" );
        out.println( "frame.setVisible( true );" );
        out.println();
        
        out.left();
        out.println("}");
        out.left();
        out.println("}");
        out.close();
    }
    


    private final class Layout extends LayoutAdapter {
        
        @Override
        public void layoutContainer( Container cont ) {
            final JButton[] buttons = { mNoExampleButton, mExampleButton, mCancelButton };
            
            final int MARGIN = 6;
            final int w      = getWidth();
            final int h      = getHeight() - MARGIN * ( buttons.length + 1 ); 
            
            for( int i = 0; i < buttons.length; i++ ) {
                int x0 = MARGIN;
                int y0 = ( i + 1 ) * MARGIN + h * i / buttons.length;
                int x1 = Math.max( x0 + 1, w - MARGIN );
                int y1 = ( i + 1 ) * MARGIN + h * ( i + 1 ) / buttons.length;
                
                buttons[i].setBounds( x0, y0, x1 - x0, y1 - y0 );
            }
        }
    
    }
    
    
    private static final class LineWriter {
        
        private final PrintWriter mOut;
        private int mIndent   = 0;
        private String mChars = "";
        
        public LineWriter( File file ) throws IOException {
            mOut = new PrintWriter( file );
        }

        
        void println() {
            mOut.println();
        }
        
        void println( String txt ) {
            mOut.print( mChars );
            mOut.println( txt );
        }
        
        void right() {
            mIndent += 4;
            mChars += "    ";
        }
        
        void left() {
            mIndent -= 4;
            mChars = mChars.substring( 0, mChars.length() - 4 );
        }
        
        void close() {
            mOut.close();
        }
        
    }
    
}
