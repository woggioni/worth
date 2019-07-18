package net.woggioni.worth.value;

import net.woggioni.worth.utils.Tuple2;
import net.woggioni.worth.xface.Value;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ObjectValueImplementationTest {

    private static List<Tuple2<ObjectValue.Implementation, Class<? extends ObjectValue>>> getImplementationMapping() {
        return Arrays.asList(
            new Tuple2<>(ObjectValue.Implementation.ArrayList, ListObjectValue.class),
            new Tuple2<>(ObjectValue.Implementation.TreeMap, TreeMapObjectValue.class),
            new Tuple2<>(ObjectValue.Implementation.HashMap, HashMapObjectValue.class),
            new Tuple2<>(ObjectValue.Implementation.LinkedHashMap, LinkedHashMapObjectValue.class)
        );
    }

    @Test
    public void test() {
        List<Tuple2<ObjectValue.Implementation, Class<? extends ObjectValue>>> mapping =
            getImplementationMapping();
        System.setProperty(ObjectValue.class.getName() + ".implementation",
            ObjectValue.Implementation.ArrayList.toString());
        ObjectValue.Implementation expectedImplementation =
            ObjectValue.Implementation.valueOf(System.getProperty(ObjectValue.class.getName() + ".implementation"));
        Class<? extends ObjectValue> expectedClass =
            mapping.stream().filter(t -> t._1 == expectedImplementation).findFirst().get()._2;
        ObjectValue obj = ObjectValue.newInstance();
        Assert.assertEquals(expectedClass, obj.getClass());
        mapping.forEach(tuple -> {
            Value.Configuration cfg = Value.Configuration.builder()
                    .objectValueImplementation(tuple._1)
                    .build();
            ObjectValue obj2 = ObjectValue.newInstance(cfg);
            Assert.assertEquals(tuple._2, obj2.getClass());
        });
    }

}
