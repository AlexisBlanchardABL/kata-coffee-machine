import java.util.EnumSet;

import static utils.MathUtils.subtractFloats;

public class CoffeeMachine {
    private DrinkMaker drinkMaker;
    private final SalesRepository salesRepository;

    public CoffeeMachine(SalesRepository salesRepository) {
        this.salesRepository = salesRepository;
    }

    public void order(Order order) {
        if (Drink.ORANGE_JUICE.equals(order.getDrink()) && (order.isExtraHot() || order.getSugar() > 0)) {
            throw new IllegalArgumentException("You won't dare.. Will you?");
        }

        if (order.getDrink().costMoreThan(order.getMoneyAmount())) {
            send(subtractFloats(order.getDrink().getPrice(), order.getMoneyAmount()) + "€ is missing");
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
        EnumSet.allOf(Drink.class).forEach((drink) ->
                System.out.println(drink.name() + ": " + salesRepository.getDrinkCount(drink))
        );
        System.out.println("Total revenue: " + salesRepository.getEarnedMoney() + "€");
    }

}
