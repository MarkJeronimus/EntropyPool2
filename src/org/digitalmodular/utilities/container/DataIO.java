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

/**
 * @author Mark Jeronimus
 */
// Created 2016-08-03
public enum DataIO {
	;

	public static LoggingCount readLoggingCount(DataInput in) throws IOException {
		int  count      = in.readInt();
		long modifyDate = in.readLong();

		LoggingCount loggingCount = new LoggingCount(count, modifyDate);
		return loggingCount;
	}

	public static void writeLoggingCount(DataOutput out, LoggingCount value) throws IOException {
		out.writeInt(value.getCount());
		out.writeLong(value.getModifyDate());
	}

	public static void writeLoggingLong(DataOutput out, LoggingLong value) throws IOException {
		out.writeLong(value.getValue());
		out.writeInt(value.getModifyCount());
		out.writeLong(value.getModifyDate());
	}

	public static LoggingLong readLoggingLong(DataInput in) throws IOException {
		long value       = in.readLong();
		int  modifyCount = in.readInt();
		long modifyDate  = in.readLong();

		LoggingLong loggingLong = new LoggingLong(value, modifyCount, modifyDate);
		return loggingLong;
	}

	public static Version readVersion(DataInput in) throws IOException {
		int major        = in.readInt();
		int minor        = in.readInt();
		int releaseValue = in.readInt();
		int revision     = in.readInt();

		Version.Release release = Version.Release.of(releaseValue);

		Version version = new Version(major, minor, release, revision);
		return version;
	}

	public static void writeVersion(DataOutput out, Version version) throws IOException {
		out.writeInt(version.getMajor());
		out.writeInt(version.getMinor());
		out.writeInt(version.getRelease().getValue());
		out.writeInt(version.getRevision());
	}
}
