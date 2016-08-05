package example;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.digitalmodular.entropypool.EntropyPool2;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-29
public class EntropyPoolExtractExampleMain {
	private static final File ENTROPY_POOL_FILE      = new File("r:\\entropypool.bin");
	private static final File ENTROPY_POOL_FILE_BAK  = new File("r:\\entropypool.bak");
	private static final File ENTROPY_POOL_FILE_TEMP = new File("r:\\entropypool.tmp");

	private static final Logger LOGGER = Logger.getLogger(EntropyPoolExtractExampleMain.class.getName());

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(EntropyPoolExtractExampleMain::new);
	}

	private EntropyPool2 pool;

	public EntropyPoolExtractExampleMain() {
		try {
			pool = EntropyPool2.loadFromFile(ENTROPY_POOL_FILE);
			pool.incrementAccessCount();

			byte[] entropy = pool.extractEntropy(1600);
			System.out.println("Extracted entropy: " + Arrays.toString(entropy));
			System.out.println("Available entropy: " + pool.getAvailableEntropy());

			pool.saveToFile(ENTROPY_POOL_FILE, ENTROPY_POOL_FILE_BAK, ENTROPY_POOL_FILE_TEMP);
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}
}
