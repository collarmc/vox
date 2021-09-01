package com.collarmc.vox.jna;

import com.google.common.io.ByteStreams;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LibraryLoader {

    private static final LibraryLoader INSTANCE = new LibraryLoader();
    private final Map<Class<?>, Object> library = new ConcurrentHashMap<>();

    public static <T> T load(String name, Class<T> aClass) {
        return INSTANCE.loadLibrary(name, aClass);
    }

    @SuppressWarnings("unchecked")
    private  <T> T loadLibrary(String name, Class<T> aClass) {
        return (T)library.computeIfAbsent(aClass, aClass1 -> {
            String path = getLibrary(name);
            File lib;
            try {
                lib = File.createTempFile(name, ".lib");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try (InputStream is = LibraryLoader.class.getResourceAsStream("/" + path);
                 FileOutputStream os = new FileOutputStream(lib)) {
                if (is == null) {
                    throw new IllegalStateException("could not find " + path);
                }
                ByteStreams.copy(is, os);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return Native.loadLibrary(lib.getAbsolutePath(), aClass);
        });
    }

    private static String getLibrary(String name) {
        boolean is64Bit = Native.POINTER_SIZE == 8;
        if (Platform.isWindows()) {
            if (is64Bit) {
                return getPath("windows64", name + ".dll");
            } else {
                return getPath("windows", name + ".dll");
            }
        }
        if (Platform.isMac()) {
            // check for Apple Silicon
            if(Platform.isARM()) {
                return getPath("mac/aarch64", name + ".dylib");
            } else {
                return getPath("mac/intel", name + ".dylib");
            }
        }
        if (Platform.isARM()) {
            return getPath("armv6", name + ".so");
        }
        if (Platform.isLinux()) {
            if (is64Bit) {
                return getPath("linux64", name + ".so");
            } else {
                return getPath("linux", name + ".so");
            }
        }
        String message = String.format("Unsupported platform: %s/%s", System.getProperty("os.name"),
                System.getProperty("os.arch"));
        throw new IllegalStateException(message);
    }

    private static String getPath(String folder, String name) {
        String separator = "/";
        return folder + separator + name;
    }

    private LibraryLoader() {}
}
