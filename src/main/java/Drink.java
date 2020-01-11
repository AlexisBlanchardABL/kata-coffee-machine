import utils.MathUtils;

public enum Drink {
    TEA("T", 0.4f),
    CHOCOLATE("H", 0.5f),
    COFFEE("C", 0.6f),
    ORANGE_JUICE("O", 0.6f);

    private String code;
    private float price;

    Drink(String code, float price) {
        this.code = code;
        this.price = price;
    }

    public String getCode() {
        return code;
    }

    public float getPrice() {
        return price;
    }

    public boolean costMoreThan(float money) {
        return MathUtils.subtractFloats(price, money) > 0;
    }
}
