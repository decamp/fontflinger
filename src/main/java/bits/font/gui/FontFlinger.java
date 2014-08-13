/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bits.layout.LayoutAdapter;


/**
 * @author Philip DeCamp
 */
public class FontFlinger extends JPanel {

    private final FontController mController;

    private final JSplitPane     mSplitPane;
    private final FontListView   mListView;
    private final FontPreviewer  mPreviewPane;

    private final JButton        mJavaButton;
    private final JButton        mLatexButton;


    public FontFlinger() {
        mController  = new FontController();
        mPreviewPane = new FontPreviewer();
        mListView    = new FontListView( mController, mPreviewPane );
        mSplitPane   = new JSplitPane( JSplitPane.VERTICAL_SPLIT, mListView, mPreviewPane );
        mJavaButton  = new JButton( "Java Export" );
        mLatexButton = new JButton( "Latexport" );

        mPreviewPane.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
        mListView.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
        mListView.addListSelectionListener( new ListSelectionHandler() );

        setLayout( new Layout() );
        add( mSplitPane );
        add( mJavaButton );
        add( mLatexButton );

        mSplitPane.setDividerLocation( 550 );
        mSplitPane.setBorder( null );

        mJavaButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                javaExport();
            }
        });
        
        mLatexButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                latexExport();
            }
        } );
    }


    
    private void javaExport() {
        List<FontHandle> fonts = mController.getFonts();
        
        if( fonts.isEmpty() ) {
            return;
        }
        
        new JavaExporter( this, fonts, null );
    }
    

    private void latexExport() {
        List<FontHandle> fonts = mController.getFonts();

        if( fonts.isEmpty() )
            return;

        new LatexExporter( this, fonts, null );
    }


    private final class Layout extends LayoutAdapter {
        public void layoutContainer( Container c ) {
            final int MARGIN        = 0;
            final int BUTTON_MARGIN = 4;
            final int BUTTON_WIDTH  = 120;
            final int BUTTON_HEIGHT = 38;
            final int w = getWidth();
            final int h = getHeight();

            int x = MARGIN;
            int y = h - MARGIN - BUTTON_MARGIN * 2 - BUTTON_HEIGHT;

            mSplitPane.setBounds( MARGIN,
                                  MARGIN,
                                  w - MARGIN * 2,
                                  y );

            x = BUTTON_MARGIN;
            y += BUTTON_MARGIN;

            mJavaButton.setBounds( x, y, BUTTON_WIDTH, BUTTON_HEIGHT );
            x += BUTTON_WIDTH + BUTTON_MARGIN;
            mLatexButton.setBounds( x, y, BUTTON_WIDTH, BUTTON_HEIGHT );
        }
    }


    private final class ListSelectionHandler implements ListSelectionListener {

        public void valueChanged( ListSelectionEvent e ) {
            FontHandle handle = mListView.getLeadSelectionFont();
            mPreviewPane.testFont( handle == null ? null : handle.font() );
        }

    }

}
