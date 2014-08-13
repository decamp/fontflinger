/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.Dialog.ModalityType;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.swing.*;

import bits.font.tex.*;
import bits.layout.ComponentStacker;
import bits.layout.LayoutAdapter;


/**
 * @author Philip DeCamp
 */
@SuppressWarnings( "rawtypes" )
class LatexExporter extends JPanel {
    
    private final JLabel           mFamilyLabel;
    private final JTextField       mFamilyField;
    private final JLabel           mNoticeLabel;

    private final JFileChooser     mChooser;
    private final List<FontPanel>  mFontPanels;
    private final ComponentStacker mFontStacker;

    private final JButton          mOkButton;
    private final JButton          mCancelButton;

    private final JDialog          mDialog;


    LatexExporter( Component parent, List<FontHandle> fonts, JFileChooser chooser ) {
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
        

        mFamilyLabel = new JLabel( "Name" );
        mFamilyLabel.setHorizontalAlignment( JLabel.RIGHT );
        mFamilyField = new JTextField( "" );
        mNoticeLabel = new JLabel( "Note that Latex maps \"\\textbf\"\nto BOLD_EXTENDED, not BOLD." );

        add( mFamilyLabel );
        add( mFamilyField );
        add( mNoticeLabel );

        mFontStacker = new ComponentStacker();
        mFontStacker.setStackDirection( ComponentStacker.STACK_VERTICAL );
        mFontStacker.setStretch( true );
        mFontStacker.setMargins( 0, 0 );

        mFontStacker.setBorder( BorderFactory.createTitledBorder( "Fonts" ) );
        mFontStacker.setBackground( Color.WHITE );
        add( mFontStacker );
        int i = 0;

        mFontPanels = new ArrayList<FontPanel>();

        for( FontHandle f : fonts ) {
            FontPanel p = new FontPanel( f );
            p.setSize( 50, 75 );

            if( i++ % 2 == 0 ) {
                p.setBackground( Palette.ODD_COLOR );
            } else {
                p.setBackground( Palette.EVEN_COLOR );
            }

            mFontStacker.add( p );
            mFontPanels.add( p );
        }


        while( parent.getParent() != null ) {
            parent = parent.getParent();
        }

        mCancelButton = new JButton( "Cancel" );
        mOkButton = new JButton( "OK" );

        add( mCancelButton );
        add( mOkButton );

        mCancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doCancel();
            }
        } );

        mOkButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doOkay();
            }
        } );


        setLayout( new Layout() );
        setOpaque( true );
        // setBackground(Color.WHITE);

        mDialog = new JDialog( (Window)parent, "Configure Tex Typeface", ModalityType.APPLICATION_MODAL );
        mDialog.setContentPane( this );
        mDialog.setSize( 550, 650 );
        mDialog.setLocationRelativeTo( null );
        mDialog.setVisible( true );
    }



    private void doCancel() {
        mDialog.setVisible( false );
    }


    private void doOkay() {
        int fontCount = 0;

        for( FontPanel fp : mFontPanels ) {
            if( fp.isSelected() ) {
                fontCount++;
            }
        }

        if( fontCount == 0 ) {
            JOptionPane.showMessageDialog( this, "No fonts selected.", "Error", JOptionPane.WARNING_MESSAGE );
            return;
        }

        String name = mFamilyField.getText().trim();
        if( name.length() == 0 ) {
            JOptionPane.showMessageDialog( this, "Invalid typeface name.", "Error", JOptionPane.WARNING_MESSAGE );
            return;
        }
        
        
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
            
        TexTypefaceBuilder b = new TexTypefaceBuilder( mFamilyField.getText() );

        try {
            for( FontPanel fp : mFontPanels ) {
                fp.build( b );
            }

            b.makeThisTheDefaultTypefaceFor( TexFamily.ROMAN, TexFamily.SAN_SERIF, TexFamily.TYPE_WRITER );
            b.buildExampleTexProject( outDir, name );

        } catch( Exception ex ) {
            JOptionPane.showMessageDialog( this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE );
            return;
        }

        mDialog.setVisible( false );
    }



    private final class FontPanel extends JPanel implements LayoutManager {

        private final FontHandle   mFont;
        private final JCheckBox    mEnableBox;
        private final JLabel       mLabel;
        private final EnumSelector mSeriesSelector;
        private final EnumSelector mShapeSelector;


        public FontPanel( FontHandle font ) {
            mFont = font;
            setLayout( this );

            mEnableBox = new JCheckBox();
            mEnableBox.setSelected( true );
            add( mEnableBox );

            mLabel = new JLabel();
            String name = null;

            if( font.font() != null )
                name = font.font().getName();

            if( name == null || name.length() == 0 )
                name = font.file().getName();

            mLabel.setText( name );
            add( mLabel );

            Map<Enum,String> seriesMap = new LinkedHashMap<Enum,String>();
            Map<Enum,String> shapeMap = new LinkedHashMap<Enum,String>();

            for( TexSeries s : TexSeries.values() ) {
                seriesMap.put( s, s.code() );
            }

            for( TexShapes s : TexShapes.values() ) {
                shapeMap.put( s, s.code() );
            }

            mSeriesSelector = new EnumSelector( "Series:", seriesMap );
            add( mSeriesSelector );

            mShapeSelector = new EnumSelector( "Shape:", shapeMap );
            add( mShapeSelector );

            mLabel.setOpaque( false );
            setOpaque( true );
        }
        


        void build( TexTypefaceBuilder b ) throws IOException {
            if( !mEnableBox.isSelected() )
                return;

            TexFontBuilder fb = TexFontBuilder.getInstance( mFont.file() );
            b.setFont( mSeriesSelector.getText(), mShapeSelector.getText(), fb );
        }


        boolean isSelected() {
            return mEnableBox.isSelected();
        }



        public void setBackground( Color c ) {
            super.setBackground( c );

            if( mLabel == null )
                return;

            mLabel.setBackground( c );
            mSeriesSelector.setBackground( c );
            mShapeSelector.setBackground( c );
        }

        
        public void layoutContainer( Container c ) {
            final int MARGIN = 2;
            final int w = getWidth();
            final int h = getHeight();

            int x = MARGIN;
            int y = MARGIN;
            int ww;
            int hh = ( h - MARGIN * 4 ) / 3;

            mEnableBox.setBounds( x, y, hh, hh );
            x += hh + MARGIN;
            mLabel.setBounds( x, y, w - x - MARGIN, hh );

            y += hh;
            x = MARGIN;
            ww = w - MARGIN;

            mSeriesSelector.setBounds( x, y, ww, hh );

            y += hh;
            mShapeSelector.setBounds( x, y, ww, hh );
        }


        public void addLayoutComponent( String s, Component c ) {}


        public Dimension minimumLayoutSize( Container c ) {
            return null;
        }


        public Dimension preferredLayoutSize( Container c ) {
            return null;
        }


        public void removeLayoutComponent( Component c ) {}

    }



    private final class Layout extends LayoutAdapter {

        @Override
        public void layoutContainer( Container cont ) {
            final int MARGIN = 4;
            final int w = getWidth();
            final int h = getHeight();

            int x = MARGIN * 3;
            int y = MARGIN;
            int hh = 24;
            int ww = 40;

            mFamilyLabel.setBounds( x, y, ww, hh );
            x += ww + MARGIN * 2;
            ww = Math.min( w - x - MARGIN, 160 );

            mFamilyField.setBounds( x, y, ww, hh );
            y += hh + MARGIN;
            x = MARGIN;

            hh *= 2;
            mNoticeLabel.setBounds( x + MARGIN, y, w - MARGIN * 4, hh );
            y += hh + MARGIN;

            hh = h - y - MARGIN * 2 - 34;
            mFontStacker.setBounds( x, y, w - MARGIN * 2, hh );
            y += hh + MARGIN;

            ww = 80;
            hh = 34;
            mCancelButton.setBounds( x, y, ww, hh );
            mOkButton.setBounds( w - ww - MARGIN, y, ww, hh );
        }

    }

}
