package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
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
    private static Student[] student_array;
    private static GPU[] gpus_array;
    private static CPU[] cpus_array;
    private static ConferenceInformation[] conferences_array;
    public static int tick_time;
    public static int duration;

    public static void main(String[] args) {
        parsingInput();
        for(int i=0; i<student_array.length; i++){
            student_array[i].setStudentForModels();
            Thread student_thread = new Thread(new StudentService("StudentService"+i, student_array[i]));
            student_thread.start();
        }
        for(int i=0; i<gpus_array.length; i++){
            Thread gpu_thread = new Thread(new GPUService("GpuService"+i,gpus_array[i]));
            gpu_thread.start();
        }
        for(int i=0; i<cpus_array.length; i++){
            Thread cpu_thread = new Thread(new CPUService("CpuService"+i,cpus_array[i]));
            cpu_thread.start();
        }
        for(int i=0; i< conferences_array.length; i++){
            Thread conference_thread = new Thread(new ConferenceService("ConferenceService"+i,conferences_array[i]));
            conference_thread.start();
        }
        Thread time_thread = new Thread(new TimeService(tick_time,duration));
        time_thread.start();
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
            student_array = gson.fromJson(student_json_array.toString(),Student[].class);

            // Extracting GPU array
            JsonArray gpus_json_array = fileObject.get("GPUS").getAsJsonArray();
            gpus_array = new GPU[gpus_json_array.size()];
            int i=0;
            for (JsonElement gpu : gpus_json_array) {
                gpus_array[i] = new GPU(gpu.getAsString());
                i++;
            }

            // Extracting CPU array
            JsonArray cpus_json_array = fileObject.get("CPUS").getAsJsonArray();
            cpus_array = new CPU[cpus_json_array.size()];
            i = 0;
            for (JsonElement cpu : cpus_json_array) {
                cpus_array[i] = new CPU(cpu.getAsInt());
                i++;
            }

            // Extracting Conferences
            JsonArray conferences_json_array = fileObject.get("Conferences").getAsJsonArray();
            conferences_array = gson.fromJson(conferences_json_array.toString(),ConferenceInformation[].class);

            // Extracting Tick Time
            tick_time = fileObject.get("TickTime").getAsInt();

            // Extracting Duration
            duration = fileObject.get("Duration").getAsInt();

        } catch (Exception e){System.out.println(e);}
    }
}
