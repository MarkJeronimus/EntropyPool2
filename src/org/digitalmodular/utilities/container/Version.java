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

package org.digitalmodular.utilities.container;

import java.util.Objects;
import static org.digitalmodular.utilities.FNV.hashFNV;
import static org.digitalmodular.utilities.FNV.startFNV;
import org.digitalmodular.utilities.Verifier;

/**
 * @author Mark Jeronimus
 */
// Created 2016-07-30
public class Version {
	public enum Release {
		STABLE(0x00000000, "Stable"),
		RC(0xCCCCCCCC, "RC"),
		BETA(0xBBBBBBBB, "Beta"),
		ALPHA(0xAAAAAAAA, "Alpha"),
		MILESTONE(0x88888888, "Milestone"),
		DEVELOPMENT(0x11111111, "Development");

		private final int    value;
		private final String releaseName;

		Release(int value, String releaseName) {
			this.value = value;
			this.releaseName = releaseName;
		}

		public int getValue()          { return value; }

		public String getReleaseName() { return releaseName; }

		public static Release of(int value) {
			for (Release release : values())
				if (release.getValue() == value) return release;

			throw new IllegalArgumentException("No Release with value: " + value);
		}
	}

	private final int     major;
	private final int     minor;
	private final Release release;
	private final int     revision;

	public Version(int major, int minor, Release release, int revision) {
		Verifier.requireThat(major >= 0,
		                     "major not in range [0,Integer.MAX_VALUE]: " +
		                     major);
		Verifier.requireThat(minor >= 0,
		                     "minor not in range [0,Integer.MAX_VALUE]: " +
		                     minor);
		Objects.requireNonNull(release,
		                       "release == null");
		Verifier.requireThat(revision > 0,
		                     "revision not in range [1,Integer.MAX_VALUE]: " +
		                     revision);

		this.major = major;
		this.minor = minor;
		this.release = release;
		this.revision = revision;
	}

	public int getMinor()       { return minor; }

	public int getMajor()       { return major; }

	public Release getRelease() { return release; }

	public int getRevision()    { return revision; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Version)) return false;

		Version version = (Version) o;

		return (getMinor() == version.getMinor() &&
		        getMajor() == version.getMajor() &&
		        getRelease() == version.getRelease() &&
		        getRevision() == version.getRevision());
	}

	@Override
	public int hashCode() {
		int hashCode = startFNV();
		hashCode = hashFNV(hashCode, getMajor());
		hashCode = hashFNV(hashCode, getMinor());
		hashCode = hashFNV(hashCode, getRelease());
		hashCode = hashFNV(hashCode, getRevision());
		return hashCode;
	}

	@Override
	public String toString() {
		if (release != Release.STABLE)
			return String.format("%d.%d %s (r%d)", major, minor, release.getReleaseName(), revision);
		else
			return String.format("%d.%d (r%d)", major, minor, revision);
	}

	public String toShortString() {
		if (release != Release.STABLE)
			return String.format("%d.%d %s", major, minor, release.getReleaseName());
		else
			return String.format("%d.%d", major, minor);
	}
}
