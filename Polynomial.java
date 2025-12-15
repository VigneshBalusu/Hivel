import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Polynomial {

    static class Point {
        int x;
        BigInteger y;

        public Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) {
        String jsonContent = readJsonFile("TestCase1.json");
        if (jsonContent == null) return;

        List<Point> points = parseJson(jsonContent);

        int k = extractValueFromKeys(jsonContent, "k");
        
        System.out.println("Points found: " + points.size());
        System.out.println("Points required (k): " + k);

        if (points.size() < k) {
            System.out.println("ERROR: Not enough points to solve. The file must contain at least " + k + " points.");
            return;
        }

        List<Point> selectedPoints = points.subList(0, k);

        BigInteger secretC = findConstantTerm(selectedPoints);

        System.out.println("Secret constant (c): " + secretC);
    }

    private static BigInteger findConstantTerm(List<Point> points) {
        BigInteger constantC = BigInteger.ZERO;

        for (int j = 0; j < points.size(); j++) {
            Point p_j = points.get(j);
            int x_j = p_j.x;
            BigInteger y_j = p_j.y;

            BigInteger num = BigInteger.ONE;
            BigInteger den = BigInteger.ONE;

            for (int m = 0; m < points.size(); m++) {
                if (j == m) continue;
                
                Point p_m = points.get(m);
                int x_m = p_m.x;

                num = num.multiply(BigInteger.valueOf(-x_m));
                
                den = den.multiply(BigInteger.valueOf(x_j - x_m));
            }

            BigInteger term = y_j.multiply(num).divide(den);
            constantC = constantC.add(term);
        }
        
        return constantC;
    }

    private static String readJsonFile(String filename) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return null;
        }
        return content.toString();
    }

    private static int extractValueFromKeys(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private static List<Point> parseJson(String json) {
        List<Point> points = new ArrayList<>();
        
        for (int i = 1; i <= 20; i++) {
            String keyPattern = "\"" + i + "\"";
            int keyIndex = json.indexOf(keyPattern);
            
            if (keyIndex != -1) {
                int braceStart = json.indexOf("{", keyIndex);
                int braceEnd = json.indexOf("}", braceStart);
                
                if (braceStart != -1 && braceEnd != -1) {
                    String block = json.substring(braceStart, braceEnd + 1);
                    
                    String baseStr = extractStringVal(block, "base");
                    String valStr = extractStringVal(block, "value");
                    
                    if (baseStr != null && valStr != null) {
                        try {
                            int base = Integer.parseInt(baseStr);
                            BigInteger y = new BigInteger(valStr, base);
                            points.add(new Point(i, y));
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
        return points;
    }

    private static String extractStringVal(String block, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(block);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}