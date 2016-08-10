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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import static java.util.Objects.requireNonNull;

import static org.digitalmodular.utilities.FNV.hashFNV;
import static org.digitalmodular.utilities.FNV.startFNV;
import static org.digitalmodular.utilities.Verifier.requireThat;

/**
 * @author Mark Jeronimus
 */
// Created 2016-07-30
public class Version {
	public enum Release {
		STABLE(0x00, "Stable"),
		RC(0xCC, "RC"),
		BETA(0xBB, "Beta"),
		ALPHA(0xAA, "Alpha"),
		MILESTONE(0x88, "Milestone"),
		DEVELOPMENT(0x11, "Development");

		private final byte   value;
		private final String releaseName;

		Release(int value, String releaseName) {
			this.value = (byte)value;
			this.releaseName = releaseName;
		}

		public byte getValue()         { return value; }

		public String getReleaseName() { return releaseName; }

		public static Release of(int value) {
			for (Release release : values())
				if (release.getValue() == value) return release;

			throw new IllegalArgumentException("No Release with value: " + value);
		}
	}

	private final byte    major;
	private final byte    minor;
	private final Release release;
	private final int     revision;

	public Version(int major, int minor, Release release, int revision) {
		requireThat(major >= 0 && major <= Byte.MAX_VALUE, "major not in range [0,Byte.MAX_VALUE]: " + major);
		requireThat(minor >= 0 && minor <= Byte.MAX_VALUE, "minor not in range [0,Byte.MAX_VALUE]: " + minor);
		requireNonNull(release, "release == null");
		requireThat(revision >= 1, "revision not in range [1,Integer.MAX_VALUE]: " + revision);

		this.major = (byte)major;
		this.minor = (byte)minor;
		this.release = release;
		this.revision = (short)revision;
	}

	public byte getMinor()      { return minor; }

	public byte getMajor()      { return major; }

	public Release getRelease() { return release; }

	public int getRevision()    { return revision; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Version)) return false;

		Version version = (Version)o;

		return getMinor() == version.getMinor() &&
		       getMajor() == version.getMajor() &&
		       getRelease() == version.getRelease() &&
		       getRevision() == version.getRevision();
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
		if (release == Release.STABLE)
			return String.format("%d.%d (r%d)", major, minor, revision);
		else
			return String.format("%d.%d %s (r%d)", major, minor, release.getReleaseName(), revision);
	}

	public String toShortString() {
		if (release == Release.STABLE)
			return String.format("%d.%d", major, minor);
		else
			return String.format("%d.%d %s", major, minor, release.getReleaseName());
	}

	public void writeTo(DataOutput out) throws IOException {
		out.writeByte(major);
		out.writeByte(minor);
		out.writeByte(release.getValue());
		out.writeInt(revision);
	}

	public static Version readFrom(DataInput in) throws IOException {
		byte major        = in.readByte();
		byte minor        = in.readByte();
		byte releaseValue = in.readByte();
		int  revision     = in.readInt();

		Version.Release release = Version.Release.of(releaseValue);

		Version version = new Version(major, minor, release, revision);
		return version;
	}
}
