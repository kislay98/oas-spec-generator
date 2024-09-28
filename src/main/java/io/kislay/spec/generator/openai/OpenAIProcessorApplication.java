package io.kislay.spec.generator.openai;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.kislay.spec.generator.document.DocumentProcessorResource;
import io.kislay.spec.generator.document.DocumentProcessorService;

public class OpenAIProcessorApplication extends Application<OpenAIProcessorConfiguration> {

    public static void main(String[] args) throws Exception {
        new OpenAIProcessorApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<OpenAIProcessorConfiguration> bootstrap) {
        // Initialize your application here if needed
    }

    @Override
    public void run(OpenAIProcessorConfiguration configuration, Environment environment) {
        DocumentProcessorService documentProcessorService = new DocumentProcessorService();
        // Register resources
        environment.jersey().register(new DocumentProcessorResource(documentProcessorService));
        environment.jersey().register(new OpenAIProcessorResource());
    }
}
