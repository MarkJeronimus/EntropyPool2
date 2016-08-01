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
import java.security.Provider;
import java.security.SecureRandom;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.prng.SP800SecureRandomBuilder;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-29
public class MoreSecureRandom extends SecureRandom {
	private final String       algorithm;
	private final SecureRandom secureRandom;

	private MoreSecureRandom(String algorithm, SecureRandom secureRandom) {
		super();
		this.algorithm = algorithm;
		this.secureRandom = secureRandom;
	}

	public static MoreSecureRandom getInstance(String algorithm) throws NoSuchAlgorithmException {
		String[] parts = algorithm.split("/");
		switch (parts[0]) {
			case "SP800CTR":
				// Format: SP800CTR/<BlockCipher>/<keySizeInBits>/<securityStrength>/<entropyBitsRequired>

				BlockCipher cipher;
				if (parts.length < 2) {
					cipher = new AESFastEngine();
				} else {
					switch (parts[1]) {
						case "AES":
							cipher = new AESFastEngine();
							break;
						default:
							throw new NoSuchAlgorithmException(
									"No BlockCipher known as " + parts[1] + " in " + algorithm);
					}
				}

				int keySizeInBits = parts.length < 3 ? 256 : Integer.parseInt(parts[2]);
				if (keySizeInBits != 128 && keySizeInBits != 92 && keySizeInBits != 256)
					throw new IllegalArgumentException(
							"keySizeInBits should be one of [128, 192, 256]: " + keySizeInBits);

				int securityStrength = parts.length < 4 ? 256 : Integer.parseInt(parts[3]);
				if (securityStrength < 256)
					throw new IllegalArgumentException(
							"securityStrength should be in the range [0, 256]: " + securityStrength);

				int entropyBitsRequired = parts.length < 5 ? 16777216 : Integer.parseInt(parts[4]);
				if (entropyBitsRequired < securityStrength)
					throw new IllegalArgumentException(
							"entropyBitsRequired should be at least securityStrength: " + entropyBitsRequired);

				SecureRandom secureRandom = SecureRandom.getInstanceStrong();

				SP800SecureRandomBuilder secureRandomBuilder = new SP800SecureRandomBuilder(secureRandom, false);
				secureRandomBuilder.setPersonalizationString(null);
				secureRandomBuilder.setSecurityStrength(securityStrength);
				secureRandomBuilder.setEntropyBitsRequired(entropyBitsRequired);

				secureRandom = secureRandomBuilder.buildCTR(cipher, keySizeInBits, null, false);
				return new MoreSecureRandom(algorithm, secureRandom);
			default:
				secureRandom = SecureRandom.getInstance(algorithm);
				return new MoreSecureRandom(algorithm, secureRandom);
		}
	}

	public static SecureRandom getInstance(String algorithm, String provider) {
		throw new UnsupportedOperationException("Use getInstance(String algorithm)");
	}

	public static SecureRandom getInstance(String algorithm, Provider provider) {
		throw new UnsupportedOperationException("Use getInstance(String algorithm)");
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

	public static byte[] getSeed(int numBytes) { throw new UnsupportedOperationException(); }

	@Override
	public byte[] generateSeed(int numBytes) { return secureRandom.generateSeed(numBytes); }

	public static SecureRandom getInstanceStrong() throws NoSuchAlgorithmException {
		throw new UnsupportedOperationException("Use getInstance(String algorithm)");
	}

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
