package team.catgirl.vox.client.debug;

import okhttp3.OkHttpClient;
import team.catgirl.vox.api.Caller;
import team.catgirl.vox.api.Channel;
import team.catgirl.vox.audio.devices.Devices;
import team.catgirl.vox.client.Vox;
import team.catgirl.vox.security.Cipher;

import java.util.UUID;

public class ClientMain {
    public static void main(String[] args) throws InterruptedException {
        OkHttpClient httpClient = new OkHttpClient();
        Devices devices = new Devices();
        Vox vox = new Vox(
                httpClient,
                "http://localhost:4000",
                new Caller(UUID.randomUUID()),
                new Channel(UUID.randomUUID()),
                devices.getDefaultInputDevice(),
                devices.getDefaultOutputDevice(),
                new Cipher() {
                    @Override
                    public byte[] decrypt(Caller sender, Channel channel, byte[] bytes) {
                        return bytes;
                    }

                    @Override
                    public byte[] crypt(Caller sender, Channel recipient, byte[] bytes) {
                        return bytes;
                    }
                }
        );
        vox.connect();

        while (true) {
            Thread.sleep(500);
        }
    }
}
