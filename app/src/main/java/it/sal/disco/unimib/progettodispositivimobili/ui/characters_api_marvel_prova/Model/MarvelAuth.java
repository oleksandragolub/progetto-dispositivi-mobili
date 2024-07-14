package it.sal.disco.unimib.progettodispositivimobili.ui.characters_api_marvel_prova.Model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MarvelAuth {
    public static String generateMarvelHash(String ts, String privateKey, String publicKey) {
        try {
            String value = ts + privateKey + publicKey;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(value.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

