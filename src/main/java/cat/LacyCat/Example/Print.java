package cat.LacyCat.Example;

import cat.LacyCat.Custom.Grammar;

import java.util.regex.Pattern;

public class Print implements Grammar {
    private final Pattern pattern = Pattern.compile("print\\s+\"(.+)\"");
    @Override
    public Pattern getGrammarPattern() {
        return pattern;
    }

    @Override
    public void Execute(String line) {
        var matcher = pattern.matcher(line);
        if (matcher.matches()) {
            String content = matcher.group(1);
            System.out.println(content);
        }
    }
}
