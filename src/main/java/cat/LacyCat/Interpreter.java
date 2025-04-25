package cat.LacyCat;
import cat.LacyCat.Annotaion.WarnUseOnly;
import cat.LacyCat.Custom.Grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Interpreter {
    private List<Grammar> grammars = new ArrayList<>();
    public Boolean debug = false;
    Context ctx = new Context();
    public void run(String code) {
        for (String line : code.strip().split("\\n")) {
            line = line.strip();
            if (line.contains("//")) {
                line = line.split("//", 2)[0].strip();
            }
            if (line.isEmpty()) continue;
            if (debug) {
                System.out.println("Processing Line " + line);
            }
            process(line);
        }
    }
    @WarnUseOnly("이 메서드는 실험적 기능을 사용하며, 언제든지 문제가 생길 수 있습니다.")
    public void runWithCustomGrammar(String code) {
        for (String line : code.split("\n")) {
            line = line.strip();
            if (line.contains("//")) {
                line = line.split("//", 2)[0].strip();
            }
            if (line.isEmpty()) continue;
            boolean handled = false;

            for (Grammar grammar : grammars) {
                Matcher matcher = grammar.getGrammarPattern().matcher(line);
                if (matcher.matches()) {
                    grammar.Execute(line);
                    handled = true;
                    break;
                }
            }

            if (!handled && !line.isEmpty()) {
                System.err.println("Unknown syntax: " + line);
            }
        }
    }
    public void addGrammar(Grammar gr) { grammars.add(gr); }
    private void process(String line) {
        if (line.startsWith("IF ")) {
            Matcher m = Pattern.compile("IF (\\w+)\\s*(==|!=|<|<=|>|>=)\\s*(\\w+) *: *(.*);").matcher(line);
            if (m.matches()) {
                String left = m.group(1);
                String op = m.group(2);
                String right = m.group(3);
                String thenPart = m.group(4);

                int leftVal = ctx.variables.get(left);
                int rightVal;

                if (ctx.variables.containsKey(right)) {
                    rightVal = ctx.variables.get(right);
                } else {
                    rightVal = Integer.parseInt(right);
                }

                boolean condition = switch (op) {
                    case "==" -> leftVal == rightVal;
                    case "!=" -> !(leftVal == rightVal);
                    case "<" -> leftVal < rightVal;
                    case "<=" -> leftVal <= rightVal;
                    case ">" -> leftVal > rightVal;
                    case ">=" -> leftVal >= rightVal;
                    default -> throw new RuntimeException("Invalid operator: " + op);
                };

                if (condition) {
                    System.out.println("Condition true: " + left + " " + op + " " + right + " → executing '" + thenPart + "'");
                    process(thenPart);
                } else {
                    if (debug) {
                        System.out.println("Condition false: " + left + " " + op + " " + right);
                    }
                }
                return;
            }
        }
        if (line.contains("[")) {
            Matcher m = Pattern.compile("(\\w+) = \\[([0-9]+)\\.\\.([0-9]+)\\];").matcher(line);
            if (m.matches()) {
                String name = m.group(1);
                int from = Integer.parseInt(m.group(2));
                int to = Integer.parseInt(m.group(3));
                if (debug) {
                    System.out.println("Creating slot: " + name + " with range [" + from + ".." + to + "]");
                }
                ctx.slots.put(name, new int[to + 1]); // 1-based 인덱스
                return;
            }
        }
        if (line.contains(":")) {
            Matcher rangeSlotFunc = Pattern.compile("(\\w+)\\[(\\d+)\\.\\.(\\d+)\\] : (\\w+);").matcher(line);
            if (rangeSlotFunc.matches()) {
                String slotName = rangeSlotFunc.group(1);  // 슬롯 이름
                int from = Integer.parseInt(rangeSlotFunc.group(2)) - 1;  // 시작 인덱스 (0-based)
                int to = Integer.parseInt(rangeSlotFunc.group(3)) - 1;    // 끝 인덱스 (0-based)
                String funcName = rangeSlotFunc.group(4);  // 함수 이름
                if (debug) {
                    System.out.println("Applying function: " + funcName + " to slot " + slotName + "[" + (from+1) + ".." + (to+1) + "]");
                }
                int[] slot = ctx.slots.get(slotName);
                Expression expr = ctx.functions.get(funcName);
                if (expr == null) throw new RuntimeException("Function not found: " + funcName);
                for (int i = from; i <= to; i++) {
                    int input = slot[i];
                    ctx.variables.put("n", input);  // n을 b[i]로 설정
                    int result = expr.evaluate(ctx, input);
                    slot[i] = result;

                    if (debug) {
                        System.out.println(slotName + "[" + (i+1) + "] is " + result);
                    } else {
                        System.out.println(slotName + "[" + (i+1) + "] = " + result);
                    }
                }
                return;
            }
            Matcher rangeExtract = Pattern.compile("(\\w+) : (\\w+)\\[(\\d+)\\.\\.(\\d+)];").matcher(line);
            if (rangeExtract.matches()) {
                String varName = rangeExtract.group(1);   // 'a'
                String slotName = rangeExtract.group(2);  // 'b'
                int from = Integer.parseInt(rangeExtract.group(3)) - 1; // 0-based index (from 1..10 to 0..9)
                int to = Integer.parseInt(rangeExtract.group(4)) - 1;   // 0-based index (from 1..10 to 0..9)
                int[] slot = ctx.slots.get(slotName);
                if (slot == null) throw new RuntimeException("Slot not found: " + slotName);
                Integer aValue = ctx.variables.get(varName);
                if (aValue == null) throw new RuntimeException("Variable not found: " + varName);
                for (int i = from; i <= to; i++) {
                    slot[i] = aValue;  // a의 값 덮어쓰기
                    if (debug) {
                        System.out.println(slotName + "[" + (i+1) + "] is " + aValue);
                    } else {
                        System.out.println(slotName + "[" + (i+1) + "] = " + aValue);
                    }
                }
                return;
            }
            Matcher m = Pattern.compile("(\\w+) : (\\w+)(\\[(\\w+)\\])?;").matcher(line);
            if (m.matches()) {
                String var = m.group(1);     // 변수 이름
                String target = m.group (2);  // 타겟 이름 (함수 또는 슬롯)
                String slotIndex = m.group(4);  // 슬롯 인덱스 (있을 경우)
                if (debug) {
                    System.out.println("Assigning value: " + var + " to " + target);
                }
                if (slotIndex != null) {
                    int index = Integer.parseInt(slotIndex) - 1;
                    int[] slot = ctx.slots.get(target);
                    int value = ctx.variables.getOrDefault(var, 0);
                    slot[index] = value;
                    if (debug) {
                        System.out.println("Slot " + target + "[" + index + "] = " + value);
                    }
                } else {
                    Expression expr = ctx.functions.get(target);
                    if (expr == null) throw new RuntimeException("Function not found: " + target);
                    int n = ctx.variables.getOrDefault(var, 0);  // n 변수 값 가져오기
                    int result = expr.evaluate(ctx, n);  // 함수 평가
                    if (debug) {
                        System.out.println("Result of function " + target + ": " + result);
                    }
                    else {
                        System.out.println(result);
                    }
                }
                return;
            }
            Matcher slotFunc = Pattern.compile("(\\w+)\\[(\\d+)\\] : (\\w+);").matcher(line);
            if (slotFunc.matches()) {
                String slotName = slotFunc.group(1);  // 슬롯 이름
                int index = Integer.parseInt(slotFunc.group(2)) - 1;  // 1부터 시작하는 인덱스 처리
                String funcName = slotFunc.group(3);  // 함수 이름
                if (debug) {
                    System.out.println("Applying function: " + funcName + " to slot " + slotName + "[" + (index + 1) + "]");
                }
                int[] slot = ctx.slots.get(slotName);  // 슬롯 배열
                int input = slot[index];  // 슬롯 값 가져오기
                ctx.variables.put("n", input);  // 'n'에 값 대입
                Expression expr = ctx.functions.get(funcName);  // 함수 객체 가져오기
                if (expr == null) throw new RuntimeException("Function not found: " + funcName);
                int result = expr.evaluate(ctx, input);  // 함수 실행
                slot[index] = result;  // 결과 저장
                if (debug) {
                    System.out.println("Result stored: " + slotName + "[" + (index + 1) + "] = " + result);
                }
                else {
                    System.out.println(slotName + "[" + (index + 1) + "] = " + result);
                }
                return;
            }
        }
        if (line.contains("=")) {
            String[] parts = line.replace(";", "").split("=", 2);
            String name = parts[0].strip();
            String value = parts[1].strip();
            if (debug) {
                System.out.println("Defining: " + name + " = " + value);
            }
            if (value.contains("n")) {
                ctx.functions.put(name, parseExpression(value));
                if (debug) {
                    System.out.println("Function defined: " + name);
                }
            } else {
                int literal = evaluateExpression(value);
                ctx.variables.put(name, literal);
                if (debug) {
                    System.out.println("Variable defined: " + name + " = " + literal);
                }
            }
            return;
        }
    }
    private int evaluateExpression(String expr) {
        if (debug) {
            System.out.println("Evaluating expression: " + expr);
        }
        return parseExpression(expr).evaluate(ctx, 0);
    }
    private Expression parseExpression(String expr) {
        List<String> tokens = new ArrayList<>(List.of(expr.replace("(", " ( ").replace(")", " ) ").split("\\s+")));
        if (debug) {
            System.out.println("Parsing expression: " + expr);
        }
        return parseExpr(tokens);
    }
    private Expression parseExpr(List<String> tokens) {
        Stack<Expression> values = new Stack<>();
        Stack<String> ops = new Stack<>();
        while (!tokens.isEmpty()) {
            String token = tokens.remove(0);
            if (debug) {
                System.out.println("Token: " + token);
            }
            if (token.matches("[0-9]+")) {
                values.push(new Literal(Integer.parseInt(token)));
                if (debug) {
                    System.out.println("Pushed literal: " + token);
                }
            } else if (token.matches("[a-zA-Z]+")) {
                values.push(new Variable(token));
                if (debug) {
                    System.out.println("Pushed variable: " + token);
                }
            } else if (token.equals("(")) {
                values.push(parseExpr(tokens));
            } else if (token.equals(")")) {
                break;
            } else if (token.matches("[-+*/\\^]")) {
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(token)) {
                    Expression b = values.pop();
                    Expression a = values.pop();
                    String op = ops.pop();
                    values.push(new BinaryOp(a, op, b));
                    if (debug) {
                        System.out.println("Pushed binary operation: " + op);
                    }
                }
                ops.push(token);
                if (debug) {
                    System.out.println("Pushed operator: " + token);
                }
            }
        }
        while (!ops.isEmpty()) {
            Expression b = values.pop();
            Expression a = values.pop();
            String op = ops.pop();
            values.push(new BinaryOp(a, op, b));
            if (debug) {
                System.out.println("Pushed final binary operation: " + op);
            }
        }
        return values.pop();
    }
    private int precedence(String op) {
        if (debug) {
            System.out.println("Operator precedence for " + op);
        }
        return switch (op) {
            case "^" -> 3;
            case "*", "/" -> 2;
            case "+", "-" -> 1;
            default -> 0;
        };
    }
}