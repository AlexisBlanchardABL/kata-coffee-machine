import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Stream;

public class CoffeeMachine {
    private static final String SHORTAGE_NOTIFICATION_MESSAGE = "{0} shortage, a notification has been sent to the maintenance company";

    private final SalesRepository salesRepository;
    private final DrinkMaker drinkMaker;
    private final BeverageQuantityChecker beverageQuantityChecker;
    private final EmailNotifier emailNotifier;
    private final Printer printer;

    public CoffeeMachine(
            SalesRepository salesRepository,
            DrinkMaker drinkMaker,
            BeverageQuantityChecker beverageQuantityChecker,
            EmailNotifier emailNotifier,
            Printer printer
    ) {
        this.salesRepository = salesRepository;
        this.drinkMaker = drinkMaker;
        this.beverageQuantityChecker = beverageQuantityChecker;
        this.emailNotifier = emailNotifier;
        this.printer = printer;
    }

    public void order(Order order) {
        Drink drink = order.getDrink();

        if (order.missingAmount() > 0) {
            displayMessage(order.missingAmount() + "€ is missing");
            return;
        }

        Liquid waterOrMilk = drink.getBase();
        if (isShortageIssue(waterOrMilk)) {
            emailNotifier.notifyMissingDrink(waterOrMilk.name());
            displayMessage(MessageFormat.format(SHORTAGE_NOTIFICATION_MESSAGE, waterOrMilk.name()));
            return;
        }

        salesRepository.save(order.getDrink());
        drinkMaker.receive(order.buildInstruction());
    }

    private void displayMessage(String message) {
        drinkMaker.receive("M:".concat(message));
    }

    public void displayReport() {
        printer.print("Beverage sales report:");
        Stream.concat(
                EnumSet.allOf(ColdDrink.class).stream(),
                EnumSet.allOf(HotDrink.class).stream()
        )
                .toList()
                .forEach((drink) -> printer.print(drink.name() + ": " + salesRepository.getDrinkCount(drink)));
        printer.print("Total revenue: " + salesRepository.getEarnedMoney() + "€");
    }

    private boolean isShortageIssue(Liquid base) {
        return Objects.nonNull(base) && beverageQuantityChecker.isEmpty(base.name());
    }

}
