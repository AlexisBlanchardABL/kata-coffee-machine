import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CoffeeMachineTest {

    @InjectMocks
    private CoffeeMachine coffeeMachine = new CoffeeMachine();

    @Mock
    private DrinkMaker drinkMaker;

    @ParameterizedTest(name = "should send command {1} when ordering {0} ")
    @MethodSource({"parameters"})
    void ordering(Order order, String command) {
        coffeeMachine.order(order);
        verify(drinkMaker).receive(command);
    }

    static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of(new Order(Drink.COFFEE, 0, 1.0f), "C::"),
                Arguments.of(new Order(Drink.COFFEE, 1, 1.0f), "C:1:0"),
                Arguments.of(new Order(Drink.COFFEE, 2, 0.6f), "C:2:0"),
                Arguments.of(new Order(Drink.COFFEE, 0, 0.0f), "M:0.6€ is missing"),
                Arguments.of(new Order(Drink.COFFEE, 0, 0.3f), "M:0.3€ is missing"),
                Arguments.of(new Order(Drink.CHOCOLATE, 0, 1.0f), "H::"),
                Arguments.of(new Order(Drink.CHOCOLATE, 1, 1.0f), "H:1:0"),
                Arguments.of(new Order(Drink.CHOCOLATE, 2, 0.5f), "H:2:0"),
                Arguments.of(new Order(Drink.CHOCOLATE, 0, 0.0f), "M:0.5€ is missing"),
                Arguments.of(new Order(Drink.CHOCOLATE, 0, 0.4f), "M:0.1€ is missing"),
                Arguments.of(new Order(Drink.TEA, 0, 1.0f), "T::"),
                Arguments.of(new Order(Drink.TEA, 1, 1.0f), "T:1:0"),
                Arguments.of(new Order(Drink.TEA, 2, 0.4f), "T:2:0"),
                Arguments.of(new Order(Drink.TEA, 0, 0.0f), "M:0.4€ is missing"),
                Arguments.of(new Order(Drink.TEA, 0, 0.35f), "M:0.05€ is missing"),
                Arguments.of(new Order(Drink.ORANGE_JUICE, 0, 1.0f), "O::"),
                Arguments.of(new Order(Drink.ORANGE_JUICE, 0, 0.0f), "M:0.6€ is missing"),
                Arguments.of(new Order(Drink.ORANGE_JUICE, 0, 0.15f), "M:0.45€ is missing")
        );
    }

    @Test
    void should_deliver_given_message_to_drink_maker() {
        coffeeMachine.send("message-content");
        verify(drinkMaker).receive("M:message-content");
    }
}