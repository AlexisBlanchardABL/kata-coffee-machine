public class CoffeeMachine {
    private DrinkMaker drinkMaker;

    public void order(Order order) {
        String command = order.getDrink().getCode()
                .concat(":")
                .concat(order.getSugar() == 0 ? "" : String.valueOf(order.getSugar()))
                .concat(":")
                .concat(order.isStickNeeded() ? "0" : "");
        drinkMaker.receive(command);
    }

    public void send(String message) {
        drinkMaker.receive("M:".concat(message));
    }
}
