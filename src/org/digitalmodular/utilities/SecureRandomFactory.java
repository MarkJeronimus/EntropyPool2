/*
 * This file is part of Utilities.
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

package org.digitalmodular.utilities;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.prng.SP800SecureRandomBuilder;

/**
 * @author Mark Jeronimus
 */
// Created 2016-07-29
public enum SecureRandomFactory {
	;

	public static SecureRandom getInstance(String algorithm) throws NoSuchAlgorithmException {
		String[] parts = algorithm.split("/");
		switch (parts[0]) {
			case "Strong":
				return instantiateSecureRandomStrong(algorithm, parts);
			case "SP800CTR":
				return instantiateSP800CTR(algorithm, parts);
			default:
				return instantiateSecureRandom(algorithm);
		}
	}

	private static SecureRandom instantiateSecureRandom(String algorithm) throws NoSuchAlgorithmException {
		SecureRandom secureRandom = SecureRandom.getInstance(algorithm);
		return secureRandom;
	}

	private static SecureRandom instantiateSecureRandomStrong(String algorithm, String[] parts)
			throws NoSuchAlgorithmException {
		if (parts.length > 1) throw new IllegalArgumentException(
				"Strong should have between 0 parameters: " + algorithm);

		SecureRandom secureRandom = SecureRandom.getInstanceStrong();
		return secureRandom;
	}

	private static SecureRandom instantiateSP800CTR(String algorithm, String[] parts) throws NoSuchAlgorithmException {
		// See: http://csrc.nist.gov/publications/nistpubs/800-90A/SP800-90A.pdf

		if (parts.length > 5) throw new IllegalArgumentException(
				algorithm + " has invalid format. Format should be " +
				"SP800CTR[/<BlockCipher>[/<keyLength>[/<SecureRandom>[/<randomBitsToUse>]]]]: ");

		// Optional parameter at index 1
		BlockCipher cipher = getBlockCipherForSP800CTR(parts, 1);

		// Optional parameter at index 2
		int defaultKeyLength = getDefaultKeyLengthForSP800CTR(cipher);
		int keyLength        = getArgsValue(parts, 2, defaultKeyLength);
		validateKeyLengthForSP800CTR(keyLength, cipher);

		int securityStrength = getSecurityStrengthForSP800(keyLength, cipher);

		// Optional parameter at index 3
		SecureRandom entropySource = getEntropySourceForSP800(parts, 3);

		// Optional parameter at index 4
		int defaultRandomBitsToUse = 16777216;
		int randomBitsToUse        = getArgsValue(parts, 4, defaultRandomBitsToUse);
		validateRandomBitsToUseForSP800(randomBitsToUse, securityStrength);

		SP800SecureRandomBuilder secureRandomBuilder = new SP800SecureRandomBuilder(entropySource, false);
		secureRandomBuilder.setPersonalizationString(null);
		secureRandomBuilder.setSecurityStrength(securityStrength);
		secureRandomBuilder.setEntropyBitsRequired(randomBitsToUse);

		SecureRandom secureRandom = secureRandomBuilder.buildCTR(cipher, keyLength, null, false);
		secureRandom = new SecureRandomWithAlgorithm(secureRandom, algorithm);
		return secureRandom;
	}

	private static int getArgsValue(String[] parts, int index, int defaultValue) {
		return parts.length <= index ? defaultValue : Integer.parseInt(parts[index]);
	}

	private static BlockCipher getBlockCipherForSP800CTR(String[] parts, int index) throws NoSuchAlgorithmException {
		if (parts.length <= index)
			return new AESFastEngine();

		switch (parts[index]) {
			case "AES":
				return new AESFastEngine();
			case "TDEA":
				return new DESedeEngine();
			default:
				throw new NoSuchAlgorithmException("BlockCipher should be one of [AES, TDEA]: " + parts[index]);
		}
	}

	private static int getDefaultKeyLengthForSP800CTR(BlockCipher cipher) {
		if (cipher instanceof AESFastEngine) {
			return 256;
		} else {
			return 168;
		}
	}

	private static void validateKeyLengthForSP800CTR(int keyLength, BlockCipher cipher) {
		if (cipher instanceof AESFastEngine) {
			if (keyLength != 128 && keyLength != 192 && keyLength != 256)
				throw new IllegalArgumentException("keyLength should be one of [128, 192, 256] for AES: " + keyLength);
		} else {
			if (keyLength != 168)
				throw new IllegalArgumentException("keyLength should be 168 for TDEA: " + keyLength);
		}
	}

	private static int getSecurityStrengthForSP800(int keyLength, BlockCipher cipher) {
		if (cipher instanceof AESFastEngine) {
			return keyLength;
		} else {
			return 112;
		}
	}

	private static SecureRandom getEntropySourceForSP800(String[] parts, int index) throws NoSuchAlgorithmException {
		String       entropySourceAlgorithm = parts.length <= index ? "Strong" : parts[index];
		SecureRandom entropySource          = getInstance(entropySourceAlgorithm);
		return entropySource;
	}

	private static void validateRandomBitsToUseForSP800(int randomBitsToUse, int securityStrength) {
		if (randomBitsToUse < securityStrength)
			throw new IllegalArgumentException(
					"randomBitsToUse should be at least securityStrength: " + randomBitsToUse);
	}

	private static final class SecureRandomWithAlgorithm extends SecureRandom {
		private final String       algorithm;
		private final SecureRandom secureRandom;

		private SecureRandomWithAlgorithm(SecureRandom secureRandom, String algorithm) {
			this.secureRandom = secureRandom;
			this.algorithm = algorithm;
		}

		@Override
		public String getAlgorithm() { return algorithm; }

		@Override
		public synchronized void setSeed(byte[] seed) { secureRandom.setSeed(seed); }

		@Override
		public synchronized void nextBytes(byte[] bytes) { secureRandom.nextBytes(bytes); }

		@Override
		public byte[] generateSeed(int numBytes) { return secureRandom.generateSeed(numBytes); }

		@Override
		public int nextInt() { return secureRandom.nextInt(); }

		@Override
		public int nextInt(int bound) { return secureRandom.nextInt(bound); }

		@Override
		public long nextLong() { return secureRandom.nextLong(); }

		@Override
		public boolean nextBoolean() { return secureRandom.nextBoolean(); }

		@Override
		public float nextFloat() { return secureRandom.nextFloat(); }

		@Override
		public double nextDouble() { return secureRandom.nextDouble(); }

		@Override
		public synchronized double nextGaussian() { return secureRandom.nextGaussian(); }

		@Override
		public IntStream ints(long streamSize) { return secureRandom.ints(streamSize); }

		@Override
		public IntStream ints() { return secureRandom.ints(); }

		@Override
		public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
			return secureRandom.ints(streamSize, randomNumberOrigin, randomNumberBound);
		}

		@Override
		public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
			return secureRandom.ints(randomNumberOrigin, randomNumberBound);
		}

		@Override
		public LongStream longs(long streamSize) { return secureRandom.longs(streamSize); }

		@Override
		public LongStream longs() { return secureRandom.longs(); }

		@Override
		public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
			return secureRandom.longs(streamSize, randomNumberOrigin, randomNumberBound);
		}

		@Override
		public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
			return secureRandom.longs(randomNumberOrigin, randomNumberBound);
		}

		@Override
		public DoubleStream doubles(long streamSize) { return secureRandom.doubles(streamSize); }

		@Override
		public DoubleStream doubles() { return secureRandom.doubles(); }

		@Override
		public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
			return secureRandom.doubles(streamSize, randomNumberOrigin, randomNumberBound);
		}

		@Override
		public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
			return secureRandom.doubles(randomNumberOrigin, randomNumberBound);
		}
	}
}
