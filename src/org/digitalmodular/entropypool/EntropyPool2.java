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
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import org.digitalmodular.utilities.LogTimer;
import org.digitalmodular.utilities.SecureRandomFactory;
import org.digitalmodular.utilities.Verifier;
import org.digitalmodular.utilities.container.LoggingCount;
import org.digitalmodular.utilities.container.LoggingLong;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-25
public class EntropyPool2 implements EntropyPool {
	// The numbers that are 'most coprime' to 64 are 19 and 45. (Hexacontatetragram{64/19})
	// Subtracting 64s until total file size < 64kiB.
	public static final int DEFAULT_ENTROPY_POOL_BYTE_LENGTH = 65536 - 64 * 3 - 45;

	public static final String DEFAULT_SECURERANDOM_STRING  = "SP800CTR/AES/256/Strong/16777216";
	public static final String DEFAULT_MESSAGEDIGEST_STRING = "Keccak-512";
	public static final String DEFAULT_CIPHER_STRING        = "Threefish-1024/EAX/NoPadding";

	private static final Logger LOGGER = Logger.getLogger(EntropyPool2.class.getName());

	private final SecureRandom  secureRandom;
	private final MessageDigest messageDigest;
	private final Cipher        cipher;

	private final byte[] buffer;

	private final long createdDate;

	private final LoggingCount mixCount;
	private final LoggingLong  injectedEntropy;
	private final LoggingLong  extractedEntropy;

	private int hashX;
	private int hashY;

	private int writePointer;

	private final EntropyPoolMixer mixer = new MultipleMixer(
			new WhitenMixer(),
			new PermuteMixer(),
			new RehashMixer());

	public EntropyPool2(int newSize) throws NoSuchAlgorithmException, NoSuchPaddingException {
		Verifier.requireThat(newSize > 0, "newSize <= 0: " + newSize);

		secureRandom = SecureRandomFactory.getInstance(DEFAULT_SECURERANDOM_STRING);
		messageDigest = MessageDigest.getInstance(DEFAULT_MESSAGEDIGEST_STRING);
		cipher = Cipher.getInstance(DEFAULT_CIPHER_STRING);
		buffer = new byte[newSize];
		createdDate = System.currentTimeMillis();
		mixCount = new LoggingCount();
		injectedEntropy = new LoggingLong(0);
		extractedEntropy = new LoggingLong(0);

		hashX = 0;
		hashY = 0;

		writePointer = 0;
	}

	public EntropyPool2(SecureRandom secureRandom, MessageDigest messageDigest, Cipher cipher,
	                    byte[] buffer, long createdDate, LoggingCount mixCount,
	                    LoggingLong injectedEntropy, LoggingLong extractedEntropy,

	                    int hashX, int hashY) {
		Objects.requireNonNull(secureRandom, "secureRandom == null");
		Objects.requireNonNull(messageDigest,
		                       "messageDigest == null");
		Objects.requireNonNull(cipher,
		                       "cipher == null");
		Objects.requireNonNull(buffer,
		                       "buffer == null");
		Verifier.requireThat(buffer.length >= messageDigest.getDigestLength(),
		                     "buffer.length < messageDigest.digestLength: " +
		                     buffer.length + " < " + messageDigest.getDigestLength());
		Verifier.requireThat(createdDate >= 0,
		                     "createdDate <= 0: " +
		                     createdDate);
		Objects.requireNonNull(mixCount,
		                       "mixCount == null");
		Verifier.requireThat(mixCount.getModifyDate() == 0 ||
		                     mixCount.getModifyDate() >= createdDate,
		                     "mixCount.modifyDate < createdDate: " +
		                     mixCount.getModifyDate() + " < " + createdDate);
		Objects.requireNonNull(injectedEntropy,
		                       "injectedEntropy == null");
		Verifier.requireThat(injectedEntropy.getValue() <= buffer.length * 8,
		                     "injectedEntropy.value > buffer.length * 8: " +
		                     injectedEntropy + " > " + buffer.length * 8);
		Verifier.requireThat(injectedEntropy.getModifyDate() == 0 ||
		                     injectedEntropy.getModifyDate() >= createdDate,
		                     "injectedEntropy.modifyDate < createdDate: " +
		                     injectedEntropy.getModifyDate() + " < " + createdDate);
		Objects.requireNonNull(extractedEntropy,
		                       "extractedEntropy == null");
		Verifier.requireThat(extractedEntropy.getValue() <= buffer.length * 8,
		                     "extractedEntropy.value > buffer.length * 8: " +
		                     extractedEntropy + " > " + buffer.length * 8);
		Verifier.requireThat(extractedEntropy.getModifyDate() == 0 ||
		                     extractedEntropy.getModifyDate() >= createdDate,
		                     "extractedEntropy.modifyDate < createdDate: " +
		                     extractedEntropy.getModifyDate() + " < " + createdDate);
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

		this.secureRandom = secureRandom;
		this.messageDigest = messageDigest;
		this.cipher = cipher;
		this.buffer = buffer;
		this.mixCount = mixCount;
		this.createdDate = createdDate;
		this.injectedEntropy = injectedEntropy;
		this.extractedEntropy = extractedEntropy;

		this.hashX = hashX;
		this.hashY = hashY;

		this.writePointer = 0;
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
			                                   " EntropyPoolLoader.loadPoolFromFile() to load any version.");

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

	public long getInjectedEntropy()  { return injectedEntropy.getValue(); }

	public long getExtractedEntropy() { return extractedEntropy.getValue(); }

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

		injectedEntropy.set(Math.min(injectedEntropy.getValue() + entropyBits, buffer.length * 8));
	}

	@Override
	public byte[] extractEntropy(int numBytes) {
		if (numBytes * 8 > getAvailableEntropy())
			throw new IllegalStateException(
					"Not enough entropy available: " + numBytes * 8 + " > " + getAvailableEntropy());

		if (writePointer > 0)
			mix();

		byte[] bytes = new byte[numBytes];
		for (int i = 0; i < buffer.length; i++)
			bytes[i % numBytes] ^= buffer[i];

		mix();

		extractedEntropy.add(numBytes * 8);

		return bytes;
	}

	@Override
	public void mix() {
		LogTimer.start();

		mixer.mix(this);

		mixCount.increment();
		writePointer = 0;

		LogTimer.finishAndLog(LOGGER, "Mixed the Entropy Pool in {0} seconds");
	}

	SecureRandom secureRandom()    { return secureRandom; }

	MessageDigest messageDigest()  { return messageDigest; }

	Cipher cipher()                { return cipher; }

	byte[] buffer()                { return buffer; }

	long createdDate()             { return createdDate; }

	LoggingCount mixCount()        { return mixCount; }

	LoggingLong injectedEntropy()  { return injectedEntropy; }

	LoggingLong extractedEntropy() { return extractedEntropy; }

	int hashX()                    { return hashX; }

	void hashX(int hashX)          { this.hashX = hashX; }

	int hashY()                    { return hashY; }

	void hashY(int hashY)          { this.hashY = hashY; }
}
