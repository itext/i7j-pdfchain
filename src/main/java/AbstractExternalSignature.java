import sun.misc.IOUtils;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class aggregates all the information needed to produce a (signed) hash
 * This includes: a hashing algorithm, a signing algorithm,  a public/private keypair
 */
public abstract class AbstractExternalSignature {

    /**
     * Get the hashing algorithm
     *
     * @return
     */
    public abstract String getHashAlgorithm();

    /**
     * Get the encryption algorithm
     *
     * @return
     */
    public abstract String getEncryptionAlgorithm();

    /**
     * Get the private key
     *
     * @return
     */
    public abstract Key getPrivateKey();

    /**
     * Get the public key
     *
     * @return
     */
    public abstract Key getPublicKey();

    /**
     * Calculate the unsigned hash for a given pdf file
     *
     * @param pdfFile
     * @return
     */
    public byte[] hash(File pdfFile) {
        try {
            MessageDigest complete = MessageDigest.getInstance(getHashAlgorithm());
            return complete.digest(IOUtils.readFully(new FileInputStream(pdfFile), 0, false));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }

    /**
     * Calculate the the signed hash for a given pdf file
     *
     * @param pdfFile
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public byte[] encryptHash(File pdfFile) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance(getEncryptionAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey());
        byte[] cipherData = cipher.doFinal(hash(pdfFile));
        return cipherData;
    }

}
