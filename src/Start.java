import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.URL;
import java.time.MonthDay;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;




/**
 * Created by catalina rotaru on 22/05/17.
 */
public class Start {


    private static JSONParser parser = new JSONParser();

    private static ArrayList array_list_items = new ArrayList();
    private static ArrayList array_list_box = new ArrayList();

    private static String[] list_with_items;
    private static String[] list_with_boundingBox ;

    private static  ArrayList client_var = new ArrayList();
    private static ArrayList client_boxes = new ArrayList();
    private static String global_workspace;


    /**
     * Method for downloading the image in your computer based on the URL.
     *
     * @return
     */
    private static Image obtainImageFromURL(int numberOfDownloads)
    {

        Image image = null;
        BufferedImage buf_image ;
        int i = 0;

//        while(i < numberOfDownloads) {
            try {
                URL url = new URL("http://rockthehackathons.com:5000/api/fax?app=BLK");
                image = ImageIO.read(url);
                buf_image = (BufferedImage) image;

//                if(buf_image == null){
//                    System.out.println("This buffer image is null. Connection not established correctly with the server");
//                }
//                else {
//                    ImageIO.write((RenderedImage) image, "png", new File( global_workspace + "imageFromURL.png"));
//                }
//                i++;

            } catch (IOException e) {
            }
    //    }

            return image;
    }





     private static String obtainInformationFromJASON(String link) throws IOException, ParseException {
         String objectToFind = null ;
         String name_of_client = null;

         Object obj  = parser.parse(new FileReader(link));
         JSONArray array = new JSONArray();
         array.add(obj);



         for (Object o : array) {
             JSONObject interior_regions = (JSONObject) o;

             JSONArray interior = (JSONArray) interior_regions.get("Regions");

             for (Object c  : interior){
                 JSONObject ches = (JSONObject) c;

                 JSONArray interior_lines = (JSONArray) ches.get("Lines");


                 for(Object d : interior_lines) {
                     JSONObject words = (JSONObject) d;

                     JSONArray interior_words = (JSONArray) words.get("Words");

                     for(Object e : interior_words) {
                         JSONObject text = (JSONObject) e;

                         String name2 = (String) text.get("Text");
                         array_list_items.add(name2);
                         array_list_box.add((String) text.get("BoundingBox"));
                         System.out.println(name2);
                     }
                 }
             }


             objectToFind = "It works!!";

         }

         return objectToFind;
     }


    /**
     * Method for transferring the values from an arraylist to a normal array
     * in order to be able to use an index as the same position to extract both the text and it's corresponding
     * bounding box.
     * Another solution to this aspect would have been the use of a Hashmap <String, String> for <Text, Bounding Box>
     */
    private static void transferToArray(){
         list_with_items = new String[array_list_items.size()];
         list_with_boundingBox = new String[array_list_box.size()];
         int i =0;
         int j =0;

         for(Object item : array_list_items){
             list_with_items[i] = (String) item;
             i++;
         }
         for (Object box : array_list_box){
             list_with_boundingBox[j] = (String) box;
             j++;
         }
     }

    /**
     * Method for obtaining the bounding box of the client.
     * @return
     */
     private static String getTheClientBox(){

         String name = null;


         for(int i = 0; i <list_with_items.length; i++){
             if(list_with_items[i].matches("Client")){
                 String box = list_with_boundingBox[i];
                 name = box;
                 client_var.add(box);

             }

         }
         return  name;
     }

    /**
     * Method for extracting the client's name for the case when the client's name is on the same line with the
     * word Client. It takes the bounding boxes and extracts the text which the line value is the same with the
     * line value of the variable from the word Client.
     * @return
     */
     private static String getTheClientName(){

         String final_name = "";

         for(Object client_box : client_var){
             String box_string = (String) client_box;

             String[] numbers = box_string.split(",");
             int[] box_positions = new int[numbers.length];

             for (int j = 0; j < numbers.length; j++) {
                 box_positions[j] = Integer.parseInt(numbers[j]);
             }

             for(int k = 0; k < list_with_boundingBox.length; k++){
                 String box_list_string = list_with_boundingBox[k];

                 String[] number_box_list = box_list_string.split(",");
                 int[] box_positions_list = new int[number_box_list.length];

                 for (int l = 0; l < numbers.length; l++) {
                     box_positions_list[l] = Integer.parseInt(number_box_list[l]);
                 }

                 if(box_positions[1] == box_positions_list[1]){
                     if(list_with_items[k].matches("Client") || list_with_items[k].matches("Signature") || list_with_items[k].matches("Name:")) {

                     }else{

                         final_name = final_name + " " + list_with_items[k];
                         client_boxes.add(list_with_boundingBox[k]);

                         System.out.println("Name of Client:  " + list_with_items[k]);

                     }

                 }
             }

         }

         return final_name;
     }


    /**
     * Method for extracting the coordinates of the bounding box from a String and adding some values to them in order to
     * extract the signature.
     * @param input
     * @return
     */
     private static int[] boundingBoxForExtraction(int[] input){
         int[][] array_of_boxes = new int[client_boxes.size()][4];
         int i = 0;
         for(Object box : client_boxes){
             String box_string = (String) box;

             String[] numbers = box_string.split(",");
             int[] entry_box = new int[numbers.length];

             for (int j = 0; j < numbers.length; j++) {
                 entry_box[j] = Integer.parseInt(numbers[j]);
             }

             for( int k = 0; k < entry_box.length; k++){
                 array_of_boxes[i][k] = entry_box[k];
             }
             i++;

         }
         System.out.println("The moment of truth: " + array_of_boxes[0][0] + " " + array_of_boxes[0][1] + " " +array_of_boxes[0][2]);

         //case one under the name of the client;
         input[0] = array_of_boxes[0][0] ;
         input[1] = array_of_boxes[0][1] + 150;
         input[2] = array_of_boxes[0][2] + 100;
         input[3] = array_of_boxes[0][3] + 70;


         return input;
     }


    /**
     * Method for cropping a part of a bufferedImage taken as input on the exact coordinates passed by the integer
     * array int[] values.
     * @param src
     * @param values
     * @return
     * @throws IOException
     */
    private static BufferedImage cropImage(BufferedImage src, int[] values ) throws IOException {

        int x = values[0];
        int y = values[1];
        int z = values[2];
        int w = values[3];
        BufferedImage dest = src.getSubimage(x, y, z, w);

        File outputfile = new File(global_workspace + "output.png");
        ImageIO.write(dest, "png", outputfile);

        return dest;
    }

    /**
     * Method for receiving the JSON of an image passed as a URL.
     */
    private static void extractJSONforImage(){
        HttpClient httpclient = new DefaultHttpClient();

        try
        {
            URIBuilder builder = new URIBuilder("https://westus.api.cognitive.microsoft.com/vision/v1.0/analyze");

            builder.setParameter("visualFeatures", "Categories");
            builder.setParameter("details", "Celebrities");
            builder.setParameter("language", "en");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);

            // Request headers - replace this example key with your valid subscription key.
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", "2989de3bdee1494ebc93ef45bf2ae234");

            // Request body. Replace the example URL with the URL for the JPEG image of a celebrity.
            StringEntity reqEntity = new StringEntity("{\"url\":\"http://example.com/images/test.jpg\"}");
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                System.out.println(EntityUtils.toString(entity));
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }


    public static void main(String args[]) throws Exception{

        //Download images from URL and store them into your workspace.
       // int numberOfDownloads = Integer.parseInt(args[0]);
       // String workspace = args[1];

        int numberOfDownloads = 1;
        String workspace = "/home/catalina/Desktop/Test";
        global_workspace = workspace;


        Image imageget = null;
        imageget = obtainImageFromURL(1);

        if(imageget == null){
            System.out.println("This buffer image is null. Connection not established correctly with the server");

        }
        else
        {
            System.out.println("Write the last image into a file again.");
            ImageIO.write((RenderedImage) imageget, "png",new File(workspace + "/imageFromURL.png"));

        }

        //Send the command in order to extract the JSON - small example
        extractJSONforImage();

        //Reading images from the workspace and storing them as BufferImages.
        //Using the universal index to keep track of the performance.
        Image image_fax = ImageIO.read(new File(workspace + "/trial.png"));
        BufferedImage buffered_fax = (BufferedImage) image_fax;


        //Taking the JSON from the working space and process them to extract what is needed (signature and customer name).
        String name = obtainInformationFromJASON( workspace + "/trial.json");
        System.out.println(name);


        //Obtain the client name and the boxing values.
        transferToArray();
        String hey = getTheClientBox();

        String name_client = getTheClientName();

        System.out.println("The Client name is the following: " + name_client);

        int[] trial = new int[4];
        int[] extract = new int[4];
        extract = boundingBoxForExtraction(trial);

        int[] box = new int[4];
        box[0] = 411;
        box[1] = 434;
        box[2] = 63;
        box[3] = 12;
        BufferedImage buffered_cropped_image ;

        //Function for cropping the specific region of the buffer image.
        buffered_cropped_image = cropImage(buffered_fax, extract);


    }

}
