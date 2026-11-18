package de.omegazirkel.risingworld.tools.privacy;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

/** Signs a destructive service request without sending the game server's service credential. */
public final class ErasureMessageSigner {
    private ErasureMessageSigner() { }

    public static String sign(String credential, String... fields) throws GeneralSecurityException {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(credential.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(hmac.doFinal(String.join("\n", fields).getBytes(StandardCharsets.UTF_8)));
    }
}
