package mil.army.rcc.kstreamrouter.model;


import javax.validation.constraints.NotNull;
import java.util.regex.Pattern;

public class RoutingRule {
    @NotNull
    private String regEx;
    @NotNull
    private String outputTopic;
    @NotNull
    private Pattern pattern;


    public String getRegEx() {
        return regEx;
    }

    public void setRegEx(String regEx) {
        this.regEx = regEx;
        //Compile pattern will validate pattern and throw PatternSyntaxException
        pattern = Pattern.compile(regEx);
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    public void setOutputTopic(String outputTopic) {
        this.outputTopic = outputTopic;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }



}
