/*
 * This file is part of EntropyPool2.
 *
 * Copyleft 2016 Mark Jeronimus. All Rights Reversed.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalmodular.entropypool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.logging.Level;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import org.digitalmodular.utilities.LogTimer;
import org.digitalmodular.utilities.SecureRandomFactory;
import org.digitalmodular.utilities.Verifier;
import org.digitalmodular.utilities.container.LoggingCount;
import org.digitalmodular.utilities.container.LoggingVariable;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-25
public class EntropyPool2 implements EntropyPool {
	// The numbers that are 'most coprime' to 64 are 19 and 45. (Hexacontatetragram{64/19})
	// Subtracting 64s until total file size < 64kiB.
	public static final int DEFAULT_ENTROPY_POOL_BYTE_LENGTH = 65536 - 64 * 4 - 19;

	public static final String DEFAULT_SECURERANDOM_STRING  = "SP800CTR/AES/256/Strong/16777216";
	public static final String DEFAULT_MESSAGEDIGEST_STRING = "Keccak-512";
	public static final String DEFAULT_CIPHER_STRING        = "Threefish-1024/EAX/NoPadding";

	private final long         createDate;
	private final LoggingCount accessCount;

	private final LoggingVariable<SecureRandom>  secureRandom;
	private final LoggingVariable<MessageDigest> messageDigest;
	private final LoggingVariable<Cipher>        cipher;

	private final LoggingVariable<Long> injectedEntropy;
	private final LoggingVariable<Long> extractedEntropy;
	private final LoggingCount          mixCount;

	private int hashX;
	private int hashY;

	private final byte[] buffer;

	private transient int writePointer = 0;

	private final EntropyPoolMixer mixer = new MultipleMixer(
			new WhitenMixer(),
			new PermuteMixer(),
			new RehashMixer());

	public EntropyPool2(int newSize) throws NoSuchAlgorithmException, NoSuchPaddingException {
		Verifier.requireThat(newSize > 0, "newSize <= 0: " + newSize);

		createDate = System.currentTimeMillis();
		accessCount = new LoggingCount();

		secureRandom = new LoggingVariable<>(SecureRandomFactory.getInstance(DEFAULT_SECURERANDOM_STRING));
		messageDigest = new LoggingVariable<>(MessageDigest.getInstance(DEFAULT_MESSAGEDIGEST_STRING));
		cipher = new LoggingVariable<>(Cipher.getInstance(DEFAULT_CIPHER_STRING));

		injectedEntropy = new LoggingVariable<>(0L);
		extractedEntropy = new LoggingVariable<>(0L);
		mixCount = new LoggingCount();

		hashX = 0;
		hashY = 0;

		buffer = new byte[newSize];
	}

	public EntropyPool2(long createDate, LoggingCount accessCount, LoggingVariable<SecureRandom> secureRandom,
	                    LoggingVariable<MessageDigest> messageDigest, LoggingVariable<Cipher> cipher,
	                    LoggingVariable<Long> injectedEntropy, LoggingVariable<Long> extractedEntropy,
	                    LoggingCount mixCount, int hashX, int hashY, byte[] buffer) {
		Objects.requireNonNull(accessCount,
		                       "accessCount == null");
		Verifier.requireThat(accessCount.getCountDate() == 0 ||
		                     accessCount.getCountDate() >= createDate,
		                     "accessCount.countDate < createdDate: " +
		                     accessCount.getCountDate() + " < " + createDate);
		Objects.requireNonNull(secureRandom, "secureRandom == null");
		Verifier.requireThat(secureRandom.getModifyDate() == 0 ||
		                     secureRandom.getModifyDate() >= createDate,
		                     "secureRandom.modifyDate < createdDate: " +
		                     secureRandom.getModifyDate() + " < " + createDate);
		Objects.requireNonNull(messageDigest,
		                       "messageDigest == null");
		Verifier.requireThat(messageDigest.getModifyDate() == 0 ||
		                     messageDigest.getModifyDate() >= createDate,
		                     "messageDigest.modifyDate < createdDate: " +
		                     messageDigest.getModifyDate() + " < " + createDate);
		Objects.requireNonNull(cipher,
		                       "cipher == null");
		Verifier.requireThat(cipher.getModifyDate() == 0 ||
		                     cipher.getModifyDate() >= createDate,
		                     "cipher.modifyDate < createdDate: " +
		                     cipher.getModifyDate() + " < " + createDate);
		Objects.requireNonNull(injectedEntropy,
		                       "injectedEntropy == null");
		Verifier.requireThat(injectedEntropy.get() >= 0,
		                     "injectedEntropy.value < 0: " +
		                     injectedEntropy.get());
		Verifier.requireThat(injectedEntropy.get() <= buffer.length * 8,
		                     "injectedEntropy.value > buffer.length * 8: " +
		                     injectedEntropy.get() + " > " + buffer.length * 8);
		Verifier.requireThat(injectedEntropy.getModifyDate() == 0 ||
		                     injectedEntropy.getModifyDate() >= createDate,
		                     "injectedEntropy.modifyDate < createdDate: " +
		                     injectedEntropy.getModifyDate() + " < " + createDate);
		Objects.requireNonNull(extractedEntropy,
		                       "extractedEntropy == null");
		Verifier.requireThat(extractedEntropy.get() >= 0,
		                     "extractedEntropy.value < 0: " +
		                     extractedEntropy.get());
		Verifier.requireThat(extractedEntropy.get() <= buffer.length * 8,
		                     "extractedEntropy.value > buffer.length * 8: " +
		                     extractedEntropy.get() + " > " + buffer.length * 8);
		Verifier.requireThat(extractedEntropy.getModifyDate() == 0 ||
		                     extractedEntropy.getModifyDate() >= createDate,
		                     "extractedEntropy.modifyDate < createdDate: " +
		                     extractedEntropy.getModifyDate() + " < " + createDate);
		Objects.requireNonNull(mixCount,
		                       "mixCount == null");
		Verifier.requireThat(mixCount.getCountDate() == 0 ||
		                     mixCount.getCountDate() >= createDate,
		                     "mixCount.modifyDate < createdDate: " +
		                     mixCount.getCountDate() + " < " + createDate);
		Verifier.requireThat(hashX >= 0,
		                     "hashX < 0: " +
		                     hashX);
		Verifier.requireThat(hashX < buffer.length,
		                     "hashX >= buffer.length: " +
		                     hashX + " >= " + buffer.length);
		Verifier.requireThat(hashY >= 0,
		                     "hashY < 0: " +
		                     hashY);
		Verifier.requireThat(hashY < buffer.length,
		                     "hashY >= buffer.length: " +
		                     hashY + " >= " + buffer.length);
		Objects.requireNonNull(buffer,
		                       "buffer == null");
		Verifier.requireThat(buffer.length >= messageDigest.get().getDigestLength(),
		                     "buffer.length < messageDigest.digestLength: " +
		                     buffer.length + " < " + messageDigest.get().getDigestLength());

		this.createDate = createDate;
		this.accessCount = accessCount;
		this.secureRandom = secureRandom;
		this.messageDigest = messageDigest;
		this.cipher = cipher;
		this.injectedEntropy = injectedEntropy;
		this.extractedEntropy = extractedEntropy;
		this.mixCount = mixCount;
		this.hashX = hashX;
		this.hashY = hashY;
		this.buffer = buffer;
	}

	public static EntropyPool2 newInstance() throws NoSuchAlgorithmException, NoSuchPaddingException {
		return new EntropyPool2(DEFAULT_ENTROPY_POOL_BYTE_LENGTH);
	}

	public static EntropyPool2 loadFromFile(File poolFile) throws IOException {
		Objects.requireNonNull(poolFile,
		                       "poolFile == null");
		Verifier.requireThat(poolFile.exists(),
		                     "poolFile.exists() == false: " +
		                     poolFile);
		Verifier.requireThat(poolFile.canRead(),
		                     "poolFile.canRead() == false: " +
		                     poolFile);

		EntropyPool pool = EntropyPoolLoader.loadFromFile(poolFile);

		if (!(pool instanceof EntropyPool2))
			throw new IllegalArgumentException("File is not version 2.0. You can use" +
			                                   " EntropyPoolLoader.loadPoolFromFile() to load any file version.");

		return (EntropyPool2) pool;
	}

	public void saveToFile(File poolFile, File bakFile, File tempFile) throws IOException {
		Objects.requireNonNull(poolFile,
		                       "poolFile == null");
		Objects.requireNonNull(bakFile,
		                       "bakFile == null");
		Objects.requireNonNull(tempFile,
		                       "tempFile == null");

		EntropyPool2Saver.saveToFile(this, tempFile);

		if (poolFile.exists())
			Files.move(poolFile.toPath(), bakFile.toPath(), StandardCopyOption.ATOMIC_MOVE);

		Files.move(tempFile.toPath(), poolFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
	}

	public long getCreateDate()                               { return createDate; }

	public int getAccessCount()                               { return accessCount.get(); }

	public long getAccessDate()                               { return accessCount.getCountDate(); }

	public void incrementAccessCount()                        { accessCount.count(); }

	public SecureRandom getSecureRandom()                     { return secureRandom.get(); }

	public int getSecureRandomModifyCount()                   { return secureRandom.getModifyCount(); }

	public long getSecureRandomModifyDate()                   { return secureRandom.getModifyDate(); }

	public void setSecureRandom(SecureRandom secureRandom)    { this.secureRandom.set(secureRandom); }

	public MessageDigest getMessageDigest()                   { return messageDigest.get(); }

	public int getMessageDigestModifyCount()                  { return messageDigest.getModifyCount(); }

	public long getMessageDigestModifyDate()                  { return messageDigest.getModifyDate(); }

	public void setMessageDigest(MessageDigest messageDigest) { this.messageDigest.set(messageDigest); }

	public Cipher getCipher()                                 { return cipher.get(); }

	public int getCipherModifyCount()                         { return cipher.getModifyCount(); }

	public long getCipherModifyDate()                         { return cipher.getModifyDate(); }

	public void setCipher(Cipher cipher)                      { this.cipher.set(cipher); }

	public long getInjectedEntropy()                          { return injectedEntropy.get(); }

	public int getInjectedEntropyModifyCount()                { return injectedEntropy.getModifyCount(); }

	public long getInjectedEntropyModifyDate()                { return injectedEntropy.getModifyDate(); }

	@Override
	public void injectEntropy(byte[] bytes, int entropyBits) {
		Objects.requireNonNull(bytes,
		                       "bytes == null");
		Verifier.requireThat(bytes.length > 0,
		                     "bytes.length == 0");

		for (byte b : bytes) {
			buffer[writePointer++] ^= b;

			if (writePointer == buffer.length) {
				writePointer = 0;
				mix();
			}
		}

		injectedEntropy.modify(oldValue -> Math.min(oldValue + entropyBits, buffer.length * 8));
	}

	public long getExtractedEntropy()           { return extractedEntropy.get(); }

	public int getExtractedEntropyModifyCount() { return extractedEntropy.getModifyCount(); }

	public long getExtractedEntropyModifyDate() { return extractedEntropy.getModifyDate(); }

	@Override
	public byte[] extractEntropy(int numBytes) {
		if (numBytes * 8 > getAvailableEntropy())
			throw new IllegalStateException(
					"More entropy requested than is available: " + numBytes * 8 + " > " + getAvailableEntropy());

		if (writePointer > 0)
			mix();

		byte[] bytes = new byte[numBytes];
		for (int i = 0; i < buffer.length; i++)
			bytes[i % numBytes] ^= buffer[i];

		mix();

		extractedEntropy.modify(oldValue -> Math.addExact(oldValue, numBytes * 8));

		return bytes;
	}

	public int getMixCount() { return mixCount.get(); }

	public long getMixDate() { return mixCount.getCountDate(); }

	@Override
	public void mix() {
		LogTimer.start();

		mixer.mix(this);

		mixCount.count();
		writePointer = 0;

		LogTimer.finishAndLog(Level.FINE, "Mixed the Entropy Pool in {0} seconds");
	}

	LoggingCount accessCount()                     { return accessCount; }

	LoggingVariable<SecureRandom> secureRandom()   { return secureRandom; }

	LoggingVariable<MessageDigest> messageDigest() { return messageDigest; }

	LoggingVariable<Cipher> cipher()               { return cipher; }

	LoggingVariable<Long> injectedEntropy()        { return injectedEntropy; }

	LoggingVariable<Long> extractedEntropy()       { return extractedEntropy; }

	LoggingCount mixCount()                        { return mixCount; }

	int hashX()                                    { return hashX; }

	void hashX(int hashX)                          { this.hashX = hashX; }

	int hashY()                                    { return hashY; }

	void hashY(int hashY)                          { this.hashY = hashY; }

	byte[] buffer()                                { return buffer; }
}

