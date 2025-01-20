package com.example.arcadeapp;

public class UserSession  {

    private static UserSession  instance;
    private String username;
    private String userId;
    private int score;


    private UserSession(){}

    public static UserSession  getInstance(){
        if(instance == null)
        {
            instance = new UserSession ();
        }
        return instance;
    }


    public void setUsername(String username){
        this.username = username;

    }

    public String getUsername()
    {
        return this.username;
    }

    public void setScore(int score){

        this.score =score;

    }

    public int getScore() {
        return this.score;
    }


    public  String getUserId(){
        return userId;
    }

    public String  setUserId() {
        return this.userId;
    }

    public  void clearSession(){

        username = null;
        userId = null;
        score = 0;
    }
}
