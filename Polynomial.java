import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Polynomial {

    // Helper class to store Points (x, y)
    static class Point {
        int x;
        BigInteger y;

        public Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) {
        // 1. Read input from JSON file
        // Make sure "input.json" is in the same folder
        String jsonContent = readJsonFile("TestCase1.json");
        if (jsonContent == null) return;

        // 2. Parse the JSON manually
        List<Point> points = parseJson(jsonContent);

        // Extract required k (minimum points needed)
        int k = extractValueFromKeys(jsonContent, "k");
        
        System.out.println("Points found: " + points.size());
        System.out.println("Points required (k): " + k);

        if (points.size() < k) {
            System.out.println("ERROR: Not enough points to solve. The file must contain at least " + k + " points.");
            return;
        }

        // Take the first k points
        List<Point> selectedPoints = points.subList(0, k);

        // 3. Calculate Constant 'c'
        BigInteger secretC = findConstantTerm(selectedPoints);

        // 4. Print Result
        System.out.println("Secret constant (c): " + secretC);
    }

    // --- Core Math Logic: Lagrange Interpolation ---
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

                // num = num * (0 - x_m) -> which is -x_m
                num = num.multiply(BigInteger.valueOf(-x_m));
                
                // den = den * (x_j - x_m)
                den = den.multiply(BigInteger.valueOf(x_j - x_m));
            }

            // Calculate term: y_j * (num / den)
            // Note: We use divide() because for this specific polynomial problem, 
            // the division is guaranteed to be exact (integer result).
            BigInteger term = y_j.multiply(num).divide(den);
            constantC = constantC.add(term);
        }
        
        return constantC;
    }

    // --- Helper: Read File ---
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

    // --- Helper: Extract 'k' or 'n' from "keys" section ---
    private static int extractValueFromKeys(String json, String key) {
        // Regex to look for "n": 10 or "k": 7 inside the keys object or globally
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    // --- Helper: Robust JSON Parser ---
    private static List<Point> parseJson(String json) {
        List<Point> points = new ArrayList<>();
        
        // We assume the keys are integers "1", "2", "3"... up to "n"
        // We will loop and try to find them.
        for (int i = 1; i <= 20; i++) {
            // Find key "1": or "1" :
            String keyPattern = "\"" + i + "\"";
            int keyIndex = json.indexOf(keyPattern);
            
            if (keyIndex != -1) {
                // Look ahead for the opening brace { associated with this key
                int braceStart = json.indexOf("{", keyIndex);
                // Look for the closing brace }
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
                            // ignore parsing errors for bad blocks
                        }
                    }
                }
            }
        }
        return points;
    }

    private static String extractStringVal(String block, String key) {
        // Matches "base": "10" OR "base": "6" (with spaces)
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(block);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}