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

import java.util.Objects;

/**
 * @author Mark Jeronimus
 */
// Created 2016-07-30
public class Version {
	public enum Release {
		STABLE(0x00000000, "Stable"),
		RC(0xCCCCCCCC, "RC"),
		BETA(0xBBBBBBBB, "Beta"),
		ALPHA(0xAAAAAAAA, "Alpha");

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

	private final int     minor;
	private final int     major;
	private final int     revision;
	private final Release release;

	public Version(int major, int minor, int revision, Release release) {
		Objects.requireNonNull(release, "release = null");
		Verifyer.requireThat(major >= 0, "Major version not in range [0,Integer.MAX_VALUE]: " + major);
		Verifyer.requireThat(minor >= 0, "Minor version not in range [0,Integer.MAX_VALUE]: " + minor);
		Verifyer.requireThat(revision > 0, "Revision not in range [1,Integer.MAX_VALUE]: " + revision);

		this.minor = minor;
		this.major = major;
		this.revision = revision;
		this.release = release;
	}

	public int getMinor()       { return minor; }

	public int getMajor()       { return major; }

	public int getRevision()    { return revision; }

	public Release getRelease() { return release; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Version)) return false;

		Version version = (Version) o;

		return (getMinor() == version.getMinor() &&
		        getMajor() == version.getMajor() &&
		        getRevision() == version.getRevision() &&
		        getRelease() == version.getRelease());
	}

	@Override
	public int hashCode() {
		int result = getMinor();
		result = 31 * result + getMajor();
		result = 31 * result + getRevision();
		result = 31 * result + (getRelease() == null ? 0 : getRelease().hashCode());
		return result;
	}

	@Override
	public String toString() {
		if (release != Release.STABLE)
			return String.format("%d.%d (r%d) %s", minor, major, revision, release.getReleaseName());
		else
			return String.format("%d.%d (r%d)", minor, major, revision);
	}
}
