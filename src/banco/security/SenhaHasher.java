package banco.security;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hash de senha com PBKDF2 + salt aleatório (já vem no JDK, sem bibliotecas externas). */
public final class SenhaHasher {
    private static final int ITERACOES = 120_000;
    private static final int TAMANHO_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private SenhaHasher() {}

    /** Retorna "salt:hash" em Base64. */
    public static String gerarHash(char[] senha) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + ":" +
               Base64.getEncoder().encodeToString(derivar(senha, salt));
    }

    public static boolean verificar(char[] senha, String armazenado) {
        String[] partes = armazenado.split(":");
        byte[] salt = Base64.getDecoder().decode(partes[0]);
        byte[] esperado = Base64.getDecoder().decode(partes[1]);
        // isEqual compara em tempo constante (evita timing attack)
        return MessageDigest.isEqual(esperado, derivar(senha, salt));
    }

    private static byte[] derivar(char[] senha, byte[] salt) {
        try {
            var spec = new PBEKeySpec(senha, salt, ITERACOES, TAMANHO_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar hash", e);
        }
    }
}
