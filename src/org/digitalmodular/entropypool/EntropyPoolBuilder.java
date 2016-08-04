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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import static org.digitalmodular.entropypool.EntropyPool2.*;
import org.digitalmodular.utilities.SecureRandomFactory;
import org.digitalmodular.utilities.container.LoggingCount;
import org.digitalmodular.utilities.container.LoggingLong;
import org.digitalmodular.utilities.container.Version;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-27
public class EntropyPoolBuilder {
	private static final Logger LOGGER = Logger.getLogger(EntropyPoolBuilder.class.getName());

	private Version version = null;

	private SecureRandom  secureRandom  = null;
	private MessageDigest messageDigest = null;
	private Cipher        cipher        = null;

	private Long createdDate = null;

	private LoggingCount mixCount         = null;
	private LoggingLong  injectedEntropy  = null;
	private LoggingLong  extractedEntropy = null;

	private Integer hashX = null;
	private Integer hashY = null;

	private byte[] buffer = null;

	public void setVersion(Version version) { this.version = version; }

	public void setSecureRandom(String secureRandom) {
		try {
			this.secureRandom = SecureRandomFactory.getInstance(secureRandom);
		} catch (NoSuchAlgorithmException ex) {
			LOGGER.log(Level.WARNING, "SecureRandom cannot be instantiated:" + secureRandom +
			                          ". Using default: " + DEFAULT_SECURERANDOM_STRING, ex);
			try {
				this.secureRandom = SecureRandomFactory.getInstance(DEFAULT_SECURERANDOM_STRING);
			} catch (NoSuchAlgorithmException ex2) {
				throw new LinkageError(ex2.getMessage(), ex2);
			}
		}
	}

	public void setMessageDigest(String messageDigest) {
		try {
			this.messageDigest = MessageDigest.getInstance(messageDigest);
		} catch (NoSuchAlgorithmException ex) {
			LOGGER.log(Level.WARNING, "MessageDigest cannot be instantiated: " + messageDigest +
			                          ". Using default: " + DEFAULT_MESSAGEDIGEST_STRING, ex);
			try {
				this.messageDigest = MessageDigest.getInstance(DEFAULT_MESSAGEDIGEST_STRING);
			} catch (NoSuchAlgorithmException ex2) {
				throw new LinkageError(ex2.getMessage(), ex2);
			}
		}
	}

	public void setCipher(String cipher) {
		try {
			this.cipher = Cipher.getInstance(cipher);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
			LOGGER.log(Level.WARNING, "Cipher cannot be instantiated: " + cipher +
			                          ". Using default: " + DEFAULT_CIPHER_STRING, ex);
			try {
				this.cipher = Cipher.getInstance(DEFAULT_CIPHER_STRING);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException ex2) {
				throw new LinkageError(ex2.getMessage(), ex2);
			}
		}
	}

	public void setCreatedDate(long createdDate)                  { this.createdDate = createdDate; }

	public void setMixCount(LoggingCount mixCount)                { this.mixCount = mixCount; }

	public void setInjectedEntropy(LoggingLong injectedEntropy)   { this.injectedEntropy = injectedEntropy; }

	public void setExtractedEntropy(LoggingLong extractedEntropy) { this.extractedEntropy = extractedEntropy; }

	public void setHashXY(int hashX, int hashY) {
		this.hashX = hashX;
		this.hashY = hashY;
	}

	public void setBuffer(byte[] buffer) { this.buffer = buffer; }

	public EntropyPool2 buildEntropyPool() {
		checkForErrors();

		EntropyPool2 pool = new EntropyPool2(secureRandom, messageDigest, cipher,
		                                     buffer, createdDate, mixCount,
		                                     injectedEntropy, extractedEntropy,
		                                     hashX, hashY);
		return pool;
	}

	private void checkForErrors() {
		if (buffer == null)
			throw new IllegalStateException("buffer == null");

		if (version == null) {
			LOGGER
					.warning("version == null");

			return;
		}

		int majorVersion = version.getMajor();

		if (majorVersion < 2) {
			throw new IllegalStateException(
					"version.major < 2: " + version);
		} else if (majorVersion == 2) {
			checkPresenceSecureRandom();
			checkPresenceMessageDigest();
			checkPresenceCipher();
			checkPresenceCreatedDate();
			checkPresenceMixCount();
			checkPresenceInjectedEntropy();
			checkPresenceExtractedEntropy();
			checkPresenceHashXY();
		} else if (majorVersion > 2) {
			throw new IllegalStateException(
					"version.major > 2: " + version);
		}
	}

	private void checkPresenceSecureRandom() {
		if (secureRandom != null) return;

		LOGGER.warning(
				"secureRandom == null. Using default: " + DEFAULT_SECURERANDOM_STRING);
		setSecureRandom(DEFAULT_SECURERANDOM_STRING);
	}

	private void checkPresenceMessageDigest() {
		if (messageDigest != null) return;

		LOGGER.warning(
				"messageDigest == null. Using default: " + DEFAULT_MESSAGEDIGEST_STRING);
		setMessageDigest(DEFAULT_MESSAGEDIGEST_STRING);
	}

	private void checkPresenceCipher() {
		if (cipher != null) return;

		LOGGER.warning(
				"cipher == null. Using default: " + DEFAULT_CIPHER_STRING);
		setCipher(DEFAULT_CIPHER_STRING);
	}

	private void checkPresenceCreatedDate() {
		if (createdDate != null) return;

		LOGGER.warning("createdDate == null. Setting to 0.");
		createdDate = 0L;
	}

	private void checkPresenceMixCount() {
		if (mixCount != null) return;

		LOGGER.warning("mixCount == null. Making a new instance.");
		mixCount = new LoggingCount();
	}

	private void checkPresenceInjectedEntropy() {
		if (injectedEntropy != null) return;

		LOGGER.warning("injectedEntropy == null. Making a new instance.");
		injectedEntropy = new LoggingLong(0);
	}

	private void checkPresenceExtractedEntropy() {
		if (extractedEntropy != null) return;

		LOGGER.warning("extractedEntropy == null. Making a new instance.");
		extractedEntropy = new LoggingLong(0);
	}

	private void checkPresenceHashXY() {
		if (hashX == null) {
			LOGGER.warning("hashX == null. Setting to 0.");
			hashX = 0;
		}
		if (hashY == null) {
			LOGGER.warning("hashY == null. Setting to 0.");
			hashY = 0;
		}
	}
}
