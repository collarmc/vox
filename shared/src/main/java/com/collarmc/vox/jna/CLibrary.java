package com.collarmc.vox.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.ptr.PointerByReference;

public interface CLibrary extends Library {
        String JNA_LIBRARY_NAME = (com.sun.jna.Platform.isWindows() ? "msvcrt" : "c");
        NativeLibrary JNA_NATIVE_LIB = NativeLibrary.getInstance(CLibrary.JNA_LIBRARY_NAME);
        CLibrary INSTANCE = (CLibrary) Native.loadLibrary(CLibrary.JNA_LIBRARY_NAME, CLibrary.class);

        FILE fopen(String __filename, String __modes);
        int fclose(FILE pointerByReference);

        class FILE extends PointerByReference {};
}
