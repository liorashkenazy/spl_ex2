package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import java.util.LinkedList;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConferenceInformation {

    private String name;
    private int date;
    private LinkedList<Model> models_to_publish;
    private int ticks_left;

    /**
     * <p>
     * @param name the name of the conference.
     * @param date the time of the conference.
     */
    public ConferenceInformation(String name, int date) {
        this.name = name;
        this.date = date;
        this.models_to_publish = new LinkedList<>();
        ticks_left = date;
    }

    /**
     * Aggregate {@code model} to hashmap of successful {@link Model}s.
     * <p>
     * @param model The model to aggregate.
     * @PRE: {@code model} != null
     * @POST: if (model.isResultGood())
     *          models_to_publish.contains(model)
     * @POST: if(!model.isResultGood())
     *          models_to_publish.contains(model) == false;
     *
     */
    public void addModelToConference(Model model) {
        if (model.isResultGood()) {
            models_to_publish.add(model);
        }
    }

    /**
     * This function should be called every time a tick occurs.
     * <p>
     * @POST: @PRE(getTicksLeft()) - 1 == getTicksLeft()
     * @POST: getTicksLeft() >= 0
     */
    public void tick() {
        ticks_left --;
    }

    /**
     * This function returns the number of ticks left to publish the successful {@link Model}s
     * <p>
     * @return [int] The number of remaining ticks to send {@link PublishConferenceBroadcast}
     * @INV getTicksLeft() >= 0;
     * @POST: @PRE(getTickLeft()) == getTickLeft()
     */
    public int getTicksLeft() {
        return ticks_left;
    }

    /**
     * <p>
     * @return [LikedList] linked list that contains successful {@link Model}s
     * that will be published via {@link PublishConferenceBroadcast}
     */
    public LinkedList<Model> getModels() {
        return models_to_publish;
    }

    public String toString() {
        return "conference name: " + name + " date: " + date + "\n";
    }
}
