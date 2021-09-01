package com.collarmc.vox.dialer;

import com.collarmc.vox.client.admin.ChannelServiceClient;
import okhttp3.OkHttpClient;
import com.collarmc.vox.api.Caller;
import com.collarmc.vox.api.Channel;
import com.collarmc.vox.api.http.ChannelService.PermitAccessRequest;
import com.collarmc.vox.audio.devices.Devices;
import com.collarmc.vox.client.Vox;
import com.collarmc.vox.security.Cipher;

import java.util.UUID;

public class Dialer {
    public static void main(String[] args) throws InterruptedException {

        if (args.length != 2) {
            System.err.println("Vox Dialer");
            System.err.println("Usage:");
            System.err.println("dialer [url] [password]");
            System.out.println();
            return;
        }

        String serverUrl = args[0];
        System.out.println("Server '" + serverUrl + "'");
        String password = args[1];
        System.out.println("Password '" + password + "'");

        OkHttpClient httpClient = new OkHttpClient();
        Devices devices = new Devices();

        Channel channel = new Channel(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        Caller caller = new Caller(UUID.randomUUID());

        System.out.print(caller + " requesting grant for channel " + channel);
        ChannelServiceClient channelServiceClient = new ChannelServiceClient(httpClient, serverUrl, password);
        byte[] token = channelServiceClient.permit(new PermitAccessRequest(channel, caller)).token;
        System.out.println("... grant received");

        System.out.print("Dialing...");
        Vox vox = new Vox(
                httpClient,
                serverUrl,
                token,
                caller,
                channel,
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

        System.out.println(" ...connected!");

        while (true) {
            Thread.sleep(500);
        }
    }
}
