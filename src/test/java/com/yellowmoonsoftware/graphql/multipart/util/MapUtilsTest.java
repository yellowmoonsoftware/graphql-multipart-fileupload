package com.yellowmoonsoftware.graphql.multipart.util;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MapUtilsTest {

    @Test
    void joinToStreamIncludesOnlySharedKeys() {
        final Map<String, String> left = new LinkedHashMap<>();
        left.put("one", "left-1");
        left.put("two", "left-2");
        final Map<String, String> right = new LinkedHashMap<>();
        right.put("two", "right-2");
        right.put("three", "right-3");

        final List<MapUtils.JoinedEntry<String, String, String>> joined = MapUtils.joinToStream(left, right).toList();

        assertThat(joined).hasSize(1);
        assertThat(joined.get(0).key()).isEqualTo("two");
        assertThat(joined.get(0).leftValue()).isEqualTo("left-2");
        assertThat(joined.get(0).rightValue()).isEqualTo("right-2");
    }

    @Test
    void joinedEntryMapsLeftAndRightValuesIndependently() {
        final MapUtils.JoinedEntry<String, Integer, String> entry = new MapUtils.JoinedEntry<>("k", 2, "value");

        final MapUtils.JoinedEntry<String, String, String> leftMapped = entry.mapLeft(i -> "left-" + (i * 2));
        final MapUtils.JoinedEntry<String, Integer, Integer> rightMapped = entry.mapRight(String::length);
        final MapUtils.JoinedEntry<String, String, Integer> bothMapped = entry.mapValues(Object::toString, String::length);

        assertThat(leftMapped.leftValue()).isEqualTo("left-4");
        assertThat(leftMapped.rightValue()).isEqualTo("value");
        assertThat(rightMapped.leftValue()).isEqualTo(2);
        assertThat(rightMapped.rightValue()).isEqualTo(5);
        assertThat(bothMapped.leftValue()).isEqualTo("2");
        assertThat(bothMapped.rightValue()).isEqualTo(5);
        assertThat(leftMapped.key()).isEqualTo("k");
        assertThat(rightMapped.key()).isEqualTo("k");
        assertThat(bothMapped.key()).isEqualTo("k");
    }

    @Test
    void joinedEntryFlatMapLeftExpandsEntries() {
        final MapUtils.JoinedEntry<String, List<Integer>, String> entry =
                new MapUtils.JoinedEntry<>("numbers", List.of(1, 2, 3), "right");

        final List<MapUtils.JoinedEntry<String, Integer, String>> expanded = entry.flatMapLeft(List::stream).toList();

        assertThat(expanded)
                .extracting(MapUtils.JoinedEntry::leftValue)
                .containsExactly(1, 2, 3);
        assertThat(expanded)
                .extracting(MapUtils.JoinedEntry::rightValue)
                .containsOnly("right");
        assertThat(expanded)
                .extracting(MapUtils.JoinedEntry::key)
                .containsOnly("numbers");
    }

    @Test
    void joinedEntryFlatMapRightExpandsEntries() {
        final MapUtils.JoinedEntry<String, String, List<String>> entry =
                new MapUtils.JoinedEntry<>("letters", "left", List.of("a", "b"));

        final List<MapUtils.JoinedEntry<String, String, String>> expanded = entry.flatMapRight(List::stream).toList();

        assertThat(expanded)
                .extracting(MapUtils.JoinedEntry::rightValue)
                .containsExactly("a", "b");
        assertThat(expanded)
                .extracting(MapUtils.JoinedEntry::leftValue)
                .containsOnly("left");
        assertThat(expanded)
                .extracting(MapUtils.JoinedEntry::key)
                .containsOnly("letters");
    }
}
