package cat.LacyCat;

public class Variable implements Expression {
    String name;
    Variable(String name) { this.name = name; }
    public int evaluate(Context ctx, int n) {
        if ("n".equals(name)) return n;
        return ctx.variables.getOrDefault(name, 0);
    }
}