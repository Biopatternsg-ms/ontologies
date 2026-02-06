package com.biopatternsg.infrastructure.clients.external_clients;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Provider
public class DoctypeRemovalFilter implements ClientResponseFilter {

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        if (responseContext.hasEntity()) {
            try (InputStream stream = responseContext.getEntityStream()) {
                String body = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                String newBody = body.replaceAll("<!DOCTYPE[^>]*>", "");
                responseContext.setEntityStream(new ByteArrayInputStream(newBody.getBytes(StandardCharsets.UTF_8)));
            }
        }
    }
}
