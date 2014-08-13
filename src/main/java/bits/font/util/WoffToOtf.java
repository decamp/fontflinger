/* 
 * Copyright (c) 2014, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.util;

import java.io.*;
import java.util.zip.*;
import bits.font.util.*;


/**
 * Converts WOFF font file to OTF.
 *
 * @author Philip DeCamp
 */
public final class WoffToOtf {
    
    @SuppressWarnings( "unused" )   //The unused variables provide documentation.
    public static void convert( File file, File outFile ) throws IOException {
        long fileLen = file.length();
        if( fileLen < 44 ) {
            throw new IOException( "Not a WOFF file or corrupted." );
        }
        
        RandomAccessFile in  = new RandomAccessFile( file, "r" );
        RandomAccessFile out = new RandomAccessFile( outFile, "rw" );
        
        try {
            int signature = in.readInt();
            if( signature != 0x774F4646 ) {
                throw new IOException( "Not a WOFF file or corrupted." );
            }
            
            int flavor         = in.readInt();
            int length         = in.readInt();
            int numTables      = in.readShort() & 0xFFFF;
            in.skipBytes( 2 );
            int totalSfntSize  = in.readInt();
            int majorVersion   = in.readShort() & 0xFFFF;
            int minorVersion   = in.readShort() & 0xFFFF;
            int metaOffset     = in.readInt();
            int metaLength     = in.readInt();
            int metaOrigLength = in.readInt();
            int privOffset     = in.readInt();
            int privLength     = in.readInt();
            
            //System.out.println( "Num Tables: " + numTables );
            //System.out.format( "Flavor: 0x%08X\n", flavor );
            out.writeInt( flavor );
            out.writeShort( numTables );
            
            int maximum       = Pots.floorPot( numTables );
            int searchRange   = maximum * 16;
            int entrySelector = (int)Math.round( Math.log( maximum ) / Math.log( 2.0 ) );
            int rangeShift    = numTables * 16 - searchRange;
            
            out.writeShort( searchRange );
            out.writeShort( entrySelector );
            out.writeShort( rangeShift );
            
            int offset = (int)out.getFilePointer();
            offset += numTables * 4 * 4;
            
            int[] inOffset   = new int[numTables];
            int[] compLength = new int[numTables];
            int[] origLength = new int[numTables];
            
            // Write index.
            for( int i = 0; i < numTables; i++ ) {
                int tag          = in.readInt();
                inOffset[i]      = in.readInt();
                compLength[i]    = in.readInt();
                origLength[i]    = in.readInt();
                int origChecksum = in.readInt();
                
                out.writeInt( tag );
                out.writeInt( origChecksum );
                out.writeInt( offset );
                out.writeInt( origLength[i] );
                
                offset += origLength[i];
                if( offset % 4 != 0 ) {
                    offset += 4 - offset % 4;
                }
            }
            
            // Write data.
            byte[] inBuf  = new byte[256];
            byte[] outBuf = new byte[256];
            
            for( int i = 0; i < numTables; i++ ) {
                in.seek( inOffset[i] );
                if( compLength[i] == origLength[i] ) {
                    transfer( in, origLength[i], inBuf, out );
                } else {
                    unzip( in, compLength[i], origLength[i], inBuf, outBuf, out ); 
                }
                
                int pad = (int)out.getFilePointer();
                if( pad % 4 != 0 ) {
                    pad = 4 - pad % 4;
                    for( int j = 0; j < pad; j++ ) {
                        out.write( 0 );
                    }
                }
            }
        } finally {
            in.close();
            out.close();
        }
    }
    
    
    
    private static void transfer( RandomAccessFile in, int n, byte[] buf, RandomAccessFile out ) throws IOException {
        while( n > 0 ) {
            int b = in.read( buf, 0, Math.min( n, buf.length ) );
            if( b <= 0 ) {
                throw new IOException( "Read failed." );
            }
            
            out.write( buf, 0, b );
            n -= b;
        }
    }
    
    
    private static void unzip( RandomAccessFile in, int inLen, int outLen, byte[] inBuf, byte[] outBuf, RandomAccessFile out ) throws IOException {
        //System.out.println( "Unzipping: "  + inLen + "\t" + outLen );
        int inLeft  = inLen;
        int written = 0;
        Inflater inflater = new Inflater();
        
        while( inLeft > 0 ) {
            int n = in.read( inBuf, 0, Math.min( inLeft, inBuf.length ) );
            if( n <= 0 ) {
                throw new IOException( "Read failed." );
            }
            inLeft -= n;
            inflater.setInput( inBuf, 0, n );
            
            while( !inflater.needsInput() ) {
                try {
                    int m = inflater.inflate( outBuf );
                    written += m;
                    if( written > outLen ) {
                        throw new IOException( "Decompression failed." );
                    }
                    out.write( outBuf, 0, m );
                } catch( DataFormatException ex ) {
                    throw new IOException( "Decompression failed." );
                }
            }
        }
        
        inflater.end();
        
        if( written != outLen ) {
            throw new IOException( "Decompression failed." );
        }
    }
    
    
    private WoffToOtf() {}
    
}
