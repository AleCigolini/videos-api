package br.com.fiap.videosapi.video.infrastructure.azure;

import com.azure.storage.blob.BlobServiceClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BlobStorageConfigurationTest {

    @Test
    void testBlobServiceClientCreation() {
        String fakeConnectionString = "DefaultEndpointsProtocol=https;AccountName=fakeaccount;AccountKey=fakekey;EndpointSuffix=core.windows.net";
        BlobStorageConfiguration config = new BlobStorageConfiguration();

        BlobServiceClient client = config.blobServiceClient(fakeConnectionString);

        assertNotNull(client, "O BlobServiceClient não deve ser nulo");
        assertEquals(BlobServiceClient.class, client.getClass());
    }
}

