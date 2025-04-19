package cat.LacyCat;

public class Literal implements Expression {
    int value;
    Literal(int value) { this.value = value; }
    public int evaluate(Context ctx, int n) { return value; }
}