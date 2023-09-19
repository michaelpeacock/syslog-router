package mil.army.rcc.kstreamrouter.config;

import mil.army.rcc.kstreamrouter.model.RoutingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

//@Validated
@Configuration
@ConfigurationProperties(prefix = "routing")
public class RoutingConfig {
    private static final Logger logger = LoggerFactory.getLogger(RoutingConfig.class);

    @NotEmpty
    private List<RoutingRule> rules = new ArrayList<>();

    public List<RoutingRule> getRules() {
        return rules;
    }

    public void setRules(List<RoutingRule> rules) {
        this.rules = rules;
    }
}