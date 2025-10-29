package com.yellowmoonsoftware.graphql.multipart.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NullObjectGraphTraverserTest {
    final NullObjectGraphTraverser nullMapper = NullObjectGraphTraverser.INSTANCE;

    @Test
    void testDereferenceWithNullPath() {
        ObjectGraphTraverser result = nullMapper.dereference(null);
        assertThat(result).isSameAs(NullObjectGraphTraverser.INSTANCE);
    }

    @Test
    void testDereferenceWithEmptyPath() {
        ObjectGraphTraverser result = nullMapper.dereference("");
        assertThat(result).isSameAs(NullObjectGraphTraverser.INSTANCE);
    }

    @Test
    void testDereferenceReturnsSameInstanceAcrossCalls() {
        ObjectGraphTraverser result1 = nullMapper.dereference("one");
        ObjectGraphTraverser result2 = nullMapper.dereference("two");

        assertThat(result1)
                .isSameAs(result2)
                .isSameAs(NullObjectGraphTraverser.INSTANCE);
    }

    @Test
    void testGetWithNullPath() {
        final Object result = nullMapper.get((String)null);
        assertThat(result).isNull();
    }

    @Test
    void testGetWithEmptyPath() {
        final Object result = nullMapper.get("");
        assertThat(result).isNull();
    }

    @Test
    void testSetReturnsNull() {
        assertThat(nullMapper.set("foo", "bar")).isNull();
        assertThat(nullMapper.set("foo", "bash")).isNull();
    }

    @Test
    void testSetWithObjectGraphPathReturnsNull() {
        final ObjectGraphPath path = ObjectGraphPath.from("variables.user.name");

        assertThat(nullMapper.set(path, "value")).isNull();
    }

    @Test
    void testSetWithObjectGraphPathAndEmptyValueReturnsNull() {
        final ObjectGraphPath path = ObjectGraphPath.from("variables.user.name");

        assertThat(nullMapper.set(path, "")).isNull();
    }

    @Test
    void testSetWithObjectGraphPathAndNullValueReturnsNull() {
        final ObjectGraphPath path = ObjectGraphPath.from("variables.user.name");

        assertThat(nullMapper.set(path, (Object)null)).isNull();
    }

    @Test
    void testGetWithObjectGraphPathReturnsNull() {
        final ObjectGraphPath path = ObjectGraphPath.from("variables.user.name");

        assertThat((Object) nullMapper.get(path)).isNull();
    }

    @Test
    void testGetWithNullObjectGraphPathReturnsNull() {
        assertThat((Object) nullMapper.get((ObjectGraphPath)null)).isNull();
    }

    @Test
    void testEqualsMethod() {
        assertThat(nullMapper.equals(NullObjectGraphTraverser.INSTANCE)).isTrue();
        assertThat(nullMapper.equals(new Object())).isFalse();
    }

    @Test
    void testHashCode() {
        int hashCode1 = nullMapper.hashCode();
        int hashCode2 = NullObjectGraphTraverser.INSTANCE.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }
}
