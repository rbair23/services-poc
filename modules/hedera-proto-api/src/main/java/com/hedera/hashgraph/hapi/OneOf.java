package com.hedera.hashgraph.hapi;

/**
 * Record for OneOf values
 *
 * @param type a enum for the type of data
 * @param value the untyped value, use enum to know type
 * @param <E> enum class for type
 */
public record OneOf<E>(
		E type,
		Object value
) {

	public <T> T getValue() {
		return (T)value;
	}
}
