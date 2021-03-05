package team.catgirl.vox.server;

import team.catgirl.vox.audio.OpusSettings;
import team.catgirl.vox.server.http.WebServer;

public class Main {
    public static void main(String[] args) {
        OpusSettings.initializeCodec();
        WebServer webServer = new WebServer();
        webServer.start();
    }
}
