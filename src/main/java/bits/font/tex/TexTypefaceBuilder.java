/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.tex;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import bits.font.util.*;
import bits.util.Files;


/**
 * @author Philip DeCamp
 */
public class TexTypefaceBuilder {

    private final String                   mFamilyName;

    private final Map<File,TexFontBuilder> mBuilderMap = new HashMap<File,TexFontBuilder>();
    private final Map<Type,TexFontBuilder> mFontMap    = new HashMap<Type,TexFontBuilder>();
    private final EnumSet<TexFamily>       mDefaultSet = EnumSet.noneOf( TexFamily.class );


    public TexTypefaceBuilder( String familyName ) {
        mFamilyName = familyName;
    }



    public void setFont( String series, String shape, File sourceFile ) throws IOException {
        TexFontBuilder f = mBuilderMap.get( sourceFile );

        if( f == null ) {
            f = TexFontBuilder.getInstance( sourceFile );
            mBuilderMap.put( sourceFile, f );
        }

        setFont( series, shape, f );
    }


    public void setFont( String series, String shape, TexFontBuilder font ) {
        if( series == null )
            throw new NullPointerException( "series" );

        if( shape == null )
            throw new NullPointerException( "shape" );

        mFontMap.put( new Type( series, shape ), font );
    }


    public void makeThisTheDefaultTypefaceFor( TexFamily... families ) {
        Collections.addAll( mDefaultSet, families );
    }


    public void buildTypefacePackage( File dir ) throws IOException {
        buildTypefacePackage( dir, null );
    }


    public void buildTypefacePackage( File dir, String packageName ) throws IOException {
        doBuildTypefacePackage( dir, packageName );
    }


    public void buildTemplateTexProject( File dir ) throws IOException {
        buildExampleTexProject( dir, null );
    }


    public void buildExampleTexProject( File dir, String packageName ) throws IOException {
        if( packageName == null )
            packageName = mFamilyName;

        Map<TexFontBuilder,String> nameMap = doBuildTypefacePackage( dir, packageName );

        File texFile = new File( dir, "example.tex" );
        PrintWriter out = new PrintWriter( texFile );

        out.println( "\\documentclass[12pt]{article}" );
        out.println();
        out.format( "\\usepackage{%s}\n", packageName );
        out.println();
        out.println( "\\usepackage{fonttable}" );
        out.println();

        out.println( "\\title{Gulliver's Travels}" );
        out.println( "\\author{Jonathan Swift}" );
        out.println( "\\begin{document}" );
        out.println();
        out.println( "\\maketitle" );
        out.println();

        out.println( "The first request I made, after I had obtained my liberty, was, that I might have license " +
                     "to see Mildendo, the metropolis; which the emperor easily granted me, but with a special " +
                     "charge to do no hurt either to the inhabitants or their houses. The people had notice, by " +
                     "proclamation, of my design to visit the town. The wall which encompassed it is two feet and a " +
                     "half high, and at least eleven inches broad, so that a coach and horses may be driven very safely " +
                     "round it; and it is flanked with strong towers at ten feet distance. I stepped over the great " +
                     "western gate, and passed very gently, and sidling, through the two principal streets, only in " +
                     "my short waistcoat, for fear of damaging the roofs and eaves of the houses with the skirts of my " +
                     "coat. I walked with the utmost circumspection, to avoid treading on any stragglers who might remain " +
                     "in the streets, although the orders were very strict, that all people should keep in their houses, at " +
                     "their own peril. The garret windows and tops of houses were so crowded with spectators, that I thought " +
                     "in all my travels I had not seen a more populous place. The city is an exact square, each side of the " +
                     "wall being five hundred feet long. The two great streets, which run across and divide it into four " +
                     "quarters, are five feet wide. The lanes and alleys, which I could not enter, but only view them as " +
                     "I passed, are from twelve to eighteen inches. The town is capable of holding five hundred thousand " +
                     "souls: the houses are from three to five stories: the shops and markets well provided." );
        out.println();
        out.println( "\\textbf{The emperor's palace is in the centre} " +
                     "\\textit{of the city where the two great streets meet.}" +
                     " It is " +
                     "enclosed by a wall of two feet high, and twenty feet distance from the buildings. I had " +
                     "his majesty's permission to step over this wall; and, the space being so wide between that " +
                     "and the palace, I could easily view it on every side. The outward court is a square of forty " +
                     "feet, and includes two other courts: in the inmost are the royal apartments, which I was very " +
                     "desirous to see, but found it extremely difficult; for the great gates, from one square into " +
                     "another, were but eighteen inches high, and seven inches wide. Now the buildings of the outer " +
                     "court were at least five feet high, and it was impossible for me to stride over them without " +
                     "infinite damage to the pile, though the walls were strongly built of hewn stone, and four inches " +
                     "thick. At the same time the emperor had a great desire that I should see the magnificence of his " +
                     "palace; but this I was not able to do till three days after, which I spent in cutting down with my " +
                     "knife some of the largest trees in the royal park, about a hundred yards distant from the city. " +
                     "Of these trees I made two stools, each about three feet high, and strong enough to bear my weight. " +
                     "The people having received notice a second time, I went again through the city to the palace with " +
                     "my two stools in my hands. When I came to the side of the outer court, I stood upon one stool, and " +
                     "took the other in my hand; this I lifted over the roof, and gently set it down on the space between " +
                     "the first and second court, which was eight feet wide. I then stept over the building very " +
                     "conveniently from one stool to the other, and drew up the first after me with a hooked stick. " +
                     "By this contrivance I got into the inmost court; and, lying down upon my side, I applied my " +
                     "face to the windows of the middle stories, which were left open on purpose, and discovered the " +
                     "most splendid apartments that can be imagined. There I saw the empress and the young princes, in " +
                     "their several lodgings, with their chief attendants about them. Her imperial majesty was pleased " +
                     "to smile very graciously upon me, and gave me out of the window her hand to kiss." );

        out.println();
        out.println( "\\clearpage" );

        for( String name : nameMap.values() ) {
            out.format( "\\fonttable{%s}\n", name );
        }

        out.println();
        out.println( "\\end{document}" );
        out.close();
    }



    private Map<TexFontBuilder,String> doBuildTypefacePackage( File dir, String packageName ) throws IOException {

        if( packageName == null )
            packageName = mFamilyName;

        dir.mkdirs();

        // Only build fonts once.
        Map<TexFontBuilder,String> nameMap = new HashMap<TexFontBuilder,String>();

        StringBuilder shapeString = new StringBuilder();
        StringBuilder mapString   = new StringBuilder();
        FileGarbage garbage       = new FileGarbage();


        // Build fonts and assign internal names.
        for( Map.Entry<Type,TexFontBuilder> e : mFontMap.entrySet() ) {
            Type type         = e.getKey();
            TexFontBuilder fb = e.getValue();

            String name = nameMap.get( fb );

            if( name == null ) {
                // Font must be built.
                name = String.format( "%s_%s_%s", mFamilyName, type.mSeries, type.mShape );
                nameMap.put( fb, name );

                // Build font and add all the resulting files.
                List<File> files = fb.buildFont( name, garbage );

                for( File f : files ) {
                    if( Files.suffix( f ) == "map" ) {
                        // Map files get combined into a single map file.
                        appendMapFile( f, mapString );

                    } else {
                        // Otherwise, just copy everything.
                        File outFile = new File( dir, f.getName() );
                        NativeFiles.copy( f, outFile );
                    }
                }
            }
        }


        PrintWriter out;

        // Write out map file.
        File mapFile = new File( dir, packageName + ".map" );

        out = new PrintWriter( mapFile );
        out.print( mapString.toString() );
        out.close();


        // Write out style file.
        File styleFile = new File( dir, packageName + ".sty" );

        out = new PrintWriter( styleFile );
        out.println( "\\NeedsTeXFormat{LaTeX2e}" );
        out.format( "\\ProvidesPackage{%s}[%s Package for loading font family %s]\n",
                    packageName,
                    new SimpleDateFormat( "yyyy/MM/dd" ).format( new Date() ),
                    mFamilyName );
        
        out.println( "\\RequirePackage[T1]{fontenc}" );
        out.println( "\\RequirePackage[utf8]{inputenc}\n" );
        out.format( "\\pdfmapfile{+%s}\n", mapFile.getName() );
        out.format( "\\DeclareFontFamily{T1}{%s}{}\n", mFamilyName );

        for( Map.Entry<Type,TexFontBuilder> e : mFontMap.entrySet() ) {
            out.format( "\\DeclareFontShape{T1}{%s}{%s}{%s}{ <-> %s }{}\n",
                        mFamilyName,
                        e.getKey().mSeries,
                        e.getKey().mShape,
                        nameMap.get( e.getValue() ) );
        }

        out.println();

        if( !mDefaultSet.isEmpty() ) {
            for( TexFamily fam : mDefaultSet ) {
                out.format( "\\renewcommand*{\\%s}{%s}\n", fam.defaultVariable(), mFamilyName );
            }

            out.println();
        }

        out.println( "\\endinput" );
        out.close();

        garbage.empty();
        return nameMap;
    }



    private static void appendMapFile( File file, StringBuilder out ) throws IOException {
        BufferedReader br = new BufferedReader( new FileReader( file ) );

        for( String k = br.readLine(); k != null; k = br.readLine() ) {
            k = k.trim();
            if( k.isEmpty() || k.startsWith( "%" ) ) {
                continue;
            }
            out.append( k );
            out.append( '\n' );
        }

        br.close();
    }


    private static final class Type {

        final String mSeries;
        final String mShape;

        Type( String series, String shape ) {
            mSeries = ( series == null ? null : series.intern() );
            mShape  = ( shape == null ? null : shape.intern() );
        }


        @Override
        public int hashCode() {
            int m = ( mSeries == null ? 0 : mSeries.hashCode() );
            int n = ( mShape == null ? 0 : mShape.hashCode() );
            return m ^ n;
        }

        @Override
        public boolean equals( Object obj ) {
            if( !( obj instanceof Type ) ) {
                return false;
            }

            Type t = (Type)obj;
            return mSeries == t.mSeries && mShape == t.mShape;
        }

    }

}
