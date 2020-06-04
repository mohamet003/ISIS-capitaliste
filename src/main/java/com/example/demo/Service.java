package com.example.demo;

import com.example.demo.generated.World;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;

public class Service
{
    InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
    public World readWorldFromXml(String username) throws JAXBException {
        String filename = username + "-world.xml";
        System.out.println(input);
        try {
            File temp = new File(filename);
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller un = cont.createUnmarshaller();
            World world = (World) un.unmarshal(temp);
            return world;
        } catch (Exception e) {
            //Unmarwhaller
            System.out.println("test");
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller un = cont.createUnmarshaller();
            System.out.println(input);
            World world = (World) un.unmarshal(input);
            return world;
        }

    }

    public void saveWorldToXml(World world, String username) throws FileNotFoundException, JAXBException {

        String fileName = username + "-world.xml";
        OutputStream output = new FileOutputStream(fileName);
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller ma = cont.createMarshaller();
        ma.marshal(world, output);

    }

    public World getWorld(String username) throws JAXBException, FileNotFoundException {
        return readWorldFromXml(username);
    }

}
