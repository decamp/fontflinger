/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.gui;

import java.awt.*;
import javax.swing.*;
import bits.layout.LayoutAdapter;



class FontLabel extends JPanel {

    private static final Dimension DIM = new Dimension( 10, 10 );
    private final JLabel           mNameLabel;
    private final JLabel           mFileLabel;
    private final SampleLabel      mSampleLabel;


    FontLabel( int fontSize ) {
        setLayout( new Layout() );

        mNameLabel = new JLabel();
        mFileLabel = new JLabel();
        mSampleLabel = new SampleLabel( fontSize );

        add( mNameLabel );
        add( mFileLabel );
        add( mSampleLabel );

        setOpaque( true );
    }



    @Override
    public void setForeground( Color color ) {
        super.setForeground( color );

        if( mNameLabel == null )
            return;

        mNameLabel.setForeground( color );
        mFileLabel.setForeground( color );
        mSampleLabel.setForeground( color );
    }

    @Override
    public void setBackground( Color color ) {
        super.setBackground( color );

        if( mNameLabel == null )
            return;

        mNameLabel.setBackground( color );
        mFileLabel.setBackground( color );
        mSampleLabel.setBackground( color );
    }



    void setFont( FontHandle font ) {
        Font jfont = font.font();

        if( jfont != null ) {
            mNameLabel.setText( jfont.getName() );
            mSampleLabel.setFont( jfont );
            mSampleLabel.setVisible( true );
        } else {
            mNameLabel.setText( "" );
            mSampleLabel.setVisible( false );
        }

        mFileLabel.setText( font.file().getName() );
    }



    private class Layout extends LayoutAdapter {

        public void layoutContainer( Container cont ) {
            int w = cont.getWidth();
            int h = cont.getHeight();
            int x = 5;

            mFileLabel.setBounds( x, 0, w / 4, h );
            x += w / 4;

            mNameLabel.setBounds( x, 0, w / 4, h );
            x += w / 4;

            mSampleLabel.setBounds( x, 0, w - x, h );
        }

        public void addLayoutComponent( String arg, Component comp ) {}

        public Dimension minimumLayoutSize( Container cont ) {
            return DIM;
        }

        public Dimension preferredLayoutSize( Container cont ) {
            return DIM;
        }

        public void removeLayoutComponent( Component comp ) {}

    }

}