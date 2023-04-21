package client;

public class User {
    private String username;
    private int password;

public User(String user, int pass){
    this.username = user;
    this.password = pass;
}

public String getUser(){
    return username;
}
public int getPass(){
    return password;
}
}
