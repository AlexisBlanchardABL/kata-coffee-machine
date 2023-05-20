import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    void aStickIsProvidedWhenOneOrMoreSugarIsOrdered() {
        assertTrue(Order.of(HotDrink.TEA, 1, 1.0f).isStickNeeded());
        assertTrue(Order.of(HotDrink.TEA, 2, 1.0f).isStickNeeded());
    }

    @Test
    void noStickProvided() {
        assertFalse(Order.of(HotDrink.TEA, 0, 1.0f).isStickNeeded());
    }

    @Test
    void orderingAnOrangeJuiceWithSugar() {
        assertThrows(IllegalArgumentException.class, () -> Order.of(ColdDrink.ORANGE_JUICE, 1, 1.0f));
    }

}
