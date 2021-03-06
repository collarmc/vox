package team.catgirl.vox.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class TokenGenerator {
    private static final SecureRandom RANDOM;

    static {
        try {
            RANDOM = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static byte[] byteToken(int size) {
        byte[] bytes = new byte[size];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    private TokenGenerator() {}
}
