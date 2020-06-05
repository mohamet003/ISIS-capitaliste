package com.example.demo;

import com.example.demo.generated.PallierType;
import com.example.demo.generated.ProductType;
import com.example.demo.generated.World;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.List;

public class Service
{
    InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
    public World readWorldFromXml(String username) throws JAXBException {
        String filename = username + "-world.xml";
        try {
            File temp = new File(filename);
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller un = cont.createUnmarshaller();
            World world = (World) un.unmarshal(temp);
            return world;
        } catch (Exception e) {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller un = cont.createUnmarshaller();
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
        World world = readWorldFromXml(username);

        long currentTime = System.currentTimeMillis();
        if(currentTime == world.getLastupdate()){
            return world;
        }
        world = majScore(world);
        world.setLastupdate(System.currentTimeMillis());
        // save world of user
        saveWorldToXml(world,username);
        return world;
    }


    public World majScore(World world){
        return world;

    }


    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateProduct(String username, ProductType newproduct) throws JAXBException, FileNotFoundException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le produit équivalent à celui passé
        // en paramètre
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) { return false;}

        // calculer la variation de quantité. Si elle est positive c'est
        // que le joueur a acheté une certaine quantité de ce produit
        // sinon c’est qu’il s’agit d’un lancement de production.
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            // soustraire de l'argent du joueur le cout de la quantité
            // achetée et mettre à jour la quantité de product
            double newPrice = product.getCout() * ((1 - Math.pow(product.getCroissance(), qtchange)) / (1 - product.getCroissance()));

            // mise à jour qte
            product.setQuantite(newproduct.getQuantite());
            // mise à jour argent
            world.setMoney(world.getMoney() - newPrice);
        } else {
            // initialiser product.timeleft à product.vitesse
            // pour lancer la production
            product.setTimeleft(product.getVitesse());
        }
        // sauvegarder les changements du monde
        saveWorldToXml(world,username);
        return true;
    }

    // prend en paramètre le pseudo du joueur et le manager acheté.
// renvoie false si l’action n’a pas pu être traitée
    public Boolean updateManager(String username, PallierType newmanager) throws JAXBException, FileNotFoundException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager équivalent à celui passé
        // en paramètre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            return false;
        }
        // débloquer ce manager

        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        // débloquer le manager de ce produit
        product.setManagerUnlocked(true);
        // mise à jour argent
        world.setMoney(world.getMoney() - manager.getSeuil());
        // soustraire de l'argent du joueur le cout du manager
        // sauvegarder les changements au monde
        saveWorldToXml(world, username);
        return true;
    }

    private ProductType findProductById(World world, int id) {
        ProductType produit = null;
        List<ProductType> products = world.getProducts().getProduct();
        for (ProductType p : products) {
            if (p.getId() == id) {
                produit = p;
            }
        }
        return produit;
    }


    private PallierType findManagerByName(World world, String name) {
        PallierType manager = null;
        List<PallierType> managers = world.getManagers().getPallier();
        for (PallierType m : managers) {
            if (m.getName().equals(name)) {
                manager = m;
            }
        }
        return manager;
    }

}
