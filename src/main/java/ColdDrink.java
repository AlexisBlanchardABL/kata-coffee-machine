import static utils.MathUtils.subtractFloats;

public enum ColdDrink implements Drink {
    ORANGE_JUICE("O", 0.6f, null);

    private final String code;
    private final float price;
    private final Liquid base;

    ColdDrink(String code, float price, Liquid base) {
        this.code = code;
        this.price = price;
        this.base = base;
    }

    public Liquid getBase() {
        return base;
    }

    public float missingAmount(float moneyAmount) {
        return subtractFloats(price, moneyAmount);
    }

    public String drinkInstruction(boolean extraHot) {
        return code;
    }

    public float pricePlus(float amount) {
        return price + amount;
    }

}
