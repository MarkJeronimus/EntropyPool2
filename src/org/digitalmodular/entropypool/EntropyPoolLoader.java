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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.digitalmodular.entropypool.EntropyPoolUtilities.*;
import org.digitalmodular.utilities.Verifyer;
import org.digitalmodular.utilities.Version;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-25
public enum EntropyPoolLoader {
	;

	public static EntropyPool loadPoolFromFile(File file) throws IOException {
		Objects.requireNonNull(file);
		Verifyer.requireThat(file.exists(), "file.exists() = false");
		Verifyer.requireThat(file.isFile(), "file.isFile() = false: " + file);
		Verifyer.requireThat(file.canRead(), "file.canRead() = false: " + file);

		try (InputStream reader = new BufferedInputStream(new FileInputStream(file))) {
			return loadPoolFromInputStream(reader);
		}
	}

	@SuppressWarnings("UnusedAssignment")
	public static EntropyPool loadPoolFromInputStream(InputStream inputStream) throws IOException {
		Objects.requireNonNull(inputStream);
		Verifyer.requireThat(inputStream.available() > 0, "inputStream.available() <= 0: " + inputStream.available());
		Verifyer.requireThat(inputStream.available() < Integer.MAX_VALUE - 1,
		                     "inputStream.available() >= Integer.MAX_VALUE - 1: " + inputStream.available());

		long t = System.nanoTime();

		DataInput dataInput = inputStream instanceof DataInput
		                      ? (DataInput) inputStream
		                      : new DataInputStream(inputStream);

		EntropyPoolBuilder builder = new EntropyPoolBuilder();

		while (true) {
			if (inputStream.available() < 4) break;
			String tag = readTag(dataInput);

			switch (tag) {
				case MAGIC_TAG:
					String magic = readPaddedString(tag, dataInput);

					builder.setMagic(magic);
					break;
				case COMMENT_TAG:
					String info = readPaddedString(tag, dataInput);
					break;
				case VERSION_TAG:
					int[] version = readPaddedInts(tag, dataInput, 4);

					builder.setVersion(new Version(version[0], version[1], version[2], Version.Release.of(version[3])));
					break;
				case POOL_TAG:
					int poolSize = readPaddedInts(tag, dataInput, 1)[0];

					builder.setPool(poolSize);

					dataInput.readFully(builder.getPoolBuffer());
					break;
				case SECURERANDOM_TAG:
					String secureRandom = readPaddedString(tag, dataInput);

					builder.setSecureRandom(secureRandom);
					break;
				case MESSAGEDIGEST_TAG:
					String digest = readPaddedString(tag, dataInput);

					builder.setMessageDigest(digest);
					break;
				case CIPHER_TAG:
					String cipher = readPaddedString(tag, dataInput);

					builder.setCipher(cipher);
					break;
				case ENTROPY_TAG:
					long[] entropy = readPaddedLongs(tag, dataInput, 3);

					builder.setEntropy(entropy);
					break;
				case HASH_TAG:
					int[] hash = readPaddedInts(tag, dataInput, 2);

					builder.setHash(hash);
					break;
				case COUNT_TAG:
					int[] count = readPaddedInts(tag, dataInput, 3);

					builder.setCount(count);
					break;
				case DATE_TAG:
					long[] date = readPaddedLongs(tag, dataInput, 4);

					builder.setDate(date);
					break;
				default:
					Logger.getLogger(EntropyPoolLoader.class.getName()).warning("Unknown tag encountered: " + tag);
			}

			if (builder.isComplete()) break;
		}

		if (inputStream.available() > 0)
			Logger.getLogger(EntropyPoolLoader.class.getName())
			      .warning(inputStream.available() + " extraneous byte(s) detected");

		EntropyPool2 pool = builder.buildEntropyPool();

		t = System.nanoTime() - t;

		Logger.getLogger(EntropyPoolLoader.class.getName())
		      .log(Level.INFO, "Loaded the entropy pool in {0} seconds", t / 1e9);

		return pool;
	}

	private static String readTag(DataInput dataInput) throws IOException {
		byte[] tagBytes = new byte[TAG_LENGTH];

		dataInput.readFully(tagBytes);

		String tag = new String(tagBytes, Charset.forName("UTF-8"));
		tag = tag.trim();
		return tag;
	}

	private static String readPaddedString(String tag, DataInput dataInput) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(32);

		while (true) {
			byte ch = dataInput.readByte();
			if (ch == '\0') break;

			out.write(ch);
		}

		unpad(dataInput, TAG_LENGTH + out.size() + 1, 0x10);

		String string = out.toString("UTF-8");

		Logger.getLogger(EntropyPoolLoader.class.getName()).info(tag + " tag: " + string);

		return string;
	}

	private static int[] readPaddedInts(String tag, DataInput dataInput, int numInts) throws IOException {
		int[] ints = new int[numInts];

		for (int i = 0; i < numInts; i++) ints[i] = dataInput.readInt();

		unpad(dataInput, TAG_LENGTH + numInts * 4, 0x10);

		Logger.getLogger(EntropyPoolLoader.class.getName()).info(tag + " tag: " + Arrays.toString(ints));

		return ints;
	}

	private static long[] readPaddedLongs(String tag, DataInput dataInput, int numLongs) throws IOException {
		long[] longs = new long[numLongs];

		for (int i = 0; i < numLongs; i++) longs[i] = dataInput.readLong();

		unpad(dataInput, TAG_LENGTH + numLongs * 8, 0x10);

		Logger.getLogger(EntropyPoolLoader.class.getName()).info(tag + " tag: " + Arrays.toString(longs));

		return longs;
	}

	private static String readString(DataInput dataInput) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(32);

		while (true) {
			byte ch = dataInput.readByte();
			if (ch == '\0') break;

			out.write(ch);
		}

		return out.toString("UTF-8");
	}

	private static void unpad(DataInput dataInput, int readLength, int padModulo) throws IOException {
		int padLength = calculatePadLength(readLength, padModulo);

		dataInput.skipBytes(padLength);
	}
}
