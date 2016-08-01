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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import static org.digitalmodular.entropypool.EntropyPool.*;
import static org.digitalmodular.entropypool.EntropyPoolUtilities.*;
import org.digitalmodular.utilities.MoreSecureRandom;
import org.digitalmodular.utilities.Version;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-27
public class EntropyPoolBuilder {
	private String  magic   = null;
	private Version version = null;

	private Integer poolSize   = null;
	private byte[]  poolBuffer = null;

	private String        secureRandom       = null;
	private SecureRandom  builtSecureRandom  = null;
	private String        messageDigest      = null;
	private MessageDigest builtMessageDigest = null;
	private String        cipher             = null;
	private Cipher        builtCipher        = null;

	private long[] entropy = null;
	private int[]  hash    = null;
	private int[]  count   = null;
	private long[] date    = null;

	public void setMagic(String magic) {
		checkRepeatedTag(MAGIC_TAG, this.magic, magic);

		this.magic = magic;
	}

	public void setVersion(Version version) {
		checkRepeatedTag(VERSION_TAG, this.version, version);

		this.version = version;
	}

	public void setSecureRandom(String secureRandom) {
		checkRepeatedTag(SECURERANDOM_TAG, this.secureRandom, secureRandom);

		this.secureRandom = secureRandom;

		try {
			builtSecureRandom = MoreSecureRandom.getInstance(secureRandom);
		} catch (NoSuchAlgorithmException ex) {
			Logger.getLogger(EntropyPoolBuilder.class.getName())
			      .log(Level.WARNING, SECURERANDOM_TAG + " value cannot be instantiated. Using default: " +
			                          DEFAULT_SECURERANDOM_STRING, ex);
			try {
				builtSecureRandom = MoreSecureRandom.getInstance(DEFAULT_SECURERANDOM_STRING);
			} catch (NoSuchAlgorithmException ex2) {
				throw new LinkageError(null, ex2);
			}
		}
	}

	public void setMessageDigest(String messageDigest) {
		checkRepeatedTag(MESSAGEDIGEST_TAG, this.messageDigest, messageDigest);

		this.messageDigest = messageDigest;

		try {
			builtMessageDigest = MessageDigest.getInstance(messageDigest);
		} catch (NoSuchAlgorithmException ex) {
			Logger.getLogger(EntropyPoolBuilder.class.getName())
			      .log(Level.WARNING, MESSAGEDIGEST_TAG + " value cannot be instantiated. Using default: " +
			                          DEFAULT_MESSAGEDIGEST_STRING, ex);
			try {
				builtMessageDigest = MessageDigest.getInstance(DEFAULT_MESSAGEDIGEST_STRING);
			} catch (NoSuchAlgorithmException ex2) {
				throw new LinkageError(null, ex2);
			}
		}
	}

	public void setCipher(String cipher) {
		checkRepeatedTag(CIPHER_TAG, this.cipher, cipher);

		this.cipher = cipher;

		try {
			builtCipher = Cipher.getInstance(cipher);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
			Logger.getLogger(EntropyPoolBuilder.class.getName())
			      .log(Level.WARNING, CIPHER_TAG + " value cannot be instantiated. Using default: " +
			                          DEFAULT_CIPHER_STRING, ex);
			try {
				builtCipher = Cipher.getInstance(DEFAULT_CIPHER_STRING);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException ex2) {
				throw new LinkageError(null, ex2);
			}
		}
	}

	public void setPool(int poolSize) {
		checkRepeatedTag(POOL_TAG, this.poolSize, poolSize);

		this.poolSize = poolSize;

		this.poolBuffer = new byte[poolSize];
	}

	public byte[] getPoolBuffer() {
		return poolBuffer;
	}

	public void setEntropy(long[] entropy) {
		checkRepeatedTag(ENTROPY_TAG, this.entropy, entropy);

		this.entropy = entropy;
	}

	public void setHash(int[] hash) {
		checkRepeatedTag(HASH_TAG, this.hash, hash);

		this.hash = hash;
	}

	public void setCount(int[] count) {
		checkRepeatedTag(COUNT_TAG, this.count, count);

		this.count = count;
	}

	public void setDate(long[] date) {
		checkRepeatedTag(DATE_TAG, this.date, date);

		this.date = date;
	}

	public boolean isComplete() {
		if (version == null) return false;

		if (poolBuffer == null) return false;

		if (version.getMajor() == 2) {
			if (secureRandom == null) return false;
			if (messageDigest == null) return false;
			if (cipher == null) return false;
			if (entropy == null) return false;
			if (hash == null) return false;
			if (count == null) return false;
			if (date == null) return false;
		}

		return true;
	}

	public EntropyPool2 buildEntropyPool() {
		checkForErrors();

		EntropyPool2 pool = new EntropyPool2(poolBuffer,
		                                     builtSecureRandom, builtMessageDigest, builtCipher,
		                                     entropy[0], entropy[1], entropy[2],
		                                     hash[0], hash[1],
		                                     count[0], count[1], count[2],
		                                     date[0], date[1], date[2], date[3]);
		return pool;
	}

	private void checkRepeatedTag(String tag, Object oldValue, Object newValue) {
		if (oldValue == null) return;

		if (!Objects.deepEquals(oldValue, newValue)) {
			throw new IllegalArgumentException(
					tag + " tag repeated, value is different: " + oldValue + " <> " + newValue);
		}

		Logger.getLogger(EntropyPoolBuilder.class.getName()).warning(tag + " tag repeated");
	}

	private void checkForErrors() {
		if (poolBuffer == null) {
			throw new IllegalStateException(POOL_TAG + " tag not set");
		}

		if (version == null) {
			Logger.getLogger(EntropyPoolBuilder.class.getName())
			      .warning(VERSION_TAG + " tag not set");

			return;
		}

		int majorVersion = version.getMajor();

		if (majorVersion < 2) {
			throw new IllegalStateException(
					"Unsupported older version of pool file: " + version);
		} else if (majorVersion == 2) {
			checkPresenceSecureRandom();
			checkPresenceMessageDigest();
			checkPresenceCipher();
			checkPresenceEntropy();
			checkPresenceHash();
			checkPresenceCount();
			checkPresenceDate();
		} else if (majorVersion > 2) {
			throw new IllegalStateException(
					"Unsupported newer version of pool file: " + version);
		}
	}

	private void checkPresenceSecureRandom() {
		if (secureRandom != null) return;

		Logger.getLogger(EntropyPoolBuilder.class.getName()).warning(
				SECURERANDOM_TAG + " tag not set. Using default: " + DEFAULT_SECURERANDOM_STRING);
		setSecureRandom(DEFAULT_SECURERANDOM_STRING);
	}

	private void checkPresenceMessageDigest() {
		if (messageDigest != null) return;

		Logger.getLogger(EntropyPoolBuilder.class.getName()).warning(
				MESSAGEDIGEST_TAG + " tag not set. Using default: " + DEFAULT_MESSAGEDIGEST_STRING);
		setMessageDigest(DEFAULT_MESSAGEDIGEST_STRING);
	}

	private void checkPresenceCipher() {
		if (cipher != null) return;

		Logger.getLogger(EntropyPoolBuilder.class.getName()).warning(
				CIPHER_TAG + " tag not set. Using default: " + DEFAULT_CIPHER_STRING);
		setCipher(DEFAULT_CIPHER_STRING);
	}

	private void checkPresenceEntropy() {
		if (entropy != null) return;

		Logger.getLogger(EntropyPoolBuilder.class.getName()).warning(ENTROPY_TAG + " tag not set.");
		entropy = new long[]{0, 0, 0};
	}

	private void checkPresenceHash() {
		if (hash != null) return;

		Logger.getLogger(EntropyPoolBuilder.class.getName()).warning(HASH_TAG + " tag not set.");
		hash = new int[]{0, 0};
	}

	private void checkPresenceCount() {
		if (count != null) return;

		Logger.getLogger(EntropyPoolBuilder.class.getName()).warning(COUNT_TAG + " tag not set.");
		count = new int[]{0, 0, 0, 0};
	}

	private void checkPresenceDate() {
		if (date != null) return;

		Logger.getLogger(EntropyPoolBuilder.class.getName()).warning(DATE_TAG + " tag not set.");
		date = new long[]{System.currentTimeMillis(), 0, 0, 0};
	}
}
