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

import java.security.MessageDigest;
import java.util.logging.Level;

import org.digitalmodular.utilities.LogTimer;
import static org.digitalmodular.utilities.container.MessageDigestUtilities.*;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-08-02
public class RehashMixer implements EntropyPoolMixer {
	private byte[] digestBuffer;

	@Override
	public void mix(EntropyPool2 pool) {
		LogTimer.start();

		MessageDigest digest = pool.getMessageDigest();
		byte[]        buffer = pool.buffer();

		int hashX = pool.hashX();
		int hashY = pool.hashX();

		int digestSize = digest.getDigestLength();

		if (digestBuffer == null || digestBuffer.length != digestSize)
			digestBuffer = new byte[digestSize];

		int numHashes = (buffer.length + digestSize - 1) / digestSize;
		for (int i = 0; i < numHashes; i++) {
			digest.reset();

			hashLong(digest, System.currentTimeMillis());
			hashLong(digest, System.nanoTime());
			hashInt(digest, Runtime.getRuntime().availableProcessors());
			hashLong(digest, Runtime.getRuntime().freeMemory());
			hashLong(digest, Runtime.getRuntime().maxMemory());
			hashLong(digest, Runtime.getRuntime().totalMemory());

			hashLong(digest, pool.getCreateDate());
			hashLoggingCount(digest, pool.accessCount());
			hashLoggingReference(digest, pool.secureRandom());
			hashLoggingReference(digest, pool.messageDigest());
			hashLoggingReference(digest, pool.cipher());
			hashLoggingReference(digest, pool.injectedEntropy());
			hashLoggingReference(digest, pool.extractedEntropy());
			hashLoggingCount(digest, pool.mixCount());
			hashInt(digest, hashX);
			hashInt(digest, hashY);
			hashInt(digest, buffer.length);

			hashBlockFromBuffer(digest, buffer, hashX);
			hashBlockFromBuffer(digest, buffer, hashY);

			getDigestToBuffer(digest, buffer, hashX, digestBuffer);

			int hi = buffer[hashX] & 0xFF;
			int lo = buffer[(hashX + 1) % buffer.length] & 0xFF;
			hashY = (hashY + (hi << 8) + lo) % buffer.length;
			hashX = (hashX + digestSize) % buffer.length;
		}

		pool.hashX(hashX);
		pool.hashY(hashY);

		LogTimer.finishAndLog(Level.FINER, "Rehashed the Entropy Pool in {0} seconds");
	}
}
