package cat.LacyCat;

public class BinaryOp implements Expression {
    Expression left, right;
    String op;
    BinaryOp(Expression left, String op, Expression right) {
        this.left = left;
        this.right = right;
        this.op = op;
    }
    public int evaluate(Context ctx, int n) {
        int a = left.evaluate(ctx, n);
        int b = right.evaluate(ctx, n);
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            case "^" -> (int)Math.pow(a, b);
            default -> throw new RuntimeException("Unknown operator: " + op);
        };
    }
}