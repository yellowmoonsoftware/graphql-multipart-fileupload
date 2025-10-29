package com.yellowmoonsoftware.graphql.multipart.util;

import graphql.com.google.common.collect.Lists;
import graphql.com.google.common.collect.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MapListGraphTraverserTest {
    Map<String, Object> aMap;
    List<Object> aList;
    Map<String, Object> baseMap;

    ObjectGraphTraverser objectGraphMapper;

    @BeforeEach
    void setup() {
        aMap = Maps.newLinkedHashMap();
        aMap.put("baz", "qux");
        aMap.put("fish", null);
        baseMap = Maps.newLinkedHashMap();
        baseMap.put("foo", null);
        baseMap.put("a-map", aMap);
        aList = Lists.newLinkedList();
        aList.add("list-item-1");
        aList.add(null);
        baseMap.put("a-list", aList);

        objectGraphMapper = MapListGraphTraverser.wrap(baseMap);
    }

    @Test
    void testSetValueAppliesToUnderlyingMap() {
        objectGraphMapper.set("foo", "fizz");

        assertThat(baseMap.get("foo")).isEqualTo("fizz");
    }

    @Test
    void testSetValueToNonExistentKeyDoesNotCreateInUnderlyingMap() {
        objectGraphMapper.set("flash", "bulb");

        assertThat(baseMap.containsKey("flash")).isFalse();
    }

    @Test
    void testSetValueAppliesToUnderlyingList() {
        objectGraphMapper.dereference("a-list")
                .set("1", "buzz");
        assertThat(aList.get(0)).isEqualTo("list-item-1");
        assertThat(aList.get(1)).isEqualTo("buzz");
    }

    @Test
    void testSetValueWithOutOfRangeOrNonNumericIndexOnMapperWithUnderlyingListDoesNotThrowException() {
        assertThatCode(() -> objectGraphMapper.dereference("a-list").set("-2", "buzz"))
                .doesNotThrowAnyException();
        assertThat(aList.size()).isEqualTo(2);
        assertThat(aList.get(0)).isEqualTo("list-item-1");
        assertThat(aList.get(1)).isNull();

        assertThatCode(() -> objectGraphMapper.dereference("a-list").set("11", "buzz"))
                .doesNotThrowAnyException();
        assertThat(aList.size()).isEqualTo(2);
        assertThat(aList.get(0)).isEqualTo("list-item-1");
        assertThat(aList.get(1)).isNull();

        assertThatCode(() -> objectGraphMapper.dereference("a-list").set("non-numeric", "buzz"))
                .doesNotThrowAnyException();
        assertThat(aList.size()).isEqualTo(2);
        assertThat(aList.get(0)).isEqualTo("list-item-1");
        assertThat(aList.get(1)).isNull();
    }

    @Test
    void testGetOnUnderlyingListReturnsTheListItem() {
        assertThat(objectGraphMapper.dereference("a-list").<String>get("0")).isEqualTo("list-item-1");
    }

    @Test
    void testGetOnUnderlyingListReturnsNullIfIndexOutOfBoundsOrNonInteger() {
        assertThat(objectGraphMapper.dereference("a-list").<String>get("foo")).isNull();

        assertThat(objectGraphMapper.dereference("a-list").<String>get("99")).isNull();
    }

    @Test
    void testGetOnUnderlyingMapReturnsTheEntryValue() {
        assertThat(objectGraphMapper.dereference("a-map").<String>get("baz")).isEqualTo("qux");
    }

    @Test
    void testSetWithObjectGraphPathUpdatesNestedValue() {
        final ObjectGraphPath mapPath = ObjectGraphPath.from("a-map.baz");
        final ObjectGraphPath listPath = ObjectGraphPath.from("a-list.1");

        objectGraphMapper.set(mapPath, "updated");
        objectGraphMapper.set(listPath, "new-list-item");

        assertThat(aMap.get("baz")).isEqualTo("updated");
        assertThat(aList.get(1)).isEqualTo("new-list-item");
    }

    @Test
    void testGetWithObjectGraphPathReturnsValue() {
        final ObjectGraphPath mapPath = ObjectGraphPath.from("a-map.baz");
        final ObjectGraphPath listPath = ObjectGraphPath.from("a-list.0");

        assertThat(objectGraphMapper.<String>get(mapPath)).isEqualTo("qux");
        assertThat(objectGraphMapper.<String>get(listPath)).isEqualTo("list-item-1");
    }

    @Test
    void testGetOnUnderlyingMapReturnsNullIfKeyDoesNotExist() {
        assertThat(objectGraphMapper.dereference("a-list").<String>get("bin-bash")).isNull();
    }

    @Test
    void testGetOnUnderlyingComplexObjectReturnsTheObject() {
        assertThat(objectGraphMapper.<List<?>>get("a-list")).isInstanceOf(List.class);
        assertThat(objectGraphMapper.<Map<?,?>>get("a-map")).isInstanceOf(Map.class);
    }

    @Test
    void testDereferenceOnMapMemberReturnsMapper() {
        final ObjectGraphTraverser newMapper = objectGraphMapper.dereference("a-map");
        assertThat(newMapper).isNotEqualTo(NullObjectGraphTraverser.INSTANCE);
    }

    @Test
    void testDereferenceOnListMemberReturnsMapper() {
        final ObjectGraphTraverser newMapper = objectGraphMapper.dereference("a-list");
        assertThat(newMapper).isNotEqualTo(NullObjectGraphTraverser.INSTANCE);
    }

    @Test
    void testDereferenceOnNonListOrMapMemberReturnsNullMapper() {
        final ObjectGraphTraverser newMapper = objectGraphMapper.dereference("foo");
        assertThat(newMapper).isEqualTo(NullObjectGraphTraverser.INSTANCE);
    }

    @Test
    void testDereferenceOnNonExistentKeyReturnsNullMapper() {
        final ObjectGraphTraverser newMapper = objectGraphMapper.dereference("non-existent-key");
        assertThat(newMapper).isEqualTo(NullObjectGraphTraverser.INSTANCE);
    }

    @Test
    void testDereferenceOnNullKeyReturnsNullMapper() {
        final ObjectGraphTraverser newMapper = objectGraphMapper.dereference(null);
        assertThat(newMapper).isEqualTo(NullObjectGraphTraverser.INSTANCE);
    }

    @Test
    void testWrapOnNonMapOrListValueReturnsNullMapper() {
        assertThat(MapListGraphTraverser.wrap(null))
                .isEqualTo(NullObjectGraphTraverser.INSTANCE);
        assertThat(MapListGraphTraverser.wrap("a-string"))
                .isEqualTo(NullObjectGraphTraverser.INSTANCE);
        assertThat(MapListGraphTraverser.wrap(42))
                .isEqualTo(NullObjectGraphTraverser.INSTANCE);
        assertThat(MapListGraphTraverser.wrap(false))
                .isEqualTo(NullObjectGraphTraverser.INSTANCE);
    }
}
