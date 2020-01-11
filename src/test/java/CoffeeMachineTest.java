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
                Arguments.of(new Order(Drink.COFFEE, 0), "C::"),
                Arguments.of(new Order(Drink.COFFEE, 1), "C:1:0"),
                Arguments.of(new Order(Drink.COFFEE, 2), "C:2:0"),
                Arguments.of(new Order(Drink.CHOCOLATE, 0), "H::"),
                Arguments.of(new Order(Drink.CHOCOLATE, 1), "H:1:0"),
                Arguments.of(new Order(Drink.CHOCOLATE, 2), "H:2:0"),
                Arguments.of(new Order(Drink.TEA, 0), "T::"),
                Arguments.of(new Order(Drink.TEA, 1), "T:1:0"),
                Arguments.of(new Order(Drink.TEA, 2), "T:2:0")
        );
    }

    @Test
    void should_deliver_given_message_to_drink_maker() {
        coffeeMachine.send("message-content");
        verify(drinkMaker).receive("M:message-content");
    }
}