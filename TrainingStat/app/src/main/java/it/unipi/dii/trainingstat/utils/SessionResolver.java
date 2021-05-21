package it.unipi.dii.trainingstat.utils;

import it.unipi.dii.trainingstat.DatabaseManager;
import it.unipi.dii.trainingstat.entities.TrainingSession;
import it.unipi.dii.trainingstat.entities.UserSession;
import it.unipi.dii.trainingstat.utils.exeptions.*;

public class SessionResolver {
    public static final int RUNNING_TRAINING_SESSION = 0;
    public static final int RUNNING_INDIVIDUAL_SESSION = 1;
    public static final int RUNNING_COLLECTIVE_SESSION = 2;
    public static final int TERMINATED_TRAINING_SESSION = 3;
    public static final int TERMINATED_INDIVIDUAL_SESSION = 4;
    public static final int TERMINATED_COLLECTIVE_SESSION = 5;

    private static final DatabaseManager databaseManager = new DatabaseManager();

    // returns type of session and the state of it
    public static int classifyUserSessionFromTrainingSessionId(String username, TrainingSession trainingSession) throws TrainingSessionNotFound, UserSessionNotFound {
        if(trainingSession == null){
            throw new IllegalArgumentException("[trainingSessionId] is null");
        }
        if(username == null || username ==""){
            throw new IllegalArgumentException("[username] is null or empty");
        }

        String trainer = getTrainerUsernameFromTrainingSession(trainingSession.getId());

        UserSession userSession = trainingSession.getSessionOfUser(username);
        if (userSession == null){
            throw new UserSessionNotFound(username);
        }

        if(isIndividualSession(trainingSession)){
            if(userSession.getStatus() == UserSession.STATUS_TERMINATED){
                return TERMINATED_INDIVIDUAL_SESSION;
            }
            return RUNNING_INDIVIDUAL_SESSION;
        }

        if(username != trainer){
            // COLLECTIVE_SESSION
            if(userSession.getStatus() == UserSession.STATUS_TERMINATED){
                return TERMINATED_COLLECTIVE_SESSION;
            }
            return RUNNING_COLLECTIVE_SESSION;
        }

        //TRAINER_SESSION
        if(trainingSession.getStatus() == TrainingSession.STATUS_TERMINATED){
            return TERMINATED_TRAINING_SESSION;
        }
        return RUNNING_TRAINING_SESSION;
    }

    public static boolean isIndividualSession(TrainingSession trainingSession){
        if(trainingSession == null){
            throw new IllegalArgumentException("[trainingSession] is null");
        }
        String trainer = getTrainerUsernameFromTrainingSession(trainingSession.getId());
        UserSession userSession = trainingSession.getSessionOfUser(trainer);
        return userSession != null && trainingSession.getUserSessions().keySet().stream().count() == 1;
    }

    public static TrainingSession getTrainingSession(String trainingSessionId) throws TrainingSessionNotFound {
        if(trainingSessionId == null || trainingSessionId ==""){
            throw new IllegalArgumentException("[trainingSessionId] is null or empty");
        }
        TrainingSession ts = new TrainingSession();
        databaseManager.getTrainingSession(trainingSessionId,ts::copyFrom);
        if(ts.getId() == null || ts.getId() ==""){
            throw new TrainingSessionNotFound(trainingSessionId);
        }
        return ts;
    }

    public static String getTrainerUsernameFromTrainingSession(String trainingSessionId){
        if(trainingSessionId == null || trainingSessionId ==""){
            throw new IllegalArgumentException("[trainingSessionId] is null or empty");
        }
        String username = trainingSessionId.substring(0, trainingSessionId.lastIndexOf("_"));
        return username;
    }
}
