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
    private static final BeverageQuantityChecker NO_BEVERAGE_SHORTAGE = drink -> false;
    private static final BeverageQuantityChecker BEVERAGE_SHORTAGE = drink -> true;

    private CoffeeMachine coffeeMachine;

    @Mock
    private DrinkMaker drinkMaker;
    @Mock
    private EmailNotifier emailNotifier;
    @Mock
    private Printer printer;

    @BeforeEach
    void setup() {
        coffeeMachine = new CoffeeMachine(
                new SalesRepository(),
                drinkMaker,
                NO_BEVERAGE_SHORTAGE,
                emailNotifier,
                printer
        );
    }

    static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of(Order.of(Drink.COFFEE, 0, 1.0f), "C::"),
                Arguments.of(Order.of(Drink.COFFEE, 1, 1.0f), "C:1:0"),
                Arguments.of(Order.of(Drink.COFFEE, 2, 0.6f), "C:2:0"),
                Arguments.of(Order.of(Drink.CHOCOLATE, 0, 1.0f), "H::"),
                Arguments.of(Order.of(Drink.CHOCOLATE, 1, 1.0f), "H:1:0"),
                Arguments.of(Order.of(Drink.CHOCOLATE, 2, 0.5f), "H:2:0"),
                Arguments.of(Order.of(Drink.TEA, 0, 1.0f), "T::"),
                Arguments.of(Order.of(Drink.TEA, 1, 1.0f), "T:1:0"),
                Arguments.of(Order.of(Drink.TEA, 2, 0.4f), "T:2:0"),
                Arguments.of(Order.of(Drink.ORANGE_JUICE, 0, 1.0f), "O::"),

                Arguments.of(Order.of(Drink.COFFEE, 0, 1.0f, true), "Ch::"),
                Arguments.of(Order.of(Drink.CHOCOLATE, 1, 1.0f, true), "Hh:1:0"),
                Arguments.of(Order.of(Drink.TEA, 2, 0.4f, true), "Th:2:0"),

                Arguments.of(Order.of(Drink.COFFEE, 0, 0.0f), "M:0.6€ is missing"),
                Arguments.of(Order.of(Drink.COFFEE, 0, 0.3f), "M:0.3€ is missing"),
                Arguments.of(Order.of(Drink.CHOCOLATE, 0, 0.0f), "M:0.5€ is missing"),
                Arguments.of(Order.of(Drink.CHOCOLATE, 0, 0.4f), "M:0.1€ is missing"),
                Arguments.of(Order.of(Drink.TEA, 0, 0.0f), "M:0.4€ is missing"),
                Arguments.of(Order.of(Drink.TEA, 0, 0.35f), "M:0.05€ is missing"),
                Arguments.of(Order.of(Drink.ORANGE_JUICE, 0, 0.0f), "M:0.6€ is missing"),
                Arguments.of(Order.of(Drink.ORANGE_JUICE, 0, 0.15f), "M:0.45€ is missing")
        );
    }

    @ParameterizedTest(name = "when ordering {0}, it should send command {1}")
    @MethodSource({"parameters"})
    void ordering(Order order, String command) {
        coffeeMachine.order(order);
        verifyNoInteractions(emailNotifier);
        verify(drinkMaker).receive(command);
        verifyNoMoreInteractions(drinkMaker);
    }


    static Stream<Arguments> shortageParameters() {
        return Stream.of(
                Arguments.of(Order.of(Drink.CHOCOLATE, 3, 0.5f), "M:MILK" + SHORTAGE_NOTIFICATION_MESSAGE),
                Arguments.of(Order.of(Drink.TEA, 3, 0.4f), "M:WATER" + SHORTAGE_NOTIFICATION_MESSAGE)
        );
    }

    @ParameterizedTest(name = "when ordering {0}, and there is a shortage of the base, it should send command {1}")
    @MethodSource({"shortageParameters"})
    void orderingWhenThereIsAShortage(Order order, String command) {
        coffeeMachine = new CoffeeMachine(
                new SalesRepository(),
                drinkMaker,
                BEVERAGE_SHORTAGE,
                emailNotifier,
                printer
        );
        coffeeMachine.order(order);
        verifyMissingDrinkNotification(order.getDrink());
        verify(drinkMaker).receive(command);
        verifyNoMoreInteractions(drinkMaker);
    }

    @Test
    void emptyReportWhenNoOrderWasMade() {
        coffeeMachine.displayReport();
        assertReport(0, 0, 0, 0, 0.0f);
    }

    @Test
    void shouldPrintReport() {
        // Given
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

    private void verifyMissingDrinkNotification(Drink drink) {
        verify(emailNotifier).notifyMissingDrink(drink.getBase().name());
        verifyNoMoreInteractions(emailNotifier);
    }

}
