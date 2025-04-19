package cat.LacyCat;

import java.util.HashMap;
import java.util.Map;

public class Context {
    Map<String, Integer> variables = new HashMap<>();
    Map<String, Expression> functions = new HashMap<>();
    Map<String, int[]> slots = new HashMap<>();
}