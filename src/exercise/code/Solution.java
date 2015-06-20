package exercise.code;

import java.util.HashMap;
import java.util.Map;

public class Solution {
	
	public static void main(String[] args) {
		System.out.println(fractionToDecimal(-1, -2147483648));
	}
	
    public static String fractionToDecimal(int numerator, int denominator) {
        if (denominator == 0) {
            throw new RuntimeException("");
        }
        if (numerator == 0) {
            return "0";
        }
    
        long numer = (long)numerator;
        long denom = (long)denominator;
        StringBuilder sb = new StringBuilder();
        if (numer * denom < 0) {
            sb.append('-');
        }
        numer = Math.abs(numer);
        denom = Math.abs(denom);
        
        long div = numer / denom;
        long reminder = numer % denom;
        sb.append(div);
        if (reminder != 0) {
            sb.append('.');
            reminder *= 10;
        }
        
        Map<Long, Integer> map = new HashMap<Long, Integer>();
        int index = sb.length();
        while (reminder > 0) {
            if (map.containsKey(reminder)) {
                sb.insert(map.get(reminder), "(");
                sb.append(')');
                break;
            }
            map.put(reminder, index++);
            div = reminder / denom;
            reminder = (reminder % denom) * 10;
            sb.append(div);
        }
        return sb.toString();
    }
}
