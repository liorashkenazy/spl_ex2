package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Model;

public class TrainModelFinished implements Broadcast {

    private Model model;

    public TrainModelFinished(Model model) {
        this.model = model;
    }

    public Model getModel() {
        return model;
    }
}
