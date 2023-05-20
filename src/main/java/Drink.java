public interface Drink {

    Liquid getBase();

    float missingAmount(float moneyAmount);

    float pricePlus(float amount);

    String drinkInstruction(boolean extraHot);

    static boolean hasLiquidBase(Drink drink) {
        return !Liquid.NONE.equals(drink.getBase());
    }

}
