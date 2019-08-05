import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Result {

    public static void main(String[] args) {
        System.out.println("Arturo Ramirez Morales");

        try {

            String productsPath = "target/classes/products.txt";
            String listingPath = "target/classes/listings.txt";

            if (args.length == 1) {
                productsPath = args[0];
            } else if (args.length == 2) {
                productsPath = args[0];
                listingPath = args[1];
            }

            File productsFile = new File(productsPath);
            File listingsFile = new File(listingPath);


            List<Product> products = getProducts(productsFile);
            HashMap<String, List<Listing>> listings = getListingByManufacturer(listingsFile);
            HashMap<Product, List<Listing>> result = machProducts(products, listings);
            resultFile(result);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void resultFile(HashMap<Product, List<Listing>>  result) throws IOException{

        File fout = new File("result.txt");
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        JsonArray productsResult = new JsonArray();

        for (Map.Entry<Product, List<Listing>> entry : result.entrySet()) {

            Gson gson = new Gson();
            JsonObject matchProduct = new JsonObject();
            matchProduct.addProperty("product_name",entry.getKey().getProductName());

            ArrayList<Listing> listings = (ArrayList<Listing>) entry.getValue();
            JsonElement element = gson.toJsonTree(listings , new TypeToken<List<Listing>>() {}.getType());
            JsonArray jsonArray = element.getAsJsonArray();
            matchProduct.add("listings", jsonArray);

            bw.write(gson.toJson(matchProduct));
            bw.newLine();

            productsResult.add(matchProduct);

        }

        bw.close();

        System.out.println(productsResult.toString());

    }

    public static HashMap<Product, List<Listing>> machProducts(List<Product> products, HashMap<String, List<Listing>> listings){

        int matchesFound = 0;

        HashMap<Product, List<Listing>> result = new HashMap<Product, List<Listing>>();


        for (Product product : products) {

            List<Listing> listingResult = new ArrayList<Listing>();

            Map<String,List<Listing>> subset = listings.entrySet().stream().filter(e-> e.getKey().contains(String.valueOf(product.getManufacturer().toUpperCase())))
                    .collect(HashMap::new,
                            (m, e) -> m.put(e.getKey(), e.getValue()),
                            Map::putAll);

            for(Map.Entry<String,List<Listing>> entry : subset.entrySet()) {
                List<Listing> listingObject = entry.getValue();


                String productName = product.getProductName().replace('_', ' ').toUpperCase() + ' ';
                String productNameNoSigns = product.getProductName().replaceAll("-", "").replaceAll("-", "").toUpperCase();

                for (Listing listing : listingObject) {

                    int intModel = listing.getTitle().toUpperCase().indexOf(product.getModel().toUpperCase());
                    int intProductName = listing.getTitle().toUpperCase().indexOf(productName);


                    String specialListing = listing.getTitle().toUpperCase().replaceAll(" ", "").replaceAll("-", "");

                    String specialListing2 = listing.getTitle().toUpperCase().replaceAll("-", "");
                    int intProductNameNoSigns = specialListing.indexOf(productNameNoSigns);

                    int intFamily = -1;
                    if (product.getFamily() != "") {
                        intFamily = specialListing2.indexOf(product.getFamily().toUpperCase() +" " + product.getModel().toUpperCase()+" ");
                    }

                    int intManufacturer = specialListing2.indexOf(product.getManufacturer() +" " + product.getModel().toUpperCase()+" ");

                    if (intProductName != -1) { ;
                        matchesFound++;
                        listingResult.add(listing);
                    }else if(intProductNameNoSigns != -1) {
                        matchesFound++;
                        listingResult.add(listing);

                    }else if ((intModel != -1) && ((intFamily != -1) || (intManufacturer != -1))) {
                        matchesFound++;
                        listingResult.add(listing);
                    }

                }
            }

            if (listingResult.size() > 0) {
                result.put(product, listingResult);
            }else{
                System.out.println(product.getProductName());
            }


        }

        System.out.println("Products whit match: " + result.size());
        System.out.println("Matches found: " + matchesFound);

        return result;

    }

    public static List<Product> getProducts(File products) throws IOException{
        FileReader productsReader = new FileReader(products);
        BufferedReader productsBufferedReader = new BufferedReader(productsReader);
        String productString;

        List<Product> productList = new ArrayList<Product>();

        while ((productString = productsBufferedReader.readLine())!= null){

            Gson gson = new Gson();
            Product product = new Product();
            JsonObject jsonObject = new JsonParser().parse(productString).getAsJsonObject();
            product.setProductName(jsonObject.has("product_name")?jsonObject.get("product_name").getAsString():"");
            product.setManufacturer(jsonObject.has("manufacturer")?jsonObject.get("manufacturer").getAsString():"");
            product.setFamily(jsonObject.has("family")?jsonObject.get("family").getAsString():"");
            product.setModel(jsonObject.has("model")?jsonObject.get("model").getAsString():"");
            product.setAnnouncedDate(jsonObject.has("announced-date")?jsonObject.get("announced-date").getAsString():"");

            productList.add(product);
        }
        productsBufferedReader.close();
        productsReader.close();

        return productList;

    }

    public static HashMap<String, List<Listing>> getListingByManufacturer(File listingsFile) throws IOException{
        FileReader listingReader = new FileReader(listingsFile);
        BufferedReader listingBufferedReader = new BufferedReader(listingReader);
        String listingString;

        HashMap<String, List<Listing>> map = new HashMap<String, List<Listing>>();

        while ((listingString = listingBufferedReader.readLine())!= null){

            Gson gson = new Gson();
            Listing listing = gson.fromJson(listingString, Listing.class);

            JsonObject listingJsonObject = (new JsonParser()).parse(listingString).getAsJsonObject();

            if(map.containsKey(String.valueOf(listingJsonObject.get("manufacturer")).toUpperCase())){

                List<Listing> listingObject = map.get(String.valueOf(listingJsonObject.get("manufacturer")).toUpperCase());
                listingObject.add(listing);
                map.replace(String.valueOf(listingJsonObject.get("manufacturer")).toUpperCase(),listingObject);

            }else{

                List<Listing> listingObject = new ArrayList<Listing>();
                listingObject.add(listing);
                map.put(String.valueOf(listingJsonObject.get("manufacturer")).toUpperCase(),listingObject);

            }
        }
        listingBufferedReader.close();
        listingReader.close();

        return map;

    }
}