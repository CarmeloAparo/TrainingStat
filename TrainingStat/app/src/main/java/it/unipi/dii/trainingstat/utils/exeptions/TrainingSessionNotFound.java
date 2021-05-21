package it.unipi.dii.trainingstat.utils.exeptions;

public class TrainingSessionNotFound extends Exception {
    public TrainingSessionNotFound(String trainingSessionId) {
        super(trainingSessionId);
    }
}