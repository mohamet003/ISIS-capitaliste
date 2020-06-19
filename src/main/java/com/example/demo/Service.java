package com.example.demo;

import com.example.demo.generated.PallierType;
import com.example.demo.generated.ProductType;
import com.example.demo.generated.TyperatioType;
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
        System.out.println("majScore");
        List<ProductType> products = world.getProducts().getProduct();
        // calcule du temps écoulé entre la derniere mise à jour du produit et maintenant
        long tempsEcoule = System.currentTimeMillis() -  world.getLastupdate();

        for (ProductType product : products){
            long test = tempsEcoule - product.getVitesse();

            // pas assez de temps écoulé pour la production d'un produit
            if (tempsEcoule < 0 ){
                System.out.println("pas assez de temps écoulé pour la production d'un produit");
                //maj du temps de production restant au cas ou la production du produit n'est pas terminée
                product.setTimeleft(product.getVitesse() - (tempsEcoule % product.getVitesse()));
            }else{

                int nbrProduit_Produit = 0;
                if (product.isManagerUnlocked()){
                    System.out.println("isManagerUnlocked = true ");
                    // calcul du nombre de produit
                    nbrProduit_Produit = (int) (tempsEcoule/product.getVitesse());

                    System.out.println("Nombre de produit produit  "+nbrProduit_Produit);
                    //maj du temps de production restant au cas ou la production du produit n'est pas terminée
                    product.setTimeleft(product.getVitesse() - (tempsEcoule % product.getVitesse()));
                }else if (product.getTimeleft() > 0){
                    nbrProduit_Produit = 1;
                    product.setTimeleft(0);
                }

                // Maj de l'argent du monde
                double argentGagner = product.getRevenu()*nbrProduit_Produit;


                // Bonus pour les anges
                argentGagner += argentGagner * (world.getActiveangels() * world.getAngelbonus() / 100 );


                // Maj de l'argent de l'argent du joueur
                world.setMoney(world.getMoney() + argentGagner);
                // Maj de l'argent du score du joueur
                world.setScore(world.getScore() + argentGagner );
            }
        }

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
        manager.setUnlocked(true);

        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        // débloquer le manager de ce produit
        product.setManagerUnlocked(true);

        // soustraire de l'argent du joueur le cout du manager
        world.setMoney(world.getMoney() - manager.getSeuil());

        // sauvegarder les changements au monde
        saveWorldToXml(world, username);
        return true;
    }

    public ProductType findProductById(World world, int id) {
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



    public void addPallier(ProductType product, PallierType pallierType) throws JAXBException, FileNotFoundException {
        // on deverouille le pallier
        pallierType.setUnlocked(true);

        if (pallierType.getTyperatio() == TyperatioType.VITESSE){
            //divise le temps de production par le ratio indiqué ;
            int newVitesse= product.getVitesse() / (int)pallierType.getRatio();
            product.setVitesse(newVitesse);
        }
        else{
            // multiplie le revenu du produit par le ratio indiqué ;
            double newRevenu = product.getRevenu() * pallierType.getRatio();
            product.setRevenu(newRevenu);
        }
    }


    //PUT /upgrade : permet au client de communiquer au serveur l’achat d’un Cash Upgrade en passant
    //cet upgrade en paramètre sous la forme d’une entité de type « pallier »

    public boolean upgrade(String username, PallierType upgrade) throws JAXBException, FileNotFoundException{
        World world= getWorld(username);
        if (!upgrade.isUnlocked() && world.getMoney() >= upgrade.getSeuil()){
            if (upgrade.getIdcible() == 0 ){
                List<ProductType> products = world.getProducts().getProduct();
                for (ProductType product :products){
                    addPallier( product,upgrade );
                }
            }
            else{
                ProductType product = findProductById(world,upgrade.getIdcible());
                addPallier( product , upgrade );
            }
        }else{
            return false;
        }
        return true;
    }

    public void deleteWorld(String username) throws JAXBException, FileNotFoundException {
        World world=getWorld(username);
        double newAnges = Math.round(150 * Math.sqrt(world.getScore()/ Math.pow(10, 15))) - world.getTotalangels();
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
        World newWord = (World) u.unmarshal(input);
        newWord.setActiveangels(world.getActiveangels() + newAnges);
        newWord.setTotalangels(world.getTotalangels() + newAnges);
        newWord.setScore(world.getScore());
        saveWorldToXml(newWord, username);
    }



    public void angelUpgrade(String username, PallierType angel) throws JAXBException, FileNotFoundException{
        World world= getWorld(username);
        if(angel.getTyperatio() == TyperatioType.ANGE){
            world.setAngelbonus((int) (world.getAngelbonus() + angel.getRatio()));
        } else{
            upgrade( username, angel );
        }
        world.setActiveangels(world.getActiveangels()  -   (int) angel.getSeuil());
    }

}
