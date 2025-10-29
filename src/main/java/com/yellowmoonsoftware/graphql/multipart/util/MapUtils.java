package com.yellowmoonsoftware.graphql.multipart.util;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * <h2>MapUtils</h2>
 * <p>
 * Helpers for working with maps in stream pipelines.
 */
public class MapUtils {
    private MapUtils() {}

    /**
     * Join two maps by key into a stream of paired entries.
     * @param leftMap map providing left values
     * @param rightMap map providing right values
     * @param <K> key type
     * @param <T> left value type
     * @param <U> right value type
     * @return stream of {@link JoinedEntry} containing only keys present in both maps
     */
    public static <K, T, U> Stream<JoinedEntry<K, T, U>> joinToStream(final Map<K, T> leftMap, final Map<K, U> rightMap) {
        return leftMap.keySet().stream()
                .filter(rightMap::containsKey)
                .map(k -> new JoinedEntry<>(k, leftMap.get(k), rightMap.get(k)));
    }

    /**
     * Pair of values with a shared key, plus mapping helpers.
     * @param <K> key type
     * @param <T> left value type
     * @param <U> right value type
     * @param key shared key for both values
     * @param leftValue value from the left map
     * @param rightValue value from the right map
     */
    public record JoinedEntry<K, T, U>(K key, T leftValue, U rightValue) {
        /**
         * Map the left value to a new type while keeping the key and right intact.
         * @param valueMapper function to transform the left value
         * @param <R> new left value type
         * @return new {@link JoinedEntry} with mapped left value
         */
        public <R> JoinedEntry<K, R, U> mapLeft(final Function<? super T, ? extends R> valueMapper) {
            return new JoinedEntry<>(this.key, valueMapper.apply(this.leftValue), this.rightValue);
        }

        /**
         * Map the right value to a new type while keeping the key and left intact.
         * @param valueMapper function to transform the right value
         * @param <R> new right value type
         * @return new {@link JoinedEntry} with mapped right value
         */
        public <R> JoinedEntry<K, T, R> mapRight(final Function<? super U, ? extends R> valueMapper) {
            return new JoinedEntry<>(this.key, this.leftValue, valueMapper.apply(this.rightValue));
        }

        /**
         * Expand the left value into multiple entries.
         * @param valueMapper function to expand the left value into a stream
         * @param <R> element type produced from the left value
         * @return stream of {@link JoinedEntry} with expanded left values
         */
        public <R> Stream<JoinedEntry<K, R, U>> flatMapLeft(final Function<? super T, ? extends Stream<? extends R>> valueMapper) {
            return valueMapper.apply(this.leftValue)
                    .map(r -> new JoinedEntry<>(this.key, r, this.rightValue));
        }

        /**
         * Expand the right value into multiple entries.
         * @param valueMapper function to expand the right value into a stream
         * @param <R> element type produced from the right value
         * @return stream of {@link JoinedEntry} with expanded right values
         */
        public <R> Stream<JoinedEntry<K, T, R>> flatMapRight(final Function<? super U, ? extends Stream<? extends R>> valueMapper) {
            return valueMapper.apply(this.rightValue)
                    .map(r -> new JoinedEntry<>(this.key, this.leftValue, r));
        }

        /**
         * Map both values at once.
         * @param leftMapper function to map the left value
         * @param rightMapper function to map the right value
         * @param <R> mapped left type
         * @param <S> mapped right type
         * @return new {@link JoinedEntry} with both values mapped
         */
        public <R, S> JoinedEntry<K, R, S> mapValues(final Function<T, R> leftMapper, final Function<U, S> rightMapper) {
            return new JoinedEntry<>(this.key, leftMapper.apply(this.leftValue), rightMapper.apply(this.rightValue));
        }
    }
}
