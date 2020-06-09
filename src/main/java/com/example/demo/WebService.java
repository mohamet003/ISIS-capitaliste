package com.example.demo;

import com.example.demo.generated.PallierType;
import com.example.demo.generated.ProductType;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
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
    public Response getWorld(@Context HttpServletRequest request) throws JAXBException, FileNotFoundException {
        String username = request.getHeader("X-user");
        return Response.ok(service.getWorld(username)).build();
    }

    @PUT
    @Path("product")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putProduct(@Context HttpServletRequest request, ProductType newproduct) throws JAXBException, FileNotFoundException, Exception {
        String username = request.getHeader("X-user");
        service.updateProduct(username, newproduct);
        return Response.ok(service.getWorld(username)).build();
    }

    @PUT
    @Path("manager")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void putManager(@Context HttpServletRequest request, PallierType newmanager) throws JAXBException, FileNotFoundException {
        String username = request.getHeader("X-user");
        service.updateManager(username, newmanager);
    }


}
