package com.driver;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@Service
public class WhatsappService {
    WhatsappRepository whatsappRepository = new WhatsappRepository();
    public String createUser(String userName, String mobileNo) throws Exception{

        return whatsappRepository.createUser(userName,mobileNo);

    }

    // Creating the New Group
    public Group createGroup(List<User> users){
       return whatsappRepository.createGroup(users);
    }

    // Creating the New MEssage
    public int createMessage(String content){
        return  whatsappRepository.createMessage(content);
    }

    // Sending Message to Group
    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.

        return whatsappRepository.sendMessage(message, sender, group);
    }

    // Changing Admin
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.

        return whatsappRepository.changeAdmin(approver, user, group);
    }

    // Removing User
    public int removeUser(User user) throws Exception{
        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)

        return whatsappRepository.removeUser(user);
    }
}
