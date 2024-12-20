### oas-spec-generator
## Getting Started

*Requirements:*
- Java 17 SDK
- Maven (for build) or your preferred build tool

*Clone the Repository:*
bash
git clone https://github.com/kislayio/oas-spec-generator
cd oas-spec-generator


Ensure config.yml is placed in src/main/resources/config.yml. Ping @kislay98 or @amit-kislaydata for the config file.

*Build:*
bash
mvn clean package


*Run the Application:*
bash
java -cp target/oas-spec-generator-<version>.jar io.kislay.spec.generator.openai.OpenAIProcessorApplication server src/main/resources/config.yml


This starts the server with the specified configuration.

*OR*

*Run the Application (IntelliJ IDEA):*
- Open the project in IntelliJ IDEA 
- Set Project SDK to Java 17 
- Create a Run Configuration:
  - Main class: io.kislay.spec.generator.openai.OpenAIProcessorApplication 
  - Program arguments: server src/main/resources/config.yml 
  - Click 'Run'

### API Usage

*Endpoint:*  
POST /documents/process

*Request Body (InputData):*
- urls: List of webpage URLs from which the OAS spec is derived
- prompt: A text prompt for generating the specification
- strategy: One of the GenerationStrategy values (e.g. OPEN_API_SPEC_ONLY, NEO_TEMPLATE_ONLY)
- sourceName: The base name for output files
- path: (optional) A file path used if strategy involves file-based code generation

*Example Request:*
json
{
  "urls": ["https://example.com/api-docs"],
  "prompt": "Generate OpenAPI spec",
  "strategy": "OPEN_API_SPEC_ONLY",
  "sourceName": "my-service",
  "path": "/path/to/openapi.json"
}

*When to Use Which Strategy:*
- OPEN_API_SPEC_ONLY: Only generate the OpenAPI spec.
- OPEN_API_SPEC_AND_NEO_TEMPLATE: Generate the OpenAPI spec and then a Neo connector template.
- OPEN_API_SPEC_AND_H20_GENERATOR: Generate the OpenAPI spec and then code using the H20 generator.
- NEO_TEMPLATE_ONLY: Directly generate the Neo template from an existing spec file.
- H20_GENERATOR_ONLY: Directly generate code using H20 from an existing spec file.
