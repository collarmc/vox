package com.collarmc.vox.server;

import com.collarmc.vox.audio.dsp.EchoCanceller;
import com.collarmc.vox.server.http.WebServer;

public class Main {
    public static void main(String[] args) throws Exception {
        WebServer webServer = new WebServer();
        webServer.start();
    }
}
