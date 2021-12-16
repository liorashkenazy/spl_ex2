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
                i++;
            }

            // Extracting Conferences
            JsonArray conferences_json_array = fileObject.get("Conferences").getAsJsonArray();
            ConferenceInformation[] conferences_array = gson.fromJson(conferences_json_array.toString(),ConferenceInformation[].class);

            // Extracting Tick Time
            int tick_time = fileObject.get("TickTime").getAsInt();

            // Extracting Duration
            int duration = fileObject.get("Duration").getAsInt();

        } catch (Exception e){System.out.println(e);}
    }
}
