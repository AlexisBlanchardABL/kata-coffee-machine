import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void given_one_sugar_a_stick_should_be_needed() {
        assertTrue(new Order(Drink.TEA, 1).isStickNeeded());
    }

    @Test
    void given_more_than_one_sugar_a_stick_should_be_needed() {
        assertTrue(new Order(Drink.TEA, 2).isStickNeeded());
    }

    @Test
    void given_no_sugar_no_stick_should_be_needed() {
        assertFalse(new Order(Drink.TEA, 0).isStickNeeded());
    }
}