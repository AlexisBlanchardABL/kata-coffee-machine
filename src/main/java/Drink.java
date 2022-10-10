import static utils.MathUtils.subtractFloats;

public enum Drink {
    TEA("T", 0.4f, Liquid.WATER),
    CHOCOLATE("H", 0.5f, Liquid.MILK),
    COFFEE("C", 0.6f, Liquid.WATER),
    ORANGE_JUICE("O", 0.6f, null);

    private final String code;
    private final float price;
    private final Liquid base;

    Drink(String code, float price, Liquid base) {
        this.code = code;
        this.price = price;
        this.base = base;
    }

    public String getCode() {
        return code;
    }

    public float getPrice() {
        return price;
    }

    public Liquid getBase() {
        return base;
    }

    float missingAmount(float moneyAmount) {
        return subtractFloats(price, moneyAmount);
    }

}
