package MineGamer.civilizationai.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundedListTest {

    @Test
    void rejectsNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new BoundedList<String>(0));
        assertThrows(IllegalArgumentException.class, () -> new BoundedList<String>(-1));
    }

    @Test
    void staysWithinCapacityAndEvictsOldestFirst() {
        BoundedList<Integer> list = new BoundedList<>(3);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4); // evicts 1
        list.add(5); // evicts 2

        assertEquals(3, list.size());
        assertEquals(3, list.capacity());
        assertEquals(java.util.List.of(3, 4, 5), list.asList());
    }

    @Test
    void isEmptyInitially() {
        BoundedList<String> list = new BoundedList<>(5);
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    void neverExceedsCapacityEvenWithManyAdds() {
        BoundedList<Integer> list = new BoundedList<>(10);
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }
        assertEquals(10, list.size());
        // The last 10 values added should be exactly what remains.
        assertEquals(990, list.asList().get(0));
        assertEquals(999, list.asList().get(9));
    }
}
