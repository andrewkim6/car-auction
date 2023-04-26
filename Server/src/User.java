
// Copy-paste this file at the top of every file you turn in.
/*
* EE422C Final Project submission by
* Replace <...> with your actual data.
* Andrew Kim
* AK46428
* 17150
* Spring 2023
*/
import java.io.Serializable;

public class User implements Serializable{
    private String username;
    private String password;

public User(String user, String pass){
    this.username = user;
    this.password = pass;
}

public String getUser(){
    return username;
}
public String getPass(){
    return password;
}
}
