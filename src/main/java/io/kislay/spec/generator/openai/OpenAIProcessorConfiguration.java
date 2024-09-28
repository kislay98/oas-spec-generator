package io.kislay.spec.generator.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import javax.validation.constraints.NotEmpty;

public class OpenAIProcessorConfiguration extends Configuration {
    @NotEmpty
    private String openAiApiKey;

    @JsonProperty
    public String getOpenAiApiKey() {
        return openAiApiKey;
    }

    @JsonProperty
    public void setOpenAiApiKey(String openAiApiKey) {
        this.openAiApiKey = openAiApiKey;
    }
}
