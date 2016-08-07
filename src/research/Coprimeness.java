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

package research;

import java.math.BigInteger;

/**
 * We want to calculate the 'coprimeness' of two coprime numbers {n/d} for the following purpose: Using n as a modulo
 * field, and taking steps with d, the visited elements in the field form a permutation. For example, {8/3} makes the
 * permutation [0, 3, 6, 1, 4, 7, 2, 5]. When plotting these points on a circle, it forms a 'star polygon', in this case
 * the Octagram. The coprimeness is how 'random' these permutations look. The coprimeness would be lowest for any {n/1}
 * because steps of 1 are not random at all. The coprimeness is also not very high if after only a few iterations the
 * smallest gap becomes 1, for example in {16/5}, where after 2 iterations the permutation is [0, 5, 10] and the gaps
 * are 5, 5, and 6, but after 3 iterations the permutation is [0, 5, 10, 15] and gaps are 5, 5, 5 and 1 (15 is one away
 * from 0 in a modulo-16 field). A very coprime pair is {12/5} and it takes 5 iterations before a gap becomes 1: [0, 5,
 * 10, 3, 8, 1]. Just measuring how long it takes before the smallest gap becomes 1 is not a good measure however, since
 * there are combinations for which the average gap size reaches the value 1 sooner than another combination, but the
 * gap sizes before that are greater, giving a higher coprimeness. For example, {27/5} [5, 5, 5, 5, 2, 2, 2, 2, 2, 2, 1,
 * ..., 1] is less coprime than {27/10} [10, 7, 3, 3, 3, 3, 3, 1, ..., 1].
 * <p>
 * I define coprimeness as the area under the graph created by plotting y=some statistic against the iteration. Using
 * the entire gap size histogram to calculate a single statistic is hard, and moreover, histogram bins to the right of
 * the leftmost non-zero bin seem to convey little or no information about coprimeness. The statistic 'smallestGap'
 * seems reasonable. Incidentally, this is the index of the left-most non-zero bin. The statistic 'averageGap' can't be
 * used since it's just a reciprocal function of iteration.
 * <p>
 * Fun fact: for {f(n)/f(n-2)} where f(n) is the nth fibonacci number, the smallest gaps write out the inverse of the
 * fibonacci series. For example: {34/13} [13, 8, 5, 5, 3, ..., 2, ..., 1].
 *
 * @author Mark Jeronimus
 * @version 1.0
 * @since 1.0
 */
// Created 2016-08-07
public class Coprimeness {
	public static final boolean DEBUG = true;

	public static void main(String[] args) {
		if (DEBUG) {
			testCase(13);
		} else {
			int digestLength = 64;
			int fieldLength  = 261;

			int from = 65536 - fieldLength - 14;
			int to   = 65536 - fieldLength;
			System.out.println("[" + from + "," + to + ")");
			for (int bufferLength = from; bufferLength <= to; bufferLength += 2)
				analyze(bufferLength, digestLength);
		}
	}

	private static void testCase(int n) {
		for (int d = 3; d <= n / 2; d += (2 - n % 2))
			analyze(n, d);
	}

	private static void analyze(int n, int d) {
		boolean[] map = new boolean[n];

		// Must be coprime first.
		if (!BigInteger.valueOf(n).gcd(BigInteger.valueOf(d)).equals(BigInteger.ONE)) return;

		int coprimeness = 0;

		int p = 0;
		map[p] = true;
		while (true) {
			p = (p + d) % n;
			if (map[p]) break;
			map[p] = true;

			int smallestGap = findSmallestGap(map);

			if (DEBUG) {
				System.out.print(smallestGap + 1 + "\t");
			} else {
				if (smallestGap == 0) break;
			}

			coprimeness += smallestGap;
		}

		System.out.println("{" + n + "/" + d + "} coprimeness = " + coprimeness);
	}

	private static int findSmallestGap(boolean[] map) {
		int smallestGap = Integer.MAX_VALUE;
		int lastSet     = 0;
		for (int i = 1; i < map.length; i++) {
			if (map[i]) {
				smallestGap = Math.min(smallestGap, i - lastSet);
				lastSet = i;
			}
		}
		if (lastSet > map.length / 2) smallestGap = Math.min(smallestGap, map.length - lastSet);

		// Return one less for speed optimization. Resulting coprimeness is only offset by a constant.
		return smallestGap - 1;
	}

	private static void dump(boolean[] map) {
		for (boolean b : map) System.out.print(b ? '1' : '0');
		System.out.println();
	}
}
