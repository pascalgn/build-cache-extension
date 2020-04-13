import module2.Counter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CounterTest {
    @Test
    public void testIncrement() {
        Counter counter = new Counter();
        counter.increment();
        counter.increment();
        assertEquals(2, counter.getValue());
    }
}
