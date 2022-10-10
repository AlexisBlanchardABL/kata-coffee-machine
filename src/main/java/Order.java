import static utils.MathUtils.subtractFloats;

public class Order {
    private final Drink drink;
    private final int sugar;
    private final float moneyAmount;
    private final boolean extraHot;

    public static Order of(Drink drink, int sugar, float moneyAmount) {
        return new Order(drink, sugar, moneyAmount);
    }

    public static Order of(Drink drink, int sugar, float moneyAmount, boolean extraHot) {
        return new Order(drink, sugar, moneyAmount, extraHot);
    }

    private Order(Drink drink, int sugar, float moneyAmount) {
        this(drink, sugar, moneyAmount, false);
    }

    private Order(Drink drink, int sugar, float moneyAmount, boolean extraHot) {
        this.drink = drink;
        this.sugar = sugar;
        this.moneyAmount = moneyAmount;
        this.extraHot = extraHot;

        if (Drink.ORANGE_JUICE.equals(drink) && (this.extraHot || this.sugar > 0)) {
            throw new IllegalArgumentException("You won't dare.. Will you?");
        }
    }

    boolean isStickNeeded() {
        return sugar > 0;
    }

    public float getMoneyAmount() {
        return moneyAmount;
    }

    public Drink getDrink() {
        return drink;
    }


    @Override
    public String toString() {
        return "a " + drink + " with " +
                (sugar > 1 ? sugar + " sugars" :
                        sugar == 1 ? "1 sugar" : "no sugar");
    }

    String buildInstruction() {
        return getDrink().getCode()
                .concat(extraHot ? "h" : "")
                .concat(":")
                .concat(sugar == 0 ? "" : String.valueOf(sugar))
                .concat(":")
                .concat(isStickNeeded() ? "0" : "");
    }

    float missingAmount() {
        return subtractFloats(drink.getPrice(), getMoneyAmount());
    }

}
