public enum Drink {
    TEA("T"),
    CHOCOLATE("H"),
    COFFEE("C");

    private String code;

    Drink(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
