package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import com.google.gson.*;
import com.google.gson.annotations.Expose;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

public class JsonParser {

    private static class OutputInfo {
        private Student[] students;
        private ConferenceInformation[] conferences;
        private int cpuTimeUsed;
        private int gpuTimeUsed;
        private int batchesProcessed;

        public OutputInfo(Student[] students,
                          ConferenceInformation[] conferences,
                          int cpu_time,
                          int gpu_time,
                          int batches_processed) {
            this.students = students;
            this.conferences = conferences;
            this.cpuTimeUsed = cpu_time;
            this.gpuTimeUsed = gpu_time;
            this.batchesProcessed = batches_processed;
        }
    }

    public static void generateOutput(String output_file, Student[] students, ConferenceInformation[] conferences,
                                      int cpu_time, int gpu_time, int batches_processed) {
        // Make sure we include prints empty lists
        for (Student s : students) {
            s.getTrainedModels();
        }
        GsonBuilder gson_builder = new GsonBuilder();
        gson_builder.setPrettyPrinting();
        // We want to prevent an endless loop (Models -> Student -> Model) so we prevent serialization of the Model
        // field using
        gson_builder.addSerializationExclusionStrategy(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                // Get the serialization annotation, we want to avoid having to declare it on every single variable
                // So if it is declared, check the value, otherwise, assume it should be serialized
                Expose ant = f.getAnnotation(Expose.class);
                if (ant != null)
                    return !ant.serialize();
                else
                    return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> c) {
                return false;
            }
        });
        Gson gson = gson_builder.create();
        try {
            OutputInfo inf = new OutputInfo(students, conferences, cpu_time, gpu_time, batches_processed);
            FileWriter f = new FileWriter(output_file);
            gson.toJson(inf, f);
            f.flush();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ConfigInformation parseInput(String file_path) {
        File input_file = new File(file_path);
        try {
            JsonElement fileElement = com.google.gson.JsonParser.parseReader(new FileReader(input_file));
            JsonObject fileObject = fileElement.getAsJsonObject();
            GsonBuilder gson_builder = new GsonBuilder();
            JsonDeserializer<Model> model_deserializer = new JsonDeserializer<Model>() {
                @Override
                public Model deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                    JsonObject obj = jsonElement.getAsJsonObject();

                    return new Model(obj.get("name").getAsString(),
                            obj.get("type").getAsString(),
                            obj.get("size").getAsInt());
                }
            };
            gson_builder.registerTypeAdapter(Model.class, model_deserializer);
            JsonDeserializer<ConferenceInformation> conf_deserializer = new JsonDeserializer<ConferenceInformation>() {
                @Override
                public ConferenceInformation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                    JsonObject obj = jsonElement.getAsJsonObject();

                    return new ConferenceInformation(obj.get("name").getAsString(),
                            obj.get("date").getAsInt());
                }
            };
            gson_builder.registerTypeAdapter(ConferenceInformation.class, conf_deserializer);
            Gson gson = gson_builder.create();

            // Extracting students array
            JsonArray student_json_array = fileObject.get("Students").getAsJsonArray();
            Student[] student_array = gson.fromJson(student_json_array.toString(),Student[].class);
            // Extracting GPU array
            JsonArray gpus_json_array = fileObject.get("GPUS").getAsJsonArray();
            GPU[] gpu_array = new GPU[gpus_json_array.size()];
            int i=0;
            for (JsonElement gpu : gpus_json_array) {
                gpu_array[i] = new GPU(gpu.getAsString());
                i++;
            }
            // Extracting CPU array
            JsonArray cpus_json_array = fileObject.get("CPUS").getAsJsonArray();
            CPU[] cpu_array = new CPU[cpus_json_array.size()];
            i = 0;
            for (JsonElement cpu : cpus_json_array) {
                cpu_array[i] = new CPU(cpu.getAsInt());
                i++;
            }
            // Extracting Conferences
            JsonArray conferences_json_array = fileObject.get("Conferences").getAsJsonArray();
            ConferenceInformation[] conference_array = gson.fromJson(conferences_json_array.toString(),
                    ConferenceInformation[].class);
            // Extracting Tick Time
            int tick_time = fileObject.get("TickTime").getAsInt();
            // Extracting Duration
            int duration = fileObject.get("Duration").getAsInt();
            return new ConfigInformation(student_array, gpu_array, cpu_array, conference_array, tick_time, duration);
        } catch (Exception e) {
            return null;
        }
    }
}
