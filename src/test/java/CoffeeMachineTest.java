import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoffeeMachineTest {
    private static final String SHORTAGE_NOTIFICATION_MESSAGE = " shortage, a notification has been sent to the maintenance company";

    private CoffeeMachine coffeeMachine;

    @Mock
    private DrinkMaker drinkMaker;
    @Mock
    private BeverageQuantityChecker beverageQuantityChecker;
    @Mock
    private EmailNotifier emailNotifier;
    @Mock
    private Printer printer;

    @BeforeEach
    void setup() {
        coffeeMachine = new CoffeeMachine(
                new SalesRepository(),
                drinkMaker,
                beverageQuantityChecker,
                emailNotifier,
                printer
        );
    }

    @ParameterizedTest(name = "when ordering {0} with shortage({1}), it should send command {2}")
    @MethodSource({"parameters"})
    void ordering(Order order, boolean shortage, String command) {
        mockBeverageQuantityChecker(order, shortage);
        coffeeMachine.order(order);
        verifyEmailNotifierInteractions(shortage, order.getDrink());
        verify(drinkMaker).receive(command);
        verifyNoMoreInteractions(drinkMaker);
    }

    static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of(Order.of(Drink.COFFEE, 0, 1.0f), false, "C::"),
                Arguments.of(Order.of(Drink.COFFEE, 1, 1.0f), false, "C:1:0"),
                Arguments.of(Order.of(Drink.COFFEE, 2, 0.6f), false, "C:2:0"),
                Arguments.of(Order.of(Drink.CHOCOLATE, 0, 1.0f), false, "H::"),
                Arguments.of(Order.of(Drink.CHOCOLATE, 1, 1.0f), false, "H:1:0"),
                Arguments.of(Order.of(Drink.CHOCOLATE, 2, 0.5f), false, "H:2:0"),
                Arguments.of(Order.of(Drink.TEA, 0, 1.0f), false, "T::"),
                Arguments.of(Order.of(Drink.TEA, 1, 1.0f), false, "T:1:0"),
                Arguments.of(Order.of(Drink.TEA, 2, 0.4f), false, "T:2:0"),
                Arguments.of(Order.of(Drink.ORANGE_JUICE, 0, 1.0f), false, "O::"),

                Arguments.of(Order.of(Drink.COFFEE, 0, 1.0f, true), false, "Ch::"),
                Arguments.of(Order.of(Drink.CHOCOLATE, 1, 1.0f, true), false, "Hh:1:0"),
                Arguments.of(Order.of(Drink.TEA, 2, 0.4f, true), false, "Th:2:0"),

                Arguments.of(Order.of(Drink.CHOCOLATE, 3, 0.5f), true, "M:MILK" + SHORTAGE_NOTIFICATION_MESSAGE),
                Arguments.of(Order.of(Drink.TEA, 3, 0.4f), true, "M:WATER" + SHORTAGE_NOTIFICATION_MESSAGE),
                Arguments.of(Order.of(Drink.COFFEE, 0, 0.0f), false, "M:0.6€ is missing"),
                Arguments.of(Order.of(Drink.COFFEE, 0, 0.3f), false, "M:0.3€ is missing"),
                Arguments.of(Order.of(Drink.CHOCOLATE, 0, 0.0f), false, "M:0.5€ is missing"),
                Arguments.of(Order.of(Drink.CHOCOLATE, 0, 0.4f), false, "M:0.1€ is missing"),
                Arguments.of(Order.of(Drink.TEA, 0, 0.0f), false, "M:0.4€ is missing"),
                Arguments.of(Order.of(Drink.TEA, 0, 0.35f), false, "M:0.05€ is missing"),
                Arguments.of(Order.of(Drink.ORANGE_JUICE, 0, 0.0f), false, "M:0.6€ is missing"),
                Arguments.of(Order.of(Drink.ORANGE_JUICE, 0, 0.15f), false, "M:0.45€ is missing")
        );
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

        Drink[] drinks = {Drink.COFFEE,
                Drink.COFFEE,
                Drink.TEA,
                Drink.TEA,
                Drink.ORANGE_JUICE,
                Drink.ORANGE_JUICE,
                Drink.CHOCOLATE,
                Drink.CHOCOLATE};
        order(
                drinks
        );

        // When
        coffeeMachine.displayReport();

        // Then
        assertReport(2, 2, 2, 2, 4.2f);
    }

    @Test
    void orderWithNoAmount() {
        // Given
        coffeeMachine.order(Order.of(Drink.COFFEE, 0, 0));

        // When
        coffeeMachine.displayReport();

        // Then
        assertReport(0, 0, 0, 0, 0f);
        verify(drinkMaker).receive("M:0.6€ is missing");
    }

    private void order(Drink ...drinks) {
        for (Drink drink : drinks) {
            coffeeMachine.order(Order.of(drink, 0, drink.getPrice()));
        }
    }

    private void assertReport(int teaCount, int chocolateCount, int coffeeCount, int orangeJuiceCount, float totalRevenue) {
        verify(printer).print("Beverage sales report:");
        verify(printer).print("TEA: " + teaCount);
        verify(printer).print("CHOCOLATE: " + chocolateCount);
        verify(printer).print("COFFEE: " + coffeeCount);
        verify(printer).print("ORANGE_JUICE: " + orangeJuiceCount);
        verify(printer).print("Total revenue: " + totalRevenue + "€");
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
