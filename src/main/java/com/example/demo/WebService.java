package com.example.demo;

import com.example.demo.generated.PallierType;
import com.example.demo.generated.ProductType;
import com.example.demo.generated.World;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;

@Path("generic")
public class WebService
{

    Service service;
    public WebService() {
        service = new Service();
    }
    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorld(@Context HttpServletRequest request) throws JAXBException, IOException {
        String username = request.getHeader("X-user");
        return Response.ok(service.getWorld(username)).build();
    }

    @PUT
    @Path("product")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putProduct(@Context HttpServletRequest request, ProductType newproduct) throws JAXBException, FileNotFoundException, Exception {
        String username = request.getHeader("X-user");
        service.updateProduct(username, newproduct);
        World world = service.getWorld(username);
        return Response.ok(service.findProductById( world, newproduct.getId())).build();
    }

    @PUT
    @Path("manager")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putManager(@Context HttpServletRequest request, PallierType newmanager) throws JAXBException, IOException {
        String username = request.getHeader("X-user");
        service.updateManager(username, newmanager);
        return Response.ok(service.getWorld(username)).build();
    }

    @PUT
    @Path("upgrade")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putUpgrade(@Context HttpServletRequest request, PallierType upgrade) throws JAXBException, IOException {
        String username = request.getHeader("X-user");
        service.upgrade(username, upgrade);
        return Response.ok(service.getWorld(username)).build();
    }

    @DELETE
    @Path("world")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteWorld(@Context HttpServletRequest request) throws JAXBException, IOException {
        String username = request.getHeader("X-user");
        service.deleteWorld(username);
        return Response.ok(service.getWorld(username)).build();
    }

    @PUT
    @Path("angelUpgrade")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putAngelUpgrade(@Context HttpServletRequest request, PallierType angelUpgrade) throws JAXBException, IOException {
        String username = request.getHeader("X-user");
        service.angelUpgrade(username, angelUpgrade);
        return Response.ok(service.getWorld(username)).build();
    }



}
