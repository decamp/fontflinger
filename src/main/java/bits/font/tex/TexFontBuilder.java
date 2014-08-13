/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.tex;

import java.io.File;
import java.io.IOException;
import java.util.List;

import bits.font.FontFormat;
import bits.font.util.FileGarbage;

/**
 * Builds a single font. You probably want TexTypefaceBuilder.
 * 
 * @author Philip DeCamp
 */
@SuppressWarnings( "unused" )
public abstract class TexFontBuilder {

    public static TexFontBuilder getInstance( File fontFile ) throws IOException {
        FontFormat format = FontFormat.forFile( fontFile );

        switch( format ) {
        case TTF:
        case OTF:
            return new OtfTranslator( fontFile );

        case PFA:
        case PFB:
            return new PfTranslator( fontFile );

        default:
            throw new IOException( "Format not supported for file: " + fontFile.getPath() );

        }
    }


    public void setExtend( double amount ) {}

    public void setSlant( double amount ) {}

    public void setSmallcaps( boolean enable ) {}

    public void setOldStyleNumerals( boolean enable ) {}

    public abstract List<File> buildFont( String internalName, FileGarbage garbage ) throws IOException;

}
