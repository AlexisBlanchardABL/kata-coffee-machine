package utils;

import java.math.BigDecimal;
import java.math.MathContext;

public class MathUtils {
    public static float subtractFloats(float value, float valueToSubtract) {
        return BigDecimal.valueOf(value).subtract(BigDecimal.valueOf(valueToSubtract), new MathContext(5)).floatValue();
    }
}
