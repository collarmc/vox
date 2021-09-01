package com.collarmc.vox.server;

import com.collarmc.vox.audio.rnnoise.Denoise;
import com.collarmc.vox.server.http.WebServer;

public class Main {
    public static void main(String[] args) throws Exception {
        Denoise denoise = new Denoise();
        WebServer webServer = new WebServer();
        webServer.start();
    }
}
