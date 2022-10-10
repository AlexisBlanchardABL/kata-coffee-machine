public class Order {
    private final Drink drink;
    private final int sugar;
    private final float moneyAmount;
    private final boolean extraHot;

    public Order(Drink drink, int sugar, float moneyAmount) {
        this(drink, sugar, moneyAmount, false);
    }

    public Order(Drink drink, int sugar, float moneyAmount, boolean extraHot) {
        this.drink = drink;
        this.sugar = sugar;
        this.moneyAmount = moneyAmount;
        this.extraHot = extraHot;
    }

    public boolean isStickNeeded() {
        return sugar > 0;
    }

    public float getMoneyAmount() {
        return moneyAmount;
    }

    public Drink getDrink() {
        return drink;
    }

    public int getSugar() {
        return sugar;
    }

    public boolean isExtraHot() {
        return extraHot;
    }

    @Override
    public String toString() {
        return "a " + drink + " with " +
                (sugar > 1 ? sugar + " sugars" :
                        sugar == 1 ? "1 sugar" : "no sugar");
    }
}
