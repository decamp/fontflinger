/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.font.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

import bits.font.FontTester;
import bits.util.gui.ImagePanel;


/**
 * Implementation of FontTester that displays each font on screen.
 * 
 * @author Philip DeCamp
 */
public class RasterizingFontTester implements FontTester {

    private final int mW;
    private final int mH;
    private final float mSize;

    private ImagePanel mPanel = null;
    private JFrame mFrame = null;


    public RasterizingFontTester( int w, int h, float size ) {
        mW = w;
        mH = h;
        mSize = size;
    }



    public void testFont( Font font ) {

        BufferedImage im = new BufferedImage( mW, mH, BufferedImage.TYPE_INT_ARGB );

        Graphics2D g = (Graphics2D)im.getGraphics();
        g.setColor( Color.WHITE );
        g.fillRect( 0, 0, mW, mH );
        g.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        g.setColor( Color.BLACK );

        font = font.deriveFont( mSize );
        g.setFont( font );
        int h = g.getFontMetrics().getHeight();
        h = (int)Math.max( h, mSize );

        g.drawString( "ABCDEFGHI", 10, h );
        g.drawString( "JKLMNOPQR", 10, h * 2 );
        g.drawString( "STUVWXYZ", 10, h * 3 );
        g.drawString( "abcdefghi", 10, h * 4 );
        g.drawString( "jklmnopqr", 10, h * 5 );
        g.drawString( "stuvwxyz", 10, h * 6 );

        if( mPanel != null ) {
            mPanel.setImage( im );
            mPanel.repaint();
        } else {
            mPanel = new ImagePanel( im );
            mFrame = ImagePanel.frameImagePanel( mPanel );
            mFrame.setVisible( true );
        }

        /*
         * try{ Thread.sleep(500L); }catch(Exception ex) {}
         */

    }

}
