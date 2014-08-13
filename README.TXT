THE FONT LIBRARY:
Library and gui for font conversion. Essentially a wrapper around a bunch of separate utilities, like Fondu and Ghostscript. 
Will convert nearly any font to a format that can be used in Java or Latex.

Currently supports: ttf, otc, pfa, pfb, ttc, tfil, ffil, lwfn



BUILDING:
[ TODO ]


RUNTIME:
1. All files in "lib" and "dist" directories (after build) are required in classpath. 


DEPENDENCIES:
Ghostscript - This library comes with a stripped down, minimal build of Ghostscript, which is like 30 stupid MiBs for one little conversion than nothing else does.

Fondu - Great at converting fonts, but hasn't been updated in years. This is what causes the OS to prompt you to install that PowerPC emulator (Rosetta or whatever) if you haven't already installed it.

A bunch of others.


KNOWN ISSUES:


MAINTAINED BY:
Philip DeCamp