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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoffeeMachineTest {
    private static final String SHORTAGE_NOTIFICATION_MESSAGE = " shortage, a notification has been sent to the maintenance company";

    private final SalesRepository salesRepository = new SalesRepository();

    @InjectMocks
    private CoffeeMachine coffeeMachine = new CoffeeMachine(salesRepository);

    @Mock
    private DrinkMaker drinkMaker;
    @Mock
    private BeverageQuantityChecker beverageQuantityChecker;
    @Mock
    private EmailNotifier emailNotifier;

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

    @ParameterizedTest(name = "when ordering {0} with shortage({1}), it should send command {2}")
    @MethodSource({"parameters"})
    void ordering(Order order, boolean shortage, String command) {
        mockBeverageQuantityChecker(order, shortage);
        assertDrinkCount(order.getDrink(), 0);
        coffeeMachine.order(order);
        verifyEmailNotifierInteractions(shortage, order.getDrink());
        verify(drinkMaker).receive(command);
        verifyNoMoreInteractions(drinkMaker);
        assertDrinkCount(order.getDrink(), !command.startsWith("M:") ? 1 : 0);
    }

    static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of(new Order(Drink.COFFEE, 0, 1.0f), false, "C::"),
                Arguments.of(new Order(Drink.COFFEE, 1, 1.0f), false, "C:1:0"),
                Arguments.of(new Order(Drink.COFFEE, 2, 0.6f), false, "C:2:0"),
                Arguments.of(new Order(Drink.CHOCOLATE, 0, 1.0f), false, "H::"),
                Arguments.of(new Order(Drink.CHOCOLATE, 1, 1.0f), false, "H:1:0"),
                Arguments.of(new Order(Drink.CHOCOLATE, 2, 0.5f), false, "H:2:0"),
                Arguments.of(new Order(Drink.TEA, 0, 1.0f), false, "T::"),
                Arguments.of(new Order(Drink.TEA, 1, 1.0f), false, "T:1:0"),
                Arguments.of(new Order(Drink.TEA, 2, 0.4f), false, "T:2:0"),
                Arguments.of(new Order(Drink.ORANGE_JUICE, 0, 1.0f), false, "O::"),

                Arguments.of(new Order(Drink.COFFEE, 0, 1.0f, true), false, "Ch::"),
                Arguments.of(new Order(Drink.CHOCOLATE, 1, 1.0f, true), false, "Hh:1:0"),
                Arguments.of(new Order(Drink.TEA, 2, 0.4f, true), false, "Th:2:0"),

                Arguments.of(new Order(Drink.CHOCOLATE, 3, 0.5f), true, "M:MILK" + SHORTAGE_NOTIFICATION_MESSAGE),
                Arguments.of(new Order(Drink.TEA, 3, 0.4f), true, "M:WATER" + SHORTAGE_NOTIFICATION_MESSAGE),
                Arguments.of(new Order(Drink.COFFEE, 0, 0.0f), false, "M:0.6€ is missing"),
                Arguments.of(new Order(Drink.COFFEE, 0, 0.3f), false, "M:0.3€ is missing"),
                Arguments.of(new Order(Drink.CHOCOLATE, 0, 0.0f), false, "M:0.5€ is missing"),
                Arguments.of(new Order(Drink.CHOCOLATE, 0, 0.4f), false, "M:0.1€ is missing"),
                Arguments.of(new Order(Drink.TEA, 0, 0.0f), false, "M:0.4€ is missing"),
                Arguments.of(new Order(Drink.TEA, 0, 0.35f), false, "M:0.05€ is missing"),
                Arguments.of(new Order(Drink.ORANGE_JUICE, 0, 0.0f), false, "M:0.6€ is missing"),
                Arguments.of(new Order(Drink.ORANGE_JUICE, 0, 0.15f), false, "M:0.45€ is missing")
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
        when(beverageQuantityChecker.isEmpty(anyString())).thenReturn(false);

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

    private void mockBeverageQuantityChecker(Order order, boolean shortage) {
        Drink drink = order.getDrink();
        if (Drink.ORANGE_JUICE != drink && !drink.costMoreThan(order.getMoneyAmount())) {
            when(beverageQuantityChecker.isEmpty(drink.getBase().name())).thenReturn(shortage);
        }
    }

    private void verifyEmailNotifierInteractions(boolean shortage, Drink drink) {
        if (shortage) {
            verify(emailNotifier).notifyMissingDrink(drink.getBase().name());
            verifyNoMoreInteractions(emailNotifier);
        } else {
            verifyNoInteractions(emailNotifier);
        }
    }

}