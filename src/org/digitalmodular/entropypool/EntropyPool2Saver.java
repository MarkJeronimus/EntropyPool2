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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.digitalmodular.entropypool.EntropyPoolUtilities.*;
import org.digitalmodular.utilities.Verifyer;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-25
public enum EntropyPool2Saver {
	;

	public static void savePoolToFile(EntropyPool2 pool, File file) throws IOException {
		Objects.requireNonNull(pool, "pool = null");
		Objects.requireNonNull(file);
		pool.checkAlive();
		Verifyer.requireThat(!file.exists() || file.isFile(), "file.isFile() = false: " + file);
		Verifyer.requireThat(!file.exists() || file.canWrite(), "file.canWrite() = false: " + file);

		try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
			savePoolToOutputStream(pool, outputStream);
		}
	}

	public static void savePoolToOutputStream(EntropyPool2 pool, OutputStream outputStream) throws IOException {
		Objects.requireNonNull(pool, "pool = null");
		Objects.requireNonNull(outputStream);
		pool.checkAlive();

		long t = System.nanoTime();

		DataOutputStream dataOutput = outputStream instanceof DataOutputStream
		                              ? (DataOutputStream) outputStream
		                              : new DataOutputStream(outputStream);

		writeHeader(pool, dataOutput);

		dataOutput.write(pool.getBuffer());

		t = System.nanoTime() - t;

		Logger.getLogger(EntropyPool2Saver.class.getName())
		      .log(Level.INFO, "Saved the entropy pool in {0} seconds", t / 1e9);
	}

	private static void writeHeader(EntropyPool2 pool, DataOutputStream dataOutput) throws IOException {
		writePaddedString(dataOutput, MAGIC_TAG, "EntropyPool v2.0 Alpha © 2016 DigitalModular");
		writePaddedInts(dataOutput, VERSION_TAG, CURRENT_VERSION.getMajor(), CURRENT_VERSION.getMinor(),
		                CURRENT_VERSION.getRevision(), CURRENT_VERSION.getRelease().getValue());
		writePaddedString(dataOutput, SECURERANDOM_TAG, pool.getSecureRandom().getAlgorithm());
		writePaddedString(dataOutput, MESSAGEDIGEST_TAG, pool.getMessageDigest().getAlgorithm());
		writePaddedString(dataOutput, CIPHER_TAG, pool.getCipher().getAlgorithm());
		writePaddedLongs(dataOutput, ENTROPY_TAG, pool.getEntropyRemaining(), pool.getEntropyInjected(),
		                 pool.getSecureRandomBitsUsed());
		writePaddedInts(dataOutput, HASH_TAG, pool.getHashX(), pool.getHashY());
		writePaddedInts(dataOutput, COUNT_TAG, pool.getMixCount(), pool.getEntropyInjectedCount(),
		                pool.getEntropyExtractedCount());
		writePaddedLongs(dataOutput, DATE_TAG, pool.getCreatedDate(), pool.getLastMixDate(),
		                 pool.getLastEntropyInjectedDate(), pool.getLastEntropyExtractedDate());
		writePaddedInts(dataOutput, POOL_TAG, pool.getBuffer().length);
	}

	private static void writePaddedString(DataOutputStream dataOutput, String tag, String string)
			throws IOException {
		writeTag(dataOutput, tag);

		if (string != null) {
			dataOutput.write(string.getBytes());
			dataOutput.writeByte(0);
		}

		pad(dataOutput, 0x10, (byte) 0);
	}

	private static void writePaddedInts(DataOutputStream dataOutput, String tag, int... ints)
			throws IOException {
		writeTag(dataOutput, tag);

		for (int i : ints)
			dataOutput.writeInt(i);

		pad(dataOutput, 0x10, (byte) 0);
	}

	private static void writePaddedLongs(DataOutputStream dataOutput, String tag, long... ints)
			throws IOException {
		writeTag(dataOutput, tag);

		for (long i : ints)
			dataOutput.writeLong(i);

		pad(dataOutput, 0x10, (byte) 0);
	}

	private static void writeTag(DataOutputStream dataOutput, String tag) throws IOException {
		dataOutput.write(tag.getBytes());
		pad(dataOutput, TAG_LENGTH, (byte) ' ');
	}

	private static void pad(DataOutputStream dataOutput, int padModulo, byte byteToWadWith) throws IOException {
		int writeSize = dataOutput.size();
		int padLength = calculatePadLength(writeSize, padModulo);

		for (int i = 0; i < padLength; i++) dataOutput.writeByte(byteToWadWith);
	}
}
