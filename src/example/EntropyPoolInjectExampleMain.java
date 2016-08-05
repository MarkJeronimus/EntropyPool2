package example;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.NoSuchPaddingException;
import javax.swing.SwingUtilities;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.digitalmodular.entropypool.EntropyPool2;
import org.digitalmodular.utilities.swing.ShowFileChooserAction;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-29
public class EntropyPoolInjectExampleMain {
	private static final File ENTROPY_POOL_FILE      = new File("r:\\entropypool.bin");
	private static final File ENTROPY_POOL_FILE_BAK  = new File("r:\\entropypool.bak");
	private static final File ENTROPY_POOL_FILE_TEMP = new File("r:\\entropypool.tmp");

	private static final Logger LOGGER = Logger.getLogger(EntropyPoolInjectExampleMain.class.getName());

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(EntropyPoolInjectExampleMain::new);
	}

	private EntropyPool2 pool;

	private final ShowFileChooserAction showFileChooserAction = new ShowFileChooserAction(new EntropyFromFilesAdder());

	public EntropyPoolInjectExampleMain() {
		try {
			if (ENTROPY_POOL_FILE.exists() && ENTROPY_POOL_FILE.canRead()) {
				pool = EntropyPool2.loadFromFile(ENTROPY_POOL_FILE);
				pool.incrementAccessCount();
			} else {
				pool = EntropyPool2.newInstance();
			}

			showFileChooserAction.actionPerformed(null);

			pool.saveToFile(ENTROPY_POOL_FILE, ENTROPY_POOL_FILE_BAK, ENTROPY_POOL_FILE_TEMP);
		} catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	private class EntropyFromFilesAdder implements Consumer<File[]> {
		@Override
		public void accept(File[] filesOrDirectories) {
			for (File fileOrDirectory : filesOrDirectories) {
				try {
					pool.injectEntropyFromFileOrDirectory(fileOrDirectory);
				} catch (IOException ex) {
					LOGGER
							.log(Level.SEVERE, ex.getMessage(), ex);
				}
			}
		}
	}
}
