package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Array;
import java.util.Arrays;
import java.util.Map;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {

        //parsingInput();
        System.out.println("Hello World!");
    }
//
//    public static void parsingInput() {
//        try {
//            File input_file = new File("C:/Users/la265/OneDrive/Desktop/Uni/ComputerScience/SPL/ex2/example_input.json");
//            JsonElement fileElement = JsonParser.parseReader(new FileReader(input_file));
//            JsonObject fileObject = fileElement.getAsJsonObject();
//
//
//            Integer tickTime = fileObject.get("TickTime").getAsInt();
//            Integer duration = fileObject.get("Duration").getAsInt();
//            System.out.println("TickTime = " + tickTime);
//            System.out.println("Duration = " + duration);
//
//
//            JsonArray jsonArrayOfStudents = fileObject.get("Students").getAsJsonArray();
//            for (JsonElement student : jsonArrayOfStudents) {
//                JsonObject studentJsonObject = student.getAsJsonObject();
//                String student_name = studentJsonObject.get("name").getAsString();
//                int name = studentJsonObject.;
//                String department;
//                String status;
//                Model[] modelsArray;
//            }

//            File input_file_path = new File("C:/Users/la265/OneDrive/Desktop/Uni/ComputerScience/SPL/ex2/example_input.json");
//            Reader reader = new FileReader(input_file_path);
//            Gson gson = new Gson();
//            JsonArray json=(JsonArray)gson.fromJson(reader, JsonElement.class);
//            for(int iterator=0;iterator<json.size();iterator++) {
//                JsonElement oElement=json.get(iterator);
//                Student[] st =gson.fromJson(reader,Student[].class);
//            }
////
////                System.out.println("s");
//            Gson gson = new Gson();
//            File input_file = new File("C:/Users/la265/OneDrive/Desktop/Uni/ComputerScience/SPL/ex2/example_input.json");
//            JsonElement fileElement = JsonParser.parseReader(new FileReader(input_file));
//            JsonObject fileObject = fileElement.getAsJsonObject();
//            JsonArray student_json_array = fileObject.get("Student").getAsJsonArray();
//            String json = "[{ 'name': 'lior','department':
//            private String department;
//            private Student.Degree status;}
//            Student[] studentArray = gson.fromJson(student_json_array,Student[].class);
//            System.out.println(studentArray.length);
//
//        } catch (Exception ex) {
//        }
//    }
}
