/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.font.gui;

import java.awt.Font;
import java.io.File;

import bits.font.FontFormat;
import bits.font.util.FileGarbage;
import bits.util.ref.AbstractRefable;


/**
 * @author Philip DeCamp
 */
public class FontHandle extends AbstractRefable {

    private final Font mFont;
    private final File mSourceFile;
    private final File mDataFile;
    private final FontFormat mFormat;

    private FileGarbage mGarbage;


    public FontHandle( File dataFile,
                       FontFormat format,
                       File sourceFile,
                       Font awtFont,
                       FileGarbage garbage )
    {
        mDataFile = dataFile;
        mFormat = format;
        mFont = awtFont;
        mSourceFile = sourceFile;
        mGarbage = garbage;

        if( mGarbage != null ) {
            mGarbage.ref();
        }
    }


    public File file() {
        return mDataFile;
    }

    public File sourceFile() {
        return mSourceFile;
    }

    public FontFormat format() {
        return mFormat;
    }

    public Font font() {
        return mFont;
    }



    @Override
    protected void finalize() throws Throwable {
        freeObject();
    }


    protected void freeObject() {
        if( mGarbage != null ) {
            mGarbage.deref();
            mGarbage = null;
        }
    }

}
