import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class SalesRepository {
    private Map<String, Integer> beverageSales = new HashMap<>();
    private float earnedMoney = 0f;

    public SalesRepository() {
        EnumSet.allOf(Drink.class).forEach((drink) -> beverageSales.put(drink.toString(), 0));
    }

    public void save(Order order) {
        beverageSales.computeIfPresent(order.getDrink().toString(), (drink, count) -> count+1);
        earnedMoney += order.getDrink().getPrice();
    }

    public Integer getDrinkCount(Drink drink) {
        return beverageSales.get(drink.toString());
    }

    public float getEarnedMoney() {
        return earnedMoney;
    }
}
