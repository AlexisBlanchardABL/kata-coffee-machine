public class Order {
    private Drink drink;
    private int sugar;

    public Order(Drink drink, int sugar) {
        this.drink = drink;
        this.sugar = sugar;
    }

    public boolean isStickNeeded() {
        return sugar > 0;
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
