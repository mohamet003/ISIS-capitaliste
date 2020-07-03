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

    public World readWorldFromXml(String username) throws JAXBException, IOException {
        String filename = username + "-world.xml";
        System.out.println("le  fichier rec "+filename);
        JAXBContext jc = JAXBContext.newInstance(World.class);
        Unmarshaller u = jc.createUnmarshaller();
        InputStream input;
        File file;
        if (username != null) {
            file = new File(username + "-world.xml");
            if (file.exists()) {
                input = new FileInputStream(file);
            } else {
                input = getClass().getClassLoader().getResourceAsStream("world.xml");
            }
        } else {
            input = getClass().getClassLoader().getResourceAsStream("world.xml");
        }
        World world = (World) u.unmarshal(input);
        input.close();
        return world;
    }

    public void saveWorldToXml(World world, String username) throws IOException, JAXBException {
System.out.println("on  lecture for  modifs");
        String fileName = username + "-world.xml";
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller ma = cont.createMarshaller();
        FileOutputStream output = new FileOutputStream(fileName);
        ma.marshal(world, output);
        output.close();

    }

    public World getWorld(String username) throws JAXBException, IOException {
        World world = readWorldFromXml(username);
        long currentTime = System.currentTimeMillis();
        if(currentTime == world.getLastupdate()){
            return world;
        }
        System.out.println("Je récupére le monde avec comme argent "+world.getMoney());
        world = majScore(world);
        world.setLastupdate(System.currentTimeMillis());
        // save world of user
        saveWorldToXml(world,username);
        return world;
    }


    public World majScore(World world){

        System.out.println("majScore");
        System.out.println(" l'argent du monde "+world.getMoney());
        List<ProductType> products = world.getProducts().getProduct();
        // calcule du temps écoulé entre la derniere mise à jour du produit et maintenant
        long tempsEcoule = System.currentTimeMillis() -  world.getLastupdate();
        for (ProductType product : products){
            long test = tempsEcoule - product.getTimeleft();
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
                    nbrProduit_Produit = (int) ((tempsEcoule - product.getTimeleft() +  product.getVitesse()) / product.getVitesse());
                    System.out.println("Nombre de produit produit  "+nbrProduit_Produit+" avec une vetesse de "+product.getVitesse());
                    //maj du temps de production restant au cas ou la production du produit n'est pas terminée
                    product.setTimeleft(product.getVitesse() - (tempsEcoule % product.getVitesse()));
                }else if (product.getTimeleft() > 0){
                    nbrProduit_Produit = 1;
                    product.setTimeleft(0);
                }
                // Maj de l'argent du monde
                double argentGagner = product.getRevenu()*nbrProduit_Produit*product.getQuantite();
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
    public Boolean updateProduct(String username, ProductType newproduct) throws JAXBException, IOException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        System.out.println("updateProduct comme argent avant "+world.getMoney());
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
            if (world.getMoney()>= newPrice){
                product.setQuantite(newproduct.getQuantite());
                // mise à jour argent
                world.setMoney(world.getMoney() - newPrice);
                System.out.println("Acheter un produit argent aprés "+world.getMoney());
            }else{
                return null;
            }
        } else {
            // initialiser product.timeleft à product.vitesse
            // pour lancer la production
            System.out.println("Lancer la production  du produit "+product.getName());
            product.setTimeleft(product.getVitesse());
        }
        List<PallierType> palliers=(List<PallierType>) product.getPalliers().getPallier();
        for (PallierType palier: palliers){
            if (palier.isUnlocked()==false && product.getQuantite()>=palier.getSeuil()){
                addPallier(product,palier);
            }
        }
        // sauvegarder
        saveWorldToXml(world,username);
        return true;
    }

    // prend en paramètre le pseudo du joueur et le manager acheté.
// renvoie false si l’action n’a pas pu être traitée
    public Boolean updateManager(String username, PallierType newmanager) throws JAXBException, IOException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        System.out.println("Acheter manager, l'argent avant "+world.getMoney());
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
        System.out.println("Acheter manager, l'argent aprés "+world.getMoney());
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
            System.out.println("New Mise à jour Palier");
            // multiplie le revenu du produit par le ratio indiqué ;
            double newRevenu = product.getRevenu() * pallierType.getRatio();
            product.setRevenu(newRevenu);
        }

    }


    //PUT /upgrade : permet au client de communiquer au serveur l’achat d’un Cash Upgrade en passant
    //cet upgrade en paramètre sous la forme d’une entité de type « pallier »

    public boolean upgrade(String username, PallierType upgrade) throws JAXBException, IOException {
        System.out.println("UPGADE DD");
        World world= getWorld(username);
        if (!upgrade.isUnlocked() && world.getMoney() >= upgrade.getSeuil()){
            if (upgrade.getIdcible() == 0 ){
                List<ProductType> products = world.getProducts().getProduct();
                for (ProductType product :products){
                    world.setMoney(world.getMoney() - upgrade.getSeuil());
                    addPallier( product,upgrade );
                    upgrade.setUnlocked(true);
                }
            }
            else{
                ProductType product = findProductById(world,upgrade.getIdcible());
                world.setMoney(world.getMoney() - upgrade.getSeuil());
                addPallier( product , upgrade );
                upgrade.setUnlocked(true);
            }
        }else{
            saveWorldToXml(world,username);
            return false;
        }
        saveWorldToXml(world,username);
        return true;
    }

    public void deleteWorld(String username) throws JAXBException, IOException {
        World world=getWorld(username);
        System.out.println("Supprimer monde argent =>  "+world.getMoney());
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



    public void angelUpgrade(String username, PallierType angel) throws JAXBException, IOException {
        World world= getWorld(username);
        if(angel.getTyperatio() == TyperatioType.ANGE){
            world.setAngelbonus((int) (world.getAngelbonus() + angel.getRatio()));
        } else{
            upgrade( username, angel );
        }
        world.setActiveangels(world.getActiveangels()  -   (int) angel.getSeuil());

        System.out.println("Mise à jour Angel Upgrade =>  "+world.getMoney());
    }

}
