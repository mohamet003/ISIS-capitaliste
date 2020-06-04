package com.example.demo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;

@Path("api")
public class WebService
{

    Service service;
    public WebService() {
        service = new Service();
    }
    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorld() throws JAXBException, FileNotFoundException {
        return Response.ok(service.getWorld("Mohamet")).build();
    }
}
