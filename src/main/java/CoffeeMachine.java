import java.util.EnumSet;
import java.util.Objects;

import static utils.MathUtils.subtractFloats;

public class CoffeeMachine {
    private static final String SHORTAGE_NOTIFICATION_MESSAGE = " shortage, a notification has been sent to the maintenance company";

    private final SalesRepository salesRepository;
    private DrinkMaker drinkMaker;
    private BeverageQuantityChecker beverageQuantityChecker;
    private EmailNotifier emailNotifier;

    public CoffeeMachine(SalesRepository salesRepository) {
        this.salesRepository = salesRepository;
    }

    public void order(Order order) {
        Drink drink = order.getDrink();
        if (Drink.ORANGE_JUICE.equals(drink) && (order.isExtraHot() || order.getSugar() > 0)) {
            throw new IllegalArgumentException("You won't dare.. Will you?");
        }

        if (drink.costMoreThan(order.getMoneyAmount())) {
            send(subtractFloats(drink.getPrice(), order.getMoneyAmount()) + "€ is missing");
            return;
        }

        Liquid waterOrMilk = drink.getBase();
        if (isShortageIssue(waterOrMilk)) {
            emailNotifier.notifyMissingDrink(waterOrMilk.name());
            drinkMaker.receive("M:" + waterOrMilk.name() + SHORTAGE_NOTIFICATION_MESSAGE);
            return;
        }

        salesRepository.save(order);
        drinkMaker.receive(buildCommand(order));
    }

    private String buildCommand(Order order) {
        return order.getDrink().getCode()
                .concat(order.isExtraHot() ? "h" : "")
                .concat(":")
                .concat(order.getSugar() == 0 ? "" : String.valueOf(order.getSugar()))
                .concat(":")
                .concat(order.isStickNeeded() ? "0" : "");
    }

    public void send(String message) {
        drinkMaker.receive("M:".concat(message));
    }

    public void displayReport() {
        System.out.println("Beverage sales report:");
        EnumSet.allOf(Drink.class).forEach((drink) -> System.out.println(drink.name() + ": " + salesRepository.getDrinkCount(drink)));
        System.out.println("Total revenue: " + salesRepository.getEarnedMoney() + "€");
    }

    private boolean isShortageIssue(Liquid base) {
        return Objects.nonNull(base) && beverageQuantityChecker.isEmpty(base.name());
    }

}
