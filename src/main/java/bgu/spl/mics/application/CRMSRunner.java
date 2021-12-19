package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: CRMSRunner [config_file] [output_file]");
            return;
        }
        // TODO Remove time testing before submitting
        long start_time = System.nanoTime();
        ConfigInformation config_info = JsonParser.parseInput(args[0]);
        InitializerService initializerService = new InitializerService(config_info);
        initializerService.run();
        long end_time = System.nanoTime();

        // Print the results
        Cluster.getInstance().summarize();
        JsonParser.generateOutput(args[1], config_info.getStudentArray(), config_info.getConferenceArray(),
                Cluster.getInstance().getStats().getTotalCPUTime(),
                Cluster.getInstance().getStats().getTotalGPUTime(),
                Cluster.getInstance().getStats().getTotalBatches());

        System.out.println((end_time - start_time)  / 1000000);
    }
}




