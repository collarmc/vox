package team.catgirl.vox.server;

import team.catgirl.vox.audio.opus.OpusSettings;
import team.catgirl.vox.server.http.WebServer;

public class Main {
    public static void main(String[] args) throws Exception {
        OpusSettings.initializeCodec();
        WebServer webServer = new WebServer();
        webServer.start();
    }
}
