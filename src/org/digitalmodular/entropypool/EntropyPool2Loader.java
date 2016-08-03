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

import java.io.DataInput;
import java.io.IOException;
import java.time.Instant;
import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;
import static org.digitalmodular.utilities.container.DataIO.readLoggingCount;
import static org.digitalmodular.utilities.container.DataIO.readLoggingLong;
import org.digitalmodular.utilities.container.LoggingCount;
import org.digitalmodular.utilities.container.LoggingLong;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-08-03
public enum EntropyPool2Loader {
	;

	static EntropyPool2 readPool(DataInput in) throws IOException {
		String secureRandom  = in.readUTF();
		String messageDigest = in.readUTF();
		String cipher        = in.readUTF();

		long         createdDate      = in.readLong();
		LoggingCount mixCount         = readLoggingCount(in);
		LoggingLong  injectedEntropy  = readLoggingLong(in);
		LoggingLong  extractedEntropy = readLoggingLong(in);

		int hashX = in.readInt();
		int hashY = in.readInt();

		int bufferLength = in.readInt();

		byte[] buffer = new byte[bufferLength];
		in.readFully(buffer);

		LOGGER.info("createdDate: " + Instant.ofEpochMilli(createdDate));
		LOGGER.info("bufferLength: " + bufferLength);
		LOGGER.info("availableEntropy: " + (injectedEntropy.getValue() - extractedEntropy.getValue()));

		EntropyPoolBuilder builder = new EntropyPoolBuilder();
		builder.setSecureRandom(secureRandom);
		builder.setMessageDigest(messageDigest);
		builder.setCipher(cipher);
		builder.setCreatedDate(createdDate);
		builder.setMixCount(mixCount);
		builder.setInjectedEntropy(injectedEntropy);
		builder.setExtractedEntropy(extractedEntropy);
		builder.setHashXY(hashX, hashY);
		builder.setBuffer(buffer);

		EntropyPool2 pool = builder.buildEntropyPool();
		return pool;
	}
}
