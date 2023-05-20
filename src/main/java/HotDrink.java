import static utils.MathUtils.subtractFloats;

public enum HotDrink implements Drink {
    TEA("T", 0.4f, Liquid.WATER),
    CHOCOLATE("H", 0.5f, Liquid.MILK),
    COFFEE("C", 0.6f, Liquid.WATER);

    private final String code;
    private final float price;
    private final Liquid base;

    HotDrink(String code, float price, Liquid base) {
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
        return code.concat(extraHot ? "h" : "");
    }

    public float pricePlus(float amount) {
        return price + amount;
    }

}
