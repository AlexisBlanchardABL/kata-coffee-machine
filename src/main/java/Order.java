public class Order {
    private Drink drink;
    private int sugar;
    private float moneyAmount;

    public Order(Drink drink, int sugar, float moneyAmount) {
        this.drink = drink;
        this.sugar = sugar;
        this.moneyAmount = moneyAmount;
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

    @Override
    public String toString() {
        return "a " + drink + " with " +
                (sugar > 1 ? sugar + " sugars" :
                        sugar == 1 ? "1 sugar" : "no sugar");
    }
}
