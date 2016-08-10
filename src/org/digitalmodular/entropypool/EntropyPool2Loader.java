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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import static org.digitalmodular.entropypool.EntropyPool2.*;
import static org.digitalmodular.utilities.io.DataIO.*;
import org.digitalmodular.utilities.SecureRandomFactory;
import org.digitalmodular.utilities.container.LoggingCount;
import org.digitalmodular.utilities.container.LoggingVariable;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-08-03
enum EntropyPool2Loader {
	;

	static EntropyPool2 readFrom(DataInput in) throws IOException {
		long         createDate  = in.readLong();
		LoggingCount accessCount = readLoggingCount(in);

		LoggingVariable<SecureRandom>  secureRandom  = readLoggingSecureRandom(in);
		LoggingVariable<MessageDigest> messageDigest = readLoggingMessageDigest(in);
		LoggingVariable<Cipher>        cipher        = readLoggingCipher(in);

		LoggingVariable<Long> injectedEntropy  = readLoggingVariable(in.readLong(), in);
		LoggingVariable<Long> extractedEntropy = readLoggingVariable(in.readLong(), in);
		LoggingCount          mixCount         = readLoggingCount(in);

		int hashX = in.readInt();
		int hashY = in.readInt();

		byte[] buffer = readByteArray(in);

		EntropyPool2 pool = new EntropyPool2(createDate, accessCount, secureRandom, messageDigest, cipher,
		                                     injectedEntropy, extractedEntropy, mixCount, hashX, hashY, buffer);
		return pool;
	}

	private static LoggingVariable<SecureRandom> readLoggingSecureRandom(DataInput in) throws IOException {
		String                        secureRandomAlgorithm = in.readUTF();
		SecureRandom                  secureRandomInstance  = instantiateSecureRandom(secureRandomAlgorithm);
		LoggingVariable<SecureRandom> secureRandom          = readLoggingVariable(secureRandomInstance, in);
		return secureRandom;
	}

	private static LoggingVariable<MessageDigest> readLoggingMessageDigest(DataInput in) throws IOException {
		String                         messageDigestAlgorithm = in.readUTF();
		MessageDigest                  messageDigestInstance  = instantiateMessageDigest(messageDigestAlgorithm);
		LoggingVariable<MessageDigest> messageDigest          = readLoggingVariable(messageDigestInstance, in);
		return messageDigest;
	}

	private static LoggingVariable<Cipher> readLoggingCipher(DataInput in) throws IOException {
		String                  cipherAlgorithm = in.readUTF();
		Cipher                  cipherInstance  = instantiateCipher(cipherAlgorithm);
		LoggingVariable<Cipher> cipher          = readLoggingVariable(cipherInstance, in);
		return cipher;
	}

	private static SecureRandom instantiateSecureRandom(String secureRandomAlgorithm) throws IOException {
		SecureRandom secureRandom;

		try {
			secureRandom = SecureRandomFactory.getInstance(secureRandomAlgorithm);
		} catch (NoSuchAlgorithmException ex) {
			Logger.getGlobal().log(Level.WARNING, "SecureRandom cannot be instantiated:" + secureRandomAlgorithm +
			                                      ". Using default: " + DEFAULT_SECURERANDOM_STRING, ex);
			try {
				secureRandom = SecureRandomFactory.getInstance(DEFAULT_SECURERANDOM_STRING);
			} catch (NoSuchAlgorithmException ex2) {
				LinkageError error = new LinkageError(ex2.getMessage(), ex2);
				error.addSuppressed(ex);
				throw error;
			}
		}

		return secureRandom;
	}

	private static MessageDigest instantiateMessageDigest(String messageDigestAlgorithm) throws IOException {
		MessageDigest messageDigest;

		try {
			messageDigest = MessageDigest.getInstance(messageDigestAlgorithm);
		} catch (NoSuchAlgorithmException ex) {
			Logger.getGlobal().log(Level.WARNING, "MessageDigest cannot be instantiated: " + messageDigestAlgorithm +
			                                      ". Using default: " + DEFAULT_MESSAGEDIGEST_STRING, ex);
			try {
				messageDigest = MessageDigest.getInstance(DEFAULT_MESSAGEDIGEST_STRING);
			} catch (NoSuchAlgorithmException ex2) {
				LinkageError error = new LinkageError(ex2.getMessage(), ex2);
				error.addSuppressed(ex);
				throw error;
			}
		}

		return messageDigest;
	}

	private static Cipher instantiateCipher(String cipherAlgorithm) throws IOException {
		Cipher cipher;

		try {
			cipher = Cipher.getInstance(cipherAlgorithm);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
			Logger.getGlobal().log(Level.WARNING, "Cipher cannot be instantiated: " + cipherAlgorithm +
			                                      ". Using default: " + DEFAULT_CIPHER_STRING, ex);
			try {
				cipher = Cipher.getInstance(DEFAULT_CIPHER_STRING);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException ex2) {
				LinkageError error = new LinkageError(ex2.getMessage(), ex2);
				error.addSuppressed(ex);
				throw error;
			}
		}

		return cipher;
	}
}
