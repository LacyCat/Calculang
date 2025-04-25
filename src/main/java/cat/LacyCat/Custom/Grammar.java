package cat.LacyCat.Custom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Grammar {
    Pattern getGrammarPattern();
    void Execute(String line);
}
