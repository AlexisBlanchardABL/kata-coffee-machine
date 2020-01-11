import static utils.MathUtils.subtractFloats;

public class CoffeeMachine {
    private DrinkMaker drinkMaker;

    public void order(Order order) {
        if (order.getDrink().costMoreThan(order.getMoneyAmount())) {
            send(subtractFloats(order.getDrink().getPrice(), order.getMoneyAmount()) + "€ is missing");
        }
        drinkMaker.receive(buildCommand(order));
    }

    private String buildCommand(Order order) {
        return order.getDrink().getCode()
                .concat(":")
                .concat(order.getSugar() == 0 ? "" : String.valueOf(order.getSugar()))
                .concat(":")
                .concat(order.isStickNeeded() ? "0" : "");
    }

    public void send(String message) {
        drinkMaker.receive("M:".concat(message));
    }
}
