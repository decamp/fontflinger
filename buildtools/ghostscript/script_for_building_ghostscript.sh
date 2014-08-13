#!/bin/bash

# You could use a normal, full version of ghostscript (GS), but it's something like 30 MB.
# This script just makes a smaller and probably barely functional version.
# Must be copied into ghostscript source directory and called from there.

# 1: Download ghostscript. Unzip.
# 2: Copy this file into ghostscript directory.
# 3: Run this file.

# Remove bulky, superfluous resources.
cd Resource
mkdir tmp
cd Font
mv * ../tmp
touch `cd ../tmp;find .`
rm ../tmp/*
cd ../CMap
mv * ../tmp
touch `cd ../tmp;find .`
rm -r ../tmp
cd ../..

#Configure
configure --disable-dbus --disable-cups --disable-gtk --without-libidn --without-libpaper --without-pdftoraster --without-ijs --without-luratech --without-jbig2dec --without-jasper --with-drivers=PS --without-x CFLAGS="-arch x86_64" LDFLAGS="-arch x86_64"

#Compile
make

#Rename binary
mv bin/gs bin/gs_partial