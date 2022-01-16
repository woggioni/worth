package net.woggioni.wson.value;

import net.woggioni.jwo.Tuple2;
import net.woggioni.wson.xface.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class ObjectValueImplementationTest {

    private static List<Tuple2<ObjectValue.Implementation, Class<? extends ObjectValue>>> getImplementationMapping() {
        return Arrays.asList(
            Tuple2.newInstance(ObjectValue.Implementation.ArrayList, ListObjectValue.class),
            Tuple2.newInstance(ObjectValue.Implementation.TreeMap, TreeMapObjectValue.class),
            Tuple2.newInstance(ObjectValue.Implementation.HashMap, HashMapObjectValue.class),
            Tuple2.newInstance(ObjectValue.Implementation.LinkedHashMap, LinkedHashMapObjectValue.class)
        );
    }

    @Test
    public void test() {
        List<Tuple2<ObjectValue.Implementation, Class<? extends ObjectValue>>> mapping =
            getImplementationMapping();
        ObjectValue.Implementation expectedImplementation =
            ObjectValue.Implementation.valueOf(System.getProperty(ObjectValue.class.getName() + ".implementation", "TreeMap"));
        Class<? extends ObjectValue> expectedClass =
            mapping.stream().filter(t -> t.get_1() == expectedImplementation).findFirst().get().get_2();
        ObjectValue obj = ObjectValue.newInstance();
        Assertions.assertEquals(expectedClass, obj.getClass());
        mapping.forEach(tuple -> {
            Value.Configuration cfg = Value.Configuration.builder()
                    .objectValueImplementation(tuple.get_1())
                    .build();
            ObjectValue obj2 = ObjectValue.newInstance(cfg);
            Assertions.assertEquals(tuple.get_2(), obj2.getClass());
        });
    }

}
