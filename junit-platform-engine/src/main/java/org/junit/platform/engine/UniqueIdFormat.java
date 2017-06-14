/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.UniqueId.Segment;

/**
 * Used to {@link #parse} a {@link UniqueId} from a string representation
 * or to {@link #format} a {@link UniqueId} into a string representation.
 *
 * @since 1.0
 */
class UniqueIdFormat implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final UniqueIdFormat defaultFormat = new UniqueIdFormat('[', ':', ']', '/');

	static UniqueIdFormat getDefault() {
		return defaultFormat;
	}

	private final char openSegment;
	private final char closeSegment;
	private final char segmentDelimiter;
	private final char typeValueSeparator;
	private final Pattern segmentSplitter;

	UniqueIdFormat(char openSegment, char typeValueSeparator, char closeSegment, char segmentDelimiter) {
		this.openSegment = openSegment;
		this.typeValueSeparator = typeValueSeparator;
		this.closeSegment = closeSegment;
		this.segmentDelimiter = segmentDelimiter;
		this.segmentSplitter = Pattern.compile(Pattern.quote("" + closeSegment + segmentDelimiter + openSegment));
	}

	/**
	 * Parse a {@code UniqueId} from the supplied string representation.
	 *
	 * @return a properly constructed {@code UniqueId}
	 * @throws JUnitException if the string cannot be parsed
	 */
	UniqueId parse(String source) throws JUnitException {
		String trim = source.trim();
		int lastIndex = trim.length() - 1;
		if (trim.charAt(0) != openSegment || trim.charAt(lastIndex) != closeSegment) {
			throw new JUnitException(String.format("'%s' is not a well-formed UniqueId", source));
		}
		trim = trim.substring(1, lastIndex);
		List<Segment> segments = segmentSplitter.splitAsStream(trim).map(this::createSegment).collect(toList());
		return new UniqueId(this, segments);
	}

	private Segment createSegment(String segmentString) throws JUnitException {
		if (segmentString.length() < 3) {
			throw new JUnitException(String.format("'%s' is not a well-formed UniqueId segment", segmentString));
		}
		int index = segmentString.indexOf(typeValueSeparator);
		if (index == -1) {
			throw new JUnitException(String.format("'%s' is not a well-formed UniqueId segment", segmentString));
		}
		String type = segmentString.substring(0, index);
		String value = segmentString.substring(index + 1);
		return new Segment(type, value);
	}

	/**
	 * Format and return the string representation of the supplied {@code UniqueId}.
	 */
	String format(UniqueId uniqueId) {
		// @formatter:off
		return uniqueId.getSegments().stream()
			.map(this::describe)
			.collect(joining(String.valueOf(this.segmentDelimiter)));
		// @formatter:on
	}

	private String describe(Segment segment) {
		return String.format("%s%s%s%s%s", this.openSegment, segment.getType(), this.typeValueSeparator,
			segment.getValue(), this.closeSegment);
	}
}
