import chain.IBlockChain;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import sign.AbstractExternalSignature;
import sign.NoOpSignature;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides blockchain functionality for pdf files
 */
public class PdfChain {

    private final AbstractExternalSignature externalSignature;
    private final IBlockChain blockChain;

    /**
     * Construct a new PdfChain object with a given IBlockchain implementation and AbstractExternalSignature implementation
     *
     * @param blockChain        the underlying blockchain to be used
     * @param externalSignature the signing and hashing methods to be used
     */
    public PdfChain(IBlockChain blockChain, AbstractExternalSignature externalSignature) {
        this.blockChain = blockChain;
        this.externalSignature = externalSignature;
    }

    /**
     * Construct a new PdfChain object with a given IBlockchain implementation
     *
     * @param blockChain the underlying blockchain to be used
     */
    public PdfChain(IBlockChain blockChain) {
        this.blockChain = blockChain;
        this.externalSignature = new NoOpSignature();
    }

    /**
     * Puts a pdfFile on the blockchain
     *
     * @param pdfFile the pdf file to be put on the blockchain
     * @return true iff the data was successfully put on the blockchain
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public boolean put(File pdfFile) throws IOException, GeneralSecurityException {
        return put(new FileInputStream(pdfFile), new HashMap<String, String>());
    }

    /**
     * Get all information related to a specific PDF File from the blockchain
     *
     * @param pdfFile
     * @return
     * @throws IOException
     */
    public List<Map<String, Object>> get(File pdfFile) throws IOException {
        return get(new FileInputStream(pdfFile));
    }

    /**
     * Get all information related to a specific PDF File from the blockchain
     *
     * @param pdfFile the file being queried
     * @return
     */
    public List<Map<String, Object>> get(InputStream pdfFile) throws IOException {

        // open document
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfFile));

        // get document properties
        PdfArray idArr = pdfDocument.getTrailer().getAsArray(PdfName.ID);
        String id1 = idArr.getAsString(0).toString();

        // close document
        pdfDocument.close();

        // return
        return blockChain.get(id1);
    }

    /**
     * Get all information related to a specific PDF document from the blockchain
     *
     * @param id1 the first ID of the PDF document
     * @return
     */
    public List<Map<String, Object>> get(String id1) {
        return blockChain.get(id1);
    }

    /**
     * Puts a pdfFile on the blockchain
     *
     * @param pdfFile   the pdf file being put on the blockchain
     * @param extraData extra attributes being added on the blockchain
     * @return true iff the data was successfully added to the blockchain
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public boolean put(InputStream pdfFile, Map<String, String> extraData) throws IOException, GeneralSecurityException {

        // open document
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfFile));

        // get document properties
        PdfArray idArr = pdfDocument.getTrailer().getAsArray(PdfName.ID);
        String id1 = idArr.getAsString(0).toString();
        String id2 = idArr.getAsString(1).toString();
        String hash = new String(externalSignature.hash(pdfFile));
        String signedHash = new String(externalSignature.encryptHash(pdfFile));

        // close document
        pdfDocument.close();

        // build data to put on chain
        Map<String, Object> dataOnChain = new HashMap<>();
        for (Map.Entry<String, String> en : extraData.entrySet()) {
            dataOnChain.put(en.getKey(), en.getValue());
        }
        dataOnChain.put("id1", id1);
        dataOnChain.put("id2", id2);
        dataOnChain.put("hsh", hash);
        dataOnChain.put("key", new String(externalSignature.getPublicKey().getEncoded()));
        dataOnChain.put("hshalgo", externalSignature.getHashAlgorithm());
        dataOnChain.put("sgnalgo", externalSignature.getEncryptionAlgorithm());
        dataOnChain.put("shsh", signedHash);

        // call blockchain implementation
        return blockChain.put(id1, dataOnChain);
    }

}
