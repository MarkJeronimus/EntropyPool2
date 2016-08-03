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
import java.util.Objects;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.prng.SP800SecureRandomBuilder;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-29
public enum SecureRandomFactory {
	;

	public static SecureRandom getInstance(String algorithm) throws NoSuchAlgorithmException {
		Objects.requireNonNull(algorithm,
		                       "algorithm = null");

		String[] parts = algorithm.split("/");
		switch (parts[0]) {
			case "Strong":
				if (parts.length > 1) throw new IllegalArgumentException(
						"Strong should have between 0 parameters: " + algorithm);

				SecureRandom secureRandom = SecureRandom.getInstanceStrong();
				return secureRandom;
			case "SP800CTR": // http://csrc.nist.gov/publications/nistpubs/800-90A/SP800-90A.pdf
				if (parts.length > 5) throw new IllegalArgumentException(
						algorithm + " has invalid format. Format should be " +
						"SP800CTR[/<BlockCipher>[/<keyLength>[/<SecureRandom>[/<randomBitsToUse>]]]]: ");

				BlockCipher cipher;
				if (parts.length < 2) {
					cipher = new AESFastEngine();
				} else {
					switch (parts[1]) {
						case "AES":
							cipher = new AESFastEngine();
							break;
						case "TDEA":
							cipher = new DESedeEngine();
							break;
						default:
							throw new NoSuchAlgorithmException("BlockCipher should be one of [AES, TDEA]: " + parts[1]);
					}
				}

				int keyLength;
				int securityStrength;
				if (cipher instanceof AESFastEngine) {
					keyLength = parts.length < 3 ? 256 : Integer.parseInt(parts[2]);
					if (keyLength != 128 && keyLength != 192 && keyLength != 256)
						throw new IllegalArgumentException(
								"keyLength should be one of [128, 192, 256] for AES: " + keyLength);
					securityStrength = keyLength;
				} else {
					keyLength = parts.length < 3 ? 168 : Integer.parseInt(parts[2]);
					if (keyLength != 168)
						throw new IllegalArgumentException("keyLength should be 168 for TDEA: " + keyLength);
					securityStrength = 112;
				}

				String secureRandomAlgorithm = parts.length < 4 ? "Strong" : parts[3];
				secureRandom = getInstance(secureRandomAlgorithm);

				int randomBitsToUse = parts.length < 5 ? 16777216 : Integer.parseInt(parts[4]);
				if (randomBitsToUse < securityStrength)
					throw new IllegalArgumentException(
							"randomBitsToUse should be at least securityStrength: " + randomBitsToUse);

				SP800SecureRandomBuilder secureRandomBuilder = new SP800SecureRandomBuilder(secureRandom, false);
				secureRandomBuilder.setPersonalizationString(null);
				secureRandomBuilder.setSecurityStrength(securityStrength);
				secureRandomBuilder.setEntropyBitsRequired(randomBitsToUse);

				secureRandom = secureRandomBuilder.buildCTR(cipher, keyLength, null, false);
				secureRandom = new SecureRandomWithAlgorithm(secureRandom, algorithm);
				return secureRandom;
			default:
				secureRandom = SecureRandom.getInstance(algorithm);
				return secureRandom;
		}
	}

	private static class SecureRandomWithAlgorithm extends SecureRandom {
		private final String       algorithm;
		private final SecureRandom secureRandom;

		private SecureRandomWithAlgorithm(SecureRandom secureRandom, String algorithm) {
			super();
			this.secureRandom = secureRandom;
			this.algorithm = algorithm;
		}

		@Override
		public String getAlgorithm() { return algorithm; }

		@Override
		public void setSeed(byte[] seed) { throw new UnsupportedOperationException(); }

		@Override
		public void setSeed(long seed) {
			super.setSeed(seed);
		}

		@Override
		public void nextBytes(byte[] bytes) { secureRandom.nextBytes(bytes); }

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
		public double nextGaussian() { return secureRandom.nextGaussian(); }

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
