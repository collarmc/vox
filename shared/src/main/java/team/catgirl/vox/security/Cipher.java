package team.catgirl.vox.security;

import team.catgirl.vox.api.Caller;
import team.catgirl.vox.api.Channel;

public interface Cipher {
    /**
     * Crypt a message to a group
     * @param sender who is sending the group message
     * @param recipient group of the message
     * @param bytes of the message
     * @return encrypted message
     */
    default byte[] crypt(Caller sender, Channel recipient, byte[] bytes) {
        return bytes;
    }

    /**
     * Decrypt a message from a group
     * @param sender who sent the group message
     * @param channel group of the message
     * @param bytes of the message
     * @return decrypted message
     */
    byte[] decrypt(Caller sender, Channel channel, byte[] bytes);
}
