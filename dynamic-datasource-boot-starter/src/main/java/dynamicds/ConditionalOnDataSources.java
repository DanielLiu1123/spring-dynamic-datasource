package dynamicds;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Conditional on the presence of multiple data sources (srping.datasource.datasources).
 *
 * @author Freeman
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(MultipleDataSourcesCondition.class)
public @interface ConditionalOnDataSources {}

class MultipleDataSourcesCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var properties = Binder.get(context.getEnvironment())
                .bindOrCreate(DataSourcesProperties.PREFIX, DataSourcesProperties.class);
        var configured = !properties.datasources().isEmpty();
        if (configured) {
            return ConditionOutcome.match();
        } else {
            return ConditionOutcome.noMatch("No multiple data sources configured.");
        }
    }
}
