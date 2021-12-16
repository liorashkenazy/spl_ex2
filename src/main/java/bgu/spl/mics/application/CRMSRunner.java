package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Student;
import com.google.gson.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;


/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {

        parsingInput();
        System.out.println("Hello World!");
    }

    private static void parsingInput() {
        File input_file = new File("C:\\tmp\\example_input.json");
        try {
            JsonElement fileElement = JsonParser.parseReader(new FileReader(input_file));
            JsonObject fileObject = fileElement.getAsJsonObject();
            Gson gson = new Gson();

            // Extracting students array
            JsonArray student_json_array = fileObject.get("Students").getAsJsonArray();
            Student[] student_array = gson.fromJson(student_json_array.toString(),Student[].class);

            // Extracting GPU array
            JsonArray gpus_json_array = fileObject.get("GPUS").getAsJsonArray();
            GPU[] gpus_array = new GPU[gpus_json_array.size()];
            int i=0;
            for (JsonElement gpu : gpus_json_array) {
                gpus_array[i] = new GPU(gpu.getAsString());
                i++;
            }

            // Extracting CPU array
            JsonArray cpus_json_array = fileObject.get("CPUS").getAsJsonArray();
            CPU[] cpus_array = new CPU[cpus_json_array.size()];
            i = 0;
            for (JsonElement cpu : cpus_json_array) {
                cpus_array[i] = new CPU(cpu.getAsInt());
                System.out.println(cpus_array[i].toString());
                i++;
            }

            // Extracting Conferences
            JsonArray conferences_json_array = fileObject.get("Conferences").getAsJsonArray();
            ConferenceInformation[] conferences_array = gson.fromJson(conferences_json_array.toString(),ConferenceInformation[].class);

            // Extracting Tick Time
            int tick_time = fileObject.get("TickTime").getAsInt();

            // Extracting Duration
            int duration = fileObject.get("Duration").getAsInt();

//            JsonArray cpu_json_array = fileObject.get("CPUS").getAsJsonArray();
//            System.out.println(cpu_json_array.toString());

        } catch (Exception e){System.out.println(e);}

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
