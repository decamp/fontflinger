/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.gui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionListener;

import bits.font.FontTester;
import bits.layout.FillLayout;


/**
 * @author Philip DeCamp
 */
public class FontListView extends JPanel {

    private static final Dimension DIM = new Dimension( 10, 10 );
    private static final int ROW_HEIGHT = 32;

    private final FontController mController;
    private final JScrollPane mListPane;
    private final JList mList;
    private final DropTarget mFileTarget;

    private boolean mDropActive = false;


    public FontListView( FontController controller, FontTester preview ) {
        mController = controller;
        mController.addObserver( new FontObserver() );

        mList = new JList();
        mListPane = new JScrollPane( mList );
        mList.setCellRenderer( new FontRenderer() );
        mFileTarget = new DropTarget( this, new DropHandler() );

        mList.setBackground( Color.WHITE );
        mList.setForeground( Color.BLACK );
        mList.setFixedCellHeight( ROW_HEIGHT );

        mListPane.setBorder( null );

        setLayout( new FillLayout( 1, 1, 1, 1 ) );
        add( mListPane );

        mList.addKeyListener( new KeyHandler() );
    }



    public void paint( Graphics g ) {
        super.paint( g );

        if( mDropActive ) {
            final int x = mListPane.getX();
            final int y = mListPane.getY();
            final int w = mListPane.getWidth();
            final int h = mListPane.getHeight();
            final int t = 4;

            g.setColor( Palette.DROP_COLOR );
            g.fillRect( x, y, w, t );
            g.fillRect( x, y + h - t, w, t );

            g.fillRect( x, y, t, h );
            g.fillRect( x + w - t, y, t, h );
        }

    }


    public void addListSelectionListener( ListSelectionListener listener ) {
        mList.addListSelectionListener( listener );
    }


    public void removeListSelectionListener( ListSelectionListener listener ) {
        mList.removeListSelectionListener( listener );
    }


    public FontHandle getLeadSelectionFont() {
        try {
            int idx = mList.getLeadSelectionIndex();
            ListModel model = mList.getModel();

            if( idx >= 0 && idx < model.getSize() ) {
                return (FontHandle)model.getElementAt( idx );
            }

        } catch( Exception ex ) {}

        return null;
    }



    private final class FontRenderer implements ListCellRenderer {

        private final Border mNormalBorder = BorderFactory.createLineBorder( new Color( 0f, 0f, 0f, 0f ), 2 );
        private final Border mFocusBorder = BorderFactory.createLineBorder( Palette.FOCUS_COLOR, 2 );
        private final FontLabel mLabel = new FontLabel( ROW_HEIGHT );


        public Component getListCellRendererComponent( JList list,
                                                       Object value,
                                                       int idx,
                                                       boolean isSelected,
                                                       boolean cellHasFocus )
        {

            if( isSelected ) {
                mLabel.setBackground( Palette.SELECT_COLOR );
                mLabel.setForeground( Color.WHITE );
            } else if( idx % 2 == 0 ) {
                mLabel.setBackground( Palette.ODD_COLOR );
                mLabel.setForeground( Color.BLACK );
            } else {
                mLabel.setBackground( Palette.EVEN_COLOR );
                mLabel.setForeground( Color.BLACK );
            }

            if( cellHasFocus ) {
                mLabel.setBorder( mFocusBorder );
            } else {
                mLabel.setBorder( mNormalBorder );
            }

            mLabel.setFont( (FontHandle)value );
            return mLabel;
        }

    }


    private final class FontObserver implements Observer {

        public void update( Observable source, Object data ) {
            mList.setListData( mController.getFonts().toArray( new FontHandle[0] ) );
            repaint();
        }

    }


    private final class DropHandler implements DropTargetListener {

        public void dragEnter( DropTargetDragEvent e ) {
            mDropActive = true;
            repaint();
        }

        public void dragExit( DropTargetEvent e ) {
            if( !mDropActive )
                return;

            mDropActive = false;
            repaint();
        }

        @SuppressWarnings( "unchecked" )
        public void drop( DropTargetDropEvent e ) {
            e.acceptDrop( DnDConstants.ACTION_LINK );
            mDropActive = false;
            repaint();

            Transferable t = e.getTransferable();
            List<File> files = null;

            try {
                files = (List<File>)t.getTransferData( DataFlavor.javaFileListFlavor );
            } catch( Exception ex ) {}

            StringBuilder errString = new StringBuilder();
            int errCount = 0;

            for( File f : files ) {
                try {
                    mController.addFontFile( f );
                } catch( IOException ex ) {
                    errCount++;
                    errString.append( "Failed: " + f.getPath() );
                    errString.append( "\n" );
                    errString.append( ex.getMessage() );
                    ex.printStackTrace();
                }
            }

            if( errCount > 0 ) {
                JOptionPane.showMessageDialog( FontListView.this,
                                               errCount + " failures: \n" + errString.toString(),
                                               "Errors",
                                               JOptionPane.WARNING_MESSAGE );
            }
        }

        public void dragOver( DropTargetDragEvent e ) {}

        public void dropActionChanged( DropTargetDragEvent e ) {}

    }


    private final class KeyHandler implements KeyListener {

        public void keyPressed( KeyEvent e ) {
            switch( e.getKeyCode() ) {
            case KeyEvent.VK_A:
                // mController.dumpAll();
                break;

            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_BACK_SPACE:
            {
                Object[] selected = mList.getSelectedValues();

                for( Object obj : selected ) {
                    mController.removeFont( (FontHandle)obj );
                }
                break;
            }
            }

        }

        public void keyReleased( KeyEvent e ) {}

        public void keyTyped( KeyEvent e ) {}

    }

}
