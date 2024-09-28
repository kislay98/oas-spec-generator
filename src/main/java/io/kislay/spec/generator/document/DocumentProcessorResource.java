package io.kislay.spec.generator.document;
import io.kislay.spec.generator.record.InputData;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;

@Path("/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentProcessorResource {

    private final DocumentProcessorService documentProcessorService;

    public DocumentProcessorResource(DocumentProcessorService documentProcessorService) {
        this.documentProcessorService = documentProcessorService;
    }

    @POST
    @Path("/process")
    public Response processDocuments(InputData inputData) throws InterruptedException {
        // Validate input data
        if (inputData.urls() == null || inputData.urls().isEmpty()) {
            throw new WebApplicationException("URL list cannot be empty", Response.Status.BAD_REQUEST);
        }
        if (inputData.prompt() == null || inputData.prompt().isEmpty()) {
            throw new WebApplicationException("Prompt cannot be empty", Response.Status.BAD_REQUEST);
        }

    // Send the aggregated content and prompt to the DocumentProcessorService
        System.out.println("Starting up!");
        Thread.sleep(2000);
        String processedResult = documentProcessorService.process(inputData);

        // Return the processed result as JSON response
        return Response.ok(processedResult).build();

    }
}