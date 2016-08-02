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
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import static org.digitalmodular.entropypool.EntropyPoolUtilities.getLengthAfterWrap;
import static org.digitalmodular.entropypool.EntropyPoolUtilities.getLengthBeforeWrap;
import org.digitalmodular.utilities.MoreSecureRandom;
import org.digitalmodular.utilities.Verifyer;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-25
public class EntropyPool2 implements EntropyPool {
	private byte[] buffer;

	private final SecureRandom  secureRandom;
	private final MessageDigest messageDigest;
	private final Cipher        cipher;
	private       byte[]        digestBuffer;

	private long entropyRemaining;
	private long entropyInjected;
	private long secureRandomBitsUsed;

	private int hashX; // Linear pointer
	private int hashY; // Random pointer

	private int mixCount;
	private int entropyInjectedCount;
	private int entropyExtractedCount;

	private long createdDate;
	private long lastMixDate;
	private long lastInjectedDate;
	private long lastExtractedDate;

	private int writePointer;

	public EntropyPool2(int newSize) throws NoSuchAlgorithmException, NoSuchPaddingException {
		Verifyer.requireThat(newSize > 0, "newSize <= 0: " + newSize);

		buffer = new byte[newSize];
		secureRandom = MoreSecureRandom.getInstance(DEFAULT_SECURERANDOM_STRING);
		messageDigest = MessageDigest.getInstance(DEFAULT_MESSAGEDIGEST_STRING);
		cipher = Cipher.getInstance(DEFAULT_CIPHER_STRING);
		digestBuffer = new byte[messageDigest.getDigestLength()];
		entropyRemaining = 0;
		entropyInjected = 0;
		secureRandomBitsUsed = 0;
		hashX = 0;
		hashY = 0;
		mixCount = 0;
		writePointer = 0;
		entropyInjectedCount = 0;
		entropyExtractedCount = 0;
		createdDate = System.currentTimeMillis();
		lastMixDate = 0;
		lastInjectedDate = 0;
		lastExtractedDate = 0;
	}

	public EntropyPool2(byte[] buffer,
	                    SecureRandom secureRandom, MessageDigest messageDigest, Cipher cipher,
	                    long entropyRemaining, long entropyInjected, long secureRandomBitsUsed,
	                    int hashX, int hashY,
	                    int mixCount, int entropyInjectedCount, int entropyExtractedCount,
	                    long createdDate, long lastMixDate, long lastInjectedDate,
	                    long lastExtractedDate) {
		Objects.requireNonNull(buffer, "buffer = null");
		Objects.requireNonNull(secureRandom, "secureRandom = null");
		Objects.requireNonNull(messageDigest, "messageDigest = null");
		Objects.requireNonNull(cipher, "cipher = null");
		Verifyer.requireThat(entropyRemaining >= 0,
		                     "entropyRemaining < 0: " +
		                     entropyRemaining);
		Verifyer.requireThat(entropyRemaining <= buffer.length * 8,
		                     "entropyRemaining > buffer.length * 8: " +
		                     entropyRemaining + " > " + buffer.length * 8);
		Verifyer.requireThat(entropyInjected >= 0,
		                     "entropyInjected < 0: " +
		                     entropyInjected);
		Verifyer.requireThat(secureRandomBitsUsed >= 0,
		                     "secureRandomBitsUsed < 0: " +
		                     secureRandomBitsUsed);
		Verifyer.requireThat(hashX >= 0,
		                     "hash < 0: " +
		                     hashX);
		Verifyer.requireThat(hashX < buffer.length,
		                     "hash >= buffer.length: " +
		                     hashX + " >= " + buffer.length);
		Verifyer.requireThat(hashY >= 0,
		                     "hash < 0: " +
		                     hashY);
		Verifyer.requireThat(hashY < buffer.length,
		                     "hash >= buffer.length: " +
		                     hashY + " >= " + buffer.length);
		Verifyer.requireThat(mixCount >= 0,
		                     "mixCount < 0: " +
		                     mixCount);
		Verifyer.requireThat(entropyInjectedCount >= 0,
		                     "entropyAddedCount < 0: " +
		                     entropyInjectedCount);
		Verifyer.requireThat(entropyExtractedCount >= 0,
		                     "entropyExtractedCount < 0: " +
		                     entropyExtractedCount);
		Verifyer.requireThat(createdDate >= 0,
		                     "createdDate < 0: " +
		                     Instant.ofEpochMilli(createdDate));
		Verifyer.requireThat(lastMixDate == 0 || lastMixDate >= createdDate,
		                     "lastMixDate != 0 and < createdDate: " +
		                     Instant.ofEpochMilli(lastMixDate) + " < " + Instant.ofEpochMilli(createdDate));
		Verifyer.requireThat(lastInjectedDate == 0 || lastInjectedDate >= createdDate,
		                     "lastInjectedDate != 0 and < createdDate: " +
		                     Instant.ofEpochMilli(lastInjectedDate) + " < " + Instant.ofEpochMilli(createdDate));
		Verifyer.requireThat(lastExtractedDate == 0 || lastExtractedDate >= createdDate,
		                     "lastExtractedDate != 0 and < createdDate: " +
		                     Instant.ofEpochMilli(lastExtractedDate) + " < " + Instant.ofEpochMilli(createdDate));

		this.buffer = buffer;
		this.secureRandom = secureRandom;
		this.messageDigest = messageDigest;
		this.cipher = cipher;
		digestBuffer = new byte[messageDigest.getDigestLength()];
		this.entropyRemaining = entropyRemaining;
		this.entropyInjected = entropyInjected;
		this.secureRandomBitsUsed = secureRandomBitsUsed;
		this.hashX = hashX;
		this.hashY = hashY;
		this.mixCount = mixCount;
		this.entropyInjectedCount = entropyInjectedCount;
		this.entropyExtractedCount = entropyExtractedCount;
		this.writePointer = 0;
		this.createdDate = createdDate;
		this.lastMixDate = lastMixDate;
		this.lastInjectedDate = lastInjectedDate;
		this.lastExtractedDate = lastExtractedDate;
	}

	public static EntropyPool2 newInstance() throws NoSuchAlgorithmException, NoSuchPaddingException {
		return new EntropyPool2(DEFAULT_ENTROPY_POOL_BYTE_LENGTH);
	}

	public static EntropyPool2 loadFromFile(File poolFile) throws IOException {
		Objects.requireNonNull(poolFile, "poolFile = null");
		Verifyer.requireThat(poolFile.exists(), "poolFile.exists() = false: " + poolFile);
		Verifyer.requireThat(poolFile.canRead(), "poolFile.canRead() = false: " + poolFile);

		EntropyPool pool = EntropyPoolLoader.loadFromFile(poolFile);

		if (!(pool instanceof EntropyPool2))
			throw new IllegalArgumentException("File is not version 2.0. You can use" +
			                                   " EntropyPoolLoader.loadPoolFromFile() to load any version.");

		return (EntropyPool2) pool;
	}

	public void saveToFile(File poolFile, File bakFile, File tempFile) throws IOException {
		Objects.requireNonNull(poolFile, "poolFile = null");
		Objects.requireNonNull(bakFile, "bakFile = null");
		Objects.requireNonNull(tempFile, "tempFile = null");
		checkAlive();

		EntropyPool2Saver.saveToFile(this, tempFile);

		if (poolFile.exists())
			Files.move(poolFile.toPath(), bakFile.toPath(), StandardCopyOption.ATOMIC_MOVE);

		Files.move(tempFile.toPath(), poolFile.toPath(), StandardCopyOption.ATOMIC_MOVE);

		terminate();
	}

	@Override
	public boolean isAlive() {
		return buffer != null;
	}

	@Override
	public void terminate() {
		checkAlive();

		buffer = null;
		digestBuffer = null;
	}

	byte[] getBuffer() {
		checkAlive();

		return buffer;
	}

	public SecureRandom getSecureRandom() {
		checkAlive();

		return secureRandom;
	}

	public MessageDigest getMessageDigest() {
		checkAlive();

		return messageDigest;
	}

	public Cipher getCipher() {
		checkAlive();

		return cipher;
	}

	public long getEntropyRemaining() {
		checkAlive();

		return entropyRemaining;
	}

	public long getEntropyInjected() {
		checkAlive();

		return entropyInjected;
	}

	public long getEntropyExtracted() {
		checkAlive();

		return getEntropyInjected() - getEntropyRemaining();
	}

	public long getSecureRandomBitsUsed() {
		checkAlive();

		return secureRandomBitsUsed;
	}

	int getHashX() {
		checkAlive();

		return hashX;
	}

	int getHashY() {
		checkAlive();

		return hashY;
	}

	public int getMixCount() {
		checkAlive();

		return mixCount;
	}

	public int getEntropyInjectedCount() {
		checkAlive();

		return entropyInjectedCount;
	}

	public int getEntropyExtractedCount() {
		checkAlive();

		return entropyExtractedCount;
	}

	public long getCreatedDate() {
		checkAlive();

		return createdDate;
	}

	public long getLastMixDate() {
		checkAlive();

		return lastMixDate;
	}

	public long getLastInjectedDate() {
		checkAlive();
		return lastInjectedDate;
	}

	public long getLastExtractedDate() {
		checkAlive();
		return lastExtractedDate;
	}

	@Override
	public void injectEntropy(byte[] bytes, int entropyBits) {
		Objects.requireNonNull(bytes, "bytes = null");
		Verifyer.requireThat(bytes.length > 0, "bytes.length = 0");
		checkAlive();

		for (byte b : bytes) {
			buffer[writePointer++] ^= b;

			if (writePointer == buffer.length) {
				writePointer = 0;
				mix();
			}
		}

		mix();

		entropyRemaining = Math.min(buffer.length * 8, entropyRemaining + entropyBits);
		entropyInjected += entropyBits;
		entropyInjectedCount++;
		lastInjectedDate = System.currentTimeMillis();
	}

	@Override
	public byte[] extractEntropy(int numBytes) {
		if (numBytes * 8 > entropyRemaining)
			throw new IllegalStateException("Not enough entropy available: " + numBytes * 8 + " > " + entropyRemaining);

		if (writePointer > 0)
			mix();

		byte[] bytes = new byte[numBytes];
		for (int i = 0; i < buffer.length; i++)
			bytes[i % numBytes] ^= buffer[i];

		mix();

		entropyRemaining -= numBytes * 8;
		entropyExtractedCount++;
		lastExtractedDate = System.currentTimeMillis();
		Verifyer.assertThat(entropyRemaining >= 0, "entropyCurrent < 0");

		return bytes;
	}

	@Override
	public void mix() {
		checkAlive();

		long time = System.nanoTime();

		whiten();
		permute();
		rehash(time);

		mixCount++;
		lastMixDate = System.currentTimeMillis();
		writePointer = 0;

		time = System.nanoTime() - time;

		Logger.getLogger(EntropyPool2.class.getName())
		      .log(Level.INFO, "Mixed the entropy pool in {0} seconds", time / 1e9);
	}

	private void whiten() {
		long t = System.nanoTime();

		for (int i = 0; i < buffer.length; i++)
			buffer[i] ^= (byte) secureRandom.nextInt(0x100);

		secureRandomBitsUsed += buffer.length * 8;
	}

	private void permute() {
		int numRandomBitsUsed = shuffle(buffer, secureRandom);

		secureRandomBitsUsed += numRandomBitsUsed;
	}

	private static int shuffle(byte[] array, Random random) {
		double entropyUsed = 0;

		for (int i = array.length - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			entropyUsed += Math.log(i);

			swap(array, i, j);
		}

		return (int) (entropyUsed / Math.log(2));
	}

	private static void swap(byte[] array, int i, int j) {
		if (i == j) return;

		byte temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	private void rehash(long time) {
		int digestSize = messageDigest.getDigestLength();

		int numIterations = (buffer.length + digestSize - 1) / digestSize;

		for (int i = 0; i < numIterations; i++) {
			messageDigest.reset();
			hashString(secureRandom.getAlgorithm());
			hashString(messageDigest.getAlgorithm());
			hashString(cipher.getAlgorithm());
			hashLong(entropyRemaining);
			hashLong(entropyInjected);
			hashLong(secureRandomBitsUsed);
			hashInt(hashX);
			hashInt(hashY);
			hashInt(mixCount);
			hashInt(writePointer);
			hashLong(createdDate);
			hashLong(lastMixDate);
			hashLong(lastInjectedDate);
			hashLong(lastExtractedDate);
			hashLong(time);
			hashBlockFromBuffer(hashX, digestSize);
			hashBlockFromBuffer(hashY, digestSize);
			getDigestToBuffer(hashX, digestSize);

			int hi = buffer[hashX] & 0xFF;
			int lo = buffer[(hashX + 1) % buffer.length] & 0xFF;
			hashY = (hashY + (hi << 8) + lo) % buffer.length;
			hashX = (hashX + digestSize) % buffer.length;
		}
	}

	private void hashString(String algorithm) {
		try {
			messageDigest.update(algorithm.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			throw new InternalError("This shouldn't happen", ex);
		}
	}

	private void hashInt(int value) {
		messageDigest.update((byte) (value >> 24));
		messageDigest.update((byte) (value >> 16));
		messageDigest.update((byte) (value >> 8));
		messageDigest.update((byte) value);
	}

	private void hashLong(long value) {
		messageDigest.update((byte) (value >> 56));
		messageDigest.update((byte) (value >> 48));
		messageDigest.update((byte) (value >> 40));
		messageDigest.update((byte) (value >> 32));
		messageDigest.update((byte) (value >> 24));
		messageDigest.update((byte) (value >> 16));
		messageDigest.update((byte) (value >> 8));
		messageDigest.update((byte) value);
	}

	private void hashBlockFromBuffer(int byteOffset, int digestSize) {
		int lengthBeforeWrap = getLengthBeforeWrap(buffer.length, byteOffset, digestSize);
		int lengthAfterWrap  = getLengthAfterWrap(buffer.length, byteOffset, digestSize);

		messageDigest.update(buffer, byteOffset, lengthBeforeWrap);
		messageDigest.update(buffer, 0, lengthAfterWrap);
	}

	private void getDigestToBuffer(int byteOffset, int digestSize) {
		try {
			messageDigest.digest(digestBuffer, 0, digestSize);

			int lengthBeforeWrap = getLengthBeforeWrap(buffer.length, byteOffset, digestSize);
			int lengthAfterWrap  = getLengthAfterWrap(buffer.length, byteOffset, digestSize);

			System.arraycopy(digestBuffer, 0, buffer, byteOffset, lengthBeforeWrap);
			System.arraycopy(digestBuffer, lengthBeforeWrap, buffer, 0, lengthAfterWrap);

			Arrays.fill(digestBuffer, (byte) 0);
		} catch (DigestException ex) {
			throw new InternalError("This shouldn't happen", ex);
		}
	}
}
