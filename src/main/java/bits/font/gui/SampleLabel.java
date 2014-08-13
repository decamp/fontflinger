/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.gui;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.*;

import javax.swing.JComponent;


/**
 * @author Philip DeCamp
 */
public class SampleLabel extends JComponent {

    public static final String DEFAULT_TEXT = "Lorem ipsum dolor sit amet";

    private WeakHashMap<Font, Font> mFontMap = new WeakHashMap<Font, Font>();

    private final int mFontHeight;
    private final String mText;
    private Font mFont = null;


    public SampleLabel( int fontHeight ) {
        this( fontHeight, null );
    }

    
    public SampleLabel( int fontHeight, String text ) {
        if( text == null ) {
            text = DEFAULT_TEXT;
        }

        mFontHeight = fontHeight;
        mText = text;
    }

    
    @Override
    public void setFont( Font font ) {
        if( font == null ) {
            mFont = null;
            repaint();
            return;
        }

        Font f = mFontMap.get( font );

        if( f == null ) {
            f = font.deriveFont( (float)Math.round( mFontHeight * 0.5f ) );
            mFontMap.put( font, f );
        }

        if( mFont == f ) {
            return;
        }

        mFont = f;
        repaint();
    }


    @Override
    public void paint( Graphics g ) {
        if( mFont == null ) {
            return;
        }

        ((Graphics2D)g).setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        g.setColor( getForeground() );
        g.setFont( mFont );
        g.drawString( mText, 4, getHeight() * 2 / 3 );
    }
}
