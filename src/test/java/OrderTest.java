import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    void aStickIsProvidedWhenOneOrMoreSugarIsOrdered() {
        assertTrue(Order.of(Drink.TEA, 1, 1.0f).isStickNeeded());
        assertTrue(Order.of(Drink.TEA, 2, 1.0f).isStickNeeded());
    }

    @Test
    void noStickProvided() {
        assertFalse(Order.of(Drink.TEA, 0, 1.0f).isStickNeeded());
    }

    @Test
    void orderingAnOrangeJuiceExtraHot() {
        assertThrows(IllegalArgumentException.class, () -> Order.of(Drink.ORANGE_JUICE, 0, 1.0f, true));
    }

    @Test
    void orderingAnOrangeJuiceWithSugar() {
        assertThrows(IllegalArgumentException.class, () -> Order.of(Drink.ORANGE_JUICE, 1, 1.0f));
    }

}
