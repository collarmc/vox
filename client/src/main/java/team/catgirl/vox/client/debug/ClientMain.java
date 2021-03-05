package team.catgirl.vox.client.debug;

import okhttp3.OkHttpClient;
import team.catgirl.vox.audio.devices.Devices;
import team.catgirl.vox.client.Vox;

import java.util.UUID;

public class ClientMain {
    public static void main(String[] args) throws InterruptedException {
        OkHttpClient httpClient = new OkHttpClient();
        Devices devices = new Devices();
        Vox vox = new Vox(httpClient, "http://localhost:4000", UUID.randomUUID(), UUID.randomUUID(), devices.getDefaultInputDevice(), devices.getDefaultOutputDevice());
        vox.connect();

        while (true) {
            Thread.sleep(500);
        }
    }
}
