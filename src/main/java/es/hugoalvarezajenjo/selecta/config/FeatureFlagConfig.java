package es.hugoalvarezajenjo.selecta.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class FeatureFlagConfig {
    @Value("${feature.filterList.enabled:false}")
    private boolean filterListEnabled;
    @Value("${feature.subjectResources.enabled:false}")
    private boolean subjectResourceEnabled;
}
