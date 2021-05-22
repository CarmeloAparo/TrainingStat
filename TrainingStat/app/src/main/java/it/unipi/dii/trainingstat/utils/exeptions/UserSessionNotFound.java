package it.unipi.dii.trainingstat.utils.exeptions;

public class UserSessionNotFound extends Exception {
    public UserSessionNotFound(String username) {
        super(username);
    }
}
