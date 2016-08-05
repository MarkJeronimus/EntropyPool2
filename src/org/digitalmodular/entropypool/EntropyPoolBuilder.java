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
import java.security.SecureRandom;
import javax.crypto.Cipher;
import org.digitalmodular.utilities.container.LoggingCount;
import org.digitalmodular.utilities.container.LoggingVariable;
import org.digitalmodular.utilities.container.Version;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-27
public class EntropyPoolBuilder {
	private Version version = null;

	private Long         createdDate = null;
	private LoggingCount accessCount = null;

	private LoggingVariable<SecureRandom>  secureRandom  = null;
	private LoggingVariable<MessageDigest> messageDigest = null;
	private LoggingVariable<Cipher>        cipher        = null;

	private LoggingVariable<Long> injectedEntropy  = null;
	private LoggingVariable<Long> extractedEntropy = null;
	private LoggingCount          mixCount         = null;

	private Integer hashX = null;
	private Integer hashY = null;

	private byte[] buffer = null;

	public void setVersion(Version version) {
		this.version = version;
	}

	public void setSecureRandom(String secureRandomAlgorithm) {

	}

	public void setMessageDigest(String messageDigestAlgorithm) {

	}

	public void setCipher(String cipherAlgorithm) {

	}

	public void setCreatedDate(long createdDate) {
		this.createdDate = createdDate;
	}

	public void setAccessCount(LoggingCount accessCount) {
		this.accessCount = accessCount;
	}

	public void setMixCount(LoggingCount mixCount) {
		this.mixCount = mixCount;
	}

	public void setInjectedEntropy(LoggingVariable<Long> injectedEntropy) {
		this.injectedEntropy = injectedEntropy;
	}

	public void setExtractedEntropy(LoggingVariable<Long> extractedEntropy) {
		this.extractedEntropy = extractedEntropy;
	}

	public void setHashXY(int hashX, int hashY) {
		this.hashX = hashX;
		this.hashY = hashY;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public EntropyPool2 build() {
		checkForErrors();

		EntropyPool2 pool = new EntropyPool2(createdDate, accessCount, secureRandom, messageDigest, cipher,
		                                     injectedEntropy, extractedEntropy, mixCount, hashX, hashY, buffer);
		return pool;
	}

	private void checkForErrors() {
		if (version == null)
			throw new IllegalStateException("'version' is required for all file versions");
		if (buffer == null)
			throw new IllegalStateException("'buffer' is required for all file versions");

		int majorVersion = version.getMajor();
		if (majorVersion < 2) {
			throw new IllegalStateException("File versions < 2 are not supported: " + version);
		} else if (majorVersion == 2) {
			checkVersion2ForErrors();
		} else if (majorVersion > 2) {
			throw new IllegalStateException("File versions > 2 are not supported: " + version);
		}
	}

	private void checkVersion2ForErrors() {
		if (secureRandom == null)
			throw new IllegalStateException("'secureRandom' is required for file version 2");
		if (messageDigest == null)
			throw new IllegalStateException("'messageDigest' is required for file version 2");
		if (cipher == null)
			throw new IllegalStateException("'cipher' is required for file version 2");
		if (createdDate == null)
			throw new IllegalStateException("'createdDate' is required for file version 2");
		if (accessCount == null)
			throw new IllegalStateException("'access' is required for file version 2");
		if (mixCount == null)
			throw new IllegalStateException("'mixCount' is required for file version 2");
		if (injectedEntropy == null)
			throw new IllegalStateException("'injectedEntropy' is required for file version 2");
		if (extractedEntropy == null)
			throw new IllegalStateException("'extractedEntropy' is required for file version 2");
		if (hashX == null)
			throw new IllegalStateException("'hashX' and 'hashY' are required for file version 2");
	}
}
