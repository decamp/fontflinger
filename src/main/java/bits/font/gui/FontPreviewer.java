/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.gui;

import javax.swing.*;
import java.awt.*;

import bits.font.*;
import bits.layout.*;

/**
 * @author Philip DeCamp
 */
public class FontPreviewer extends JPanel implements FontTester {

    private static final float DEFAULT_FONT_SIZE = 54.0f;
    private static String DEFAULT_TEXT = "ABCDEFGHIJKLM\nNOPQRSTUVWXYZ\nabcdefghijklm\nnopqrstuvwxyz\n0123456789\n.,!?@#$%^&*¤¦¨©"; 
    

    private final JTextArea    mTextArea;
    private final JScrollPane  mTextPane;
    
    private Font               mFont = null;
    

    FontPreviewer() {
        // Subtle gotcha: setMargin doesn't work with non-default border.
        mTextArea = new JTextArea() {
            @Override
            public Insets getInsets() {
                return new Insets( 15, 15, 15, 15 );
            }
        };
        
        mTextArea.setEditable( true );
        mTextArea.setText( DEFAULT_TEXT );
        mTextArea.setAutoscrolls( true );
        mTextArea.setWrapStyleWord( false );
        mTextArea.setBorder( null );

        mTextPane = new JScrollPane( mTextArea );
        mTextPane.setBorder( null );
        mTextPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        mTextPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
        mTextPane.setVisible( false );
        
        setLayout( new FillLayout( 1, 1, 1, 1 ) );
        add( mTextPane );

        setBackground( Color.LIGHT_GRAY );
        setOpaque( true );
    }
    
    
    public void testFont( Font font ) {
        setFont( font );
    }

    
    @Override
    public void setFont( Font font ) {
        if( mTextArea == null ) {
            return;
        }
        
        if( font == null ) {
            mFont = null;
            mTextPane.setVisible( false );
        } else {
            mFont = font.deriveFont( DEFAULT_FONT_SIZE );
            mTextArea.setFont( mFont );
            mTextPane.setVisible( true );
            //mTextArea.setText( GlyphLister.supportedGlyphs( font, 14 ) );
        }

        repaint();
    }

    
    @Override
    public Font getFont() {
        return mFont;
    }
   
}
