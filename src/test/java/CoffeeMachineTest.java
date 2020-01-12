import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class CoffeeMachineTest {
    private final SalesRepository salesRepository = new SalesRepository();

    @InjectMocks
    private CoffeeMachine coffeeMachine = new CoffeeMachine(salesRepository);

    @Mock
    private DrinkMaker drinkMaker;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private PrintStream originalOut;

    @BeforeEach
    void setup() {
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreSystemOut() {
        System.setOut(originalOut);
    }

    @ParameterizedTest(name = "should send command {1} when ordering {0} ")
    @MethodSource({"parameters"})
    void ordering(Order order, String command) {
        assertDrinkCount(order.getDrink(), 0);
        coffeeMachine.order(order);
        verify(drinkMaker).receive(command);
        verifyNoMoreInteractions(drinkMaker);
        assertDrinkCount(order.getDrink(), !command.startsWith("M:") ? 1 : 0);
    }

    static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of(new Order(Drink.COFFEE, 0, 1.0f), "C::"),
                Arguments.of(new Order(Drink.COFFEE, 1, 1.0f), "C:1:0"),
                Arguments.of(new Order(Drink.COFFEE, 2, 0.6f), "C:2:0"),
                Arguments.of(new Order(Drink.CHOCOLATE, 0, 1.0f), "H::"),
                Arguments.of(new Order(Drink.CHOCOLATE, 1, 1.0f), "H:1:0"),
                Arguments.of(new Order(Drink.CHOCOLATE, 2, 0.5f), "H:2:0"),
                Arguments.of(new Order(Drink.TEA, 0, 1.0f), "T::"),
                Arguments.of(new Order(Drink.TEA, 1, 1.0f), "T:1:0"),
                Arguments.of(new Order(Drink.TEA, 2, 0.4f), "T:2:0"),
                Arguments.of(new Order(Drink.ORANGE_JUICE, 0, 1.0f), "O::"),

                Arguments.of(new Order(Drink.COFFEE, 0, 1.0f, true), "Ch::"),
                Arguments.of(new Order(Drink.CHOCOLATE, 1, 1.0f, true), "Hh:1:0"),
                Arguments.of(new Order(Drink.TEA, 2, 0.4f, true), "Th:2:0"),

                Arguments.of(new Order(Drink.COFFEE, 0, 0.0f), "M:0.6€ is missing"),
                Arguments.of(new Order(Drink.COFFEE, 0, 0.3f), "M:0.3€ is missing"),
                Arguments.of(new Order(Drink.CHOCOLATE, 0, 0.0f), "M:0.5€ is missing"),
                Arguments.of(new Order(Drink.CHOCOLATE, 0, 0.4f), "M:0.1€ is missing"),
                Arguments.of(new Order(Drink.TEA, 0, 0.0f), "M:0.4€ is missing"),
                Arguments.of(new Order(Drink.TEA, 0, 0.35f), "M:0.05€ is missing"),
                Arguments.of(new Order(Drink.ORANGE_JUICE, 0, 0.0f), "M:0.6€ is missing"),
                Arguments.of(new Order(Drink.ORANGE_JUICE, 0, 0.15f), "M:0.45€ is missing")
        );
    }

    @Test
    void should_deliver_given_message_to_drink_maker() {
        coffeeMachine.send("message-content");
        verify(drinkMaker).receive("M:message-content");
    }

    @Test
    void should_throw_an_exception_when_ordering_an_orange_juice_extra_hot() {
        assertThrows(IllegalArgumentException.class, () -> coffeeMachine.order(new Order(Drink.ORANGE_JUICE, 0, 1.0f, true)));
    }

    @Test
    void should_throw_an_exception_when_ordering_an_orange_juice_with_sugar() {
        assertThrows(IllegalArgumentException.class, () -> coffeeMachine.order(new Order(Drink.ORANGE_JUICE, 1, 1.0f)));
    }

    @Test
    void given_no_orders_were_made_when_displaying_the_report_it_should_print_it_on_the_console() {
        coffeeMachine.displayReport();
        assertReport(0, 0, 0, 0, 0.0f);
    }

    @Test
    void given_some_orders_were_made_when_displaying_the_report_it_should_print_it_on_the_console() {
        // Given
        orderA(Drink.COFFEE);
        orderA(Drink.COFFEE);
        orderA(Drink.TEA);
        orderA(Drink.TEA);
        orderA(Drink.ORANGE_JUICE);
        orderA(Drink.ORANGE_JUICE);
        orderA(Drink.CHOCOLATE);
        orderA(Drink.CHOCOLATE);

        // When
        coffeeMachine.displayReport();

        // Then
        assertReport(2, 2, 2, 2, 4.2f);
    }

    @Test
    void given_an_order_with_no_money_when_displaying_the_report_it_should_not_be_printed_in_the_report() {
        // Given
        coffeeMachine.order(new Order(Drink.COFFEE, 0, 0));

        // When
        coffeeMachine.displayReport();

        // Then
        assertReport(0, 0, 0, 0, 0f);
    }

    private void assertDrinkCount(Drink drink, int expected) {
        assertThat(salesRepository.getDrinkCount(drink)).isEqualTo(expected);
    }

    private void orderA(Drink drink) {
        coffeeMachine.order(new Order(drink, 0, drink.getPrice()));
    }

    private void assertReport(int teaCount, int chocolateCount, int coffeeCount, int orangeJuiceCount, float totalRevenue) {
        assertThat(outContent.toString())
                .contains("Beverage sales report:")
                .contains("TEA: " + teaCount)
                .contains("CHOCOLATE: " + chocolateCount)
                .contains("COFFEE: " + coffeeCount)
                .contains("ORANGE_JUICE: " + orangeJuiceCount)
                .contains("Total revenue: " + totalRevenue + "€");
    }
}