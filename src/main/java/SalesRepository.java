import java.util.*;

public class SalesRepository {
    private final List<Drink> sales;

    public SalesRepository() {
        sales = new ArrayList<>();
    }

    public void save(Drink drink) {
        sales.add(drink);
    }

    public long getDrinkCount(Drink drink) {
        return sales.stream()
                .filter(d -> d.equals(drink))
                .count();
    }

    public float getEarnedMoney() {
        return sales.stream()
                .reduce(
                        0f,
                        (amount, drink) -> drink.pricePlus(amount),
                        Float::sum
                );
    }

}
