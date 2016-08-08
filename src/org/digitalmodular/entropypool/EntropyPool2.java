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
import java.util.logging.Level;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import static java.util.Objects.requireNonNull;
import static org.digitalmodular.utilities.Verifier.requireThat;
import org.digitalmodular.utilities.LogTimer;
import org.digitalmodular.utilities.SecureRandomFactory;
import org.digitalmodular.utilities.container.LoggingCount;
import org.digitalmodular.utilities.container.LoggingVariable;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-25
public class EntropyPool2 implements EntropyPool {
	// The number in the range [65261,65275) that's 'most coprime' to 64 is 65261.
	public static final int DEFAULT_ENTROPY_POOL_BYTE_LENGTH = 65261;

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

	public EntropyPool2(int size) throws NoSuchAlgorithmException, NoSuchPaddingException {
		requireThat(size > 0, "size <= 0: " + size);

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

		buffer = new byte[size];
	}

	public EntropyPool2(long createDate, LoggingCount accessCount, LoggingVariable<SecureRandom> secureRandom,
	                    LoggingVariable<MessageDigest> messageDigest, LoggingVariable<Cipher> cipher,
	                    LoggingVariable<Long> injectedEntropy, LoggingVariable<Long> extractedEntropy,
	                    LoggingCount mixCount, int hashX, int hashY, byte[] buffer) {
		requireNonNull(accessCount, "accessCount == null");
		requireNonNull(secureRandom, "secureRandom == null");
		requireNonNull(messageDigest, "messageDigest == null");
		requireNonNull(cipher, "cipher == null");
		requireThat(injectedEntropy.get() >= 0, "injectedEntropy.value < 0: " + injectedEntropy.get());
		requireThat(extractedEntropy.get() >= 0, "extractedEntropy.value < 0: " + extractedEntropy.get());
		requireNonNull(mixCount, "mixCount == null");
		requireThat(hashX >= 0, "hashX < 0: " + hashX);
		requireThat(hashX < buffer.length, "hashX not in range [0,buffer.length): " + hashX + " >= " + buffer.length);
		requireThat(hashY >= 0, "hashY < 0: " + hashY);
		requireThat(hashY < buffer.length, "hashY not in range [0,buffer.length): " + hashY + " >= " + buffer.length);
		requireThat(buffer.length >= messageDigest.get().getDigestLength(),
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
		requireThat(poolFile.exists(), "poolFile.exists() == false: " + poolFile);
		requireThat(poolFile.canRead(), "poolFile.canRead() == false: " + poolFile);

		EntropyPool pool = EntropyPoolLoader.loadFromFile(poolFile);

		if (!(pool instanceof EntropyPool2))
			throw new IllegalArgumentException("File is not version 2.0. You can use" +
			                                   " EntropyPoolLoader.loadPoolFromFile() to load any file version.");

		return (EntropyPool2) pool;
	}

	public void saveToFile(File poolFile, File bakFile, File tempFile) throws IOException {
		requireNonNull(poolFile, "poolFile == null");
		requireNonNull(bakFile, "bakFile == null");
		requireNonNull(tempFile, "tempFile == null");

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
		requireThat(bytes.length > 0, "bytes.length == 0");

		for (byte b : bytes) {
			buffer[writePointer++] ^= b;

			if (writePointer == buffer.length) {
				writePointer = 0;
				mix();
			}
		}

		injectedEntropy.set(Math.min(injectedEntropy.get() + entropyBits, buffer.length * 8));
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

		extractedEntropy.set(Math.addExact(extractedEntropy.get(), numBytes * 8));

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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append("[");
		sb.append("size=").append(buffer.length);
		sb.append(", availableEntropy=").append(getAvailableEntropy());
		return sb.toString();
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

