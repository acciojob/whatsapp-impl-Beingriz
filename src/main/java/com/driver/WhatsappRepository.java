package com.driver;

import java.util.*;

import io.swagger.models.auth.In;
import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> Groups_Db;
    private HashMap<Group, List<Message>> GroupMessages_Db;
    private HashMap<Message, User> SenderMsg_Db;
    private HashMap<Group, User> Admin_Db;
    private HashSet<String> userMobile;
    private HashMap<String, User> User_Db;
    private HashMap<User, Group> UserGroups_DB;
    private HashMap<Integer, Message> AllMessages_Db;
    private int customGroupCount;
    private int messageId;


    public WhatsappRepository(){
        this.GroupMessages_Db = new HashMap<Group, List<Message>>();
        this.Groups_Db = new HashMap<Group, List<User>>();
        this.SenderMsg_Db = new HashMap<Message, User>();
        this.Admin_Db = new HashMap<Group, User>();
        this.UserGroups_DB = new HashMap<User, Group>();
        this.AllMessages_Db = new HashMap<Integer, Message>();
        this.userMobile = new HashSet<>();
        this.User_Db = new HashMap<>();

        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String userName, String mobileNo) throws Exception{
        if(userMobile.contains(mobileNo)) {
            throw new Exception("User already exists");
        }
        userMobile.add(mobileNo);
        User_Db.put(mobileNo, new User(userName, mobileNo));
        return "SUCCESS";
    }

    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.
        Group grp = null;
        if(users.size()==2){
            // Passing Group Name & No of Participants in the Group
            grp = new Group(users.get(1).getName(), users.size()); // Personal Chat
            Groups_Db.put(grp, users); // Personal Chat
            User admin = users.get(0); // Getting first user  as admin
            Admin_Db.put(grp, admin); // Mapping Admin to the group
            // Mapping 1 User to 1 Group only
            UserGroups_DB.put(users.get(0), grp);
            UserGroups_DB.put(users.get(1), grp);
        }else if(users.size()>2){
            if(this.customGroupCount == 0) this.customGroupCount = 1;
            String grpName = "Group "+ this.customGroupCount;
            grp = new Group(grpName, users.size()); // Group is Created
            this.customGroupCount++; // Incrementing the count of groups
            Groups_Db.put(grp, users); // Added to Group DB
            User admin = users.get(0); // Getting first user as admin
            Admin_Db.put(grp, admin); // Mapping Admin to the group

           // Mapping Users to Single Group
            for (int i = 0; i < users.size(); i++) {
                UserGroups_DB.put(users.get(i), grp);
            }
        }
        return grp;
    }

    // Creating new Message
    public int createMessage(String content){
        Message msg = new Message(content);
        AllMessages_Db.put(msg.getId(),msg);
        this.messageId++;
        return this.messageId;
    }

    // Sending Message to Group
    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        int count = 0;
        if(!Groups_Db.containsKey(group)){
            throw new Exception("Group does not exist");
        }else {
            List<User> users = Groups_Db.get(group);
            if(!users.contains(sender)){
                throw new Exception("You are not allowed to send message");
            }
        }
        Message msg = new Message(message.getContent());
        AllMessages_Db.put(msg.getId(),msg);
        this.messageId++;
        List<Message> mesgs = GroupMessages_Db.get(group);
        mesgs.add(msg);
        count = mesgs.size();
        SenderMsg_Db.put(msg,sender);
        return count;
    }

    // Changing Admin
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.

        if(!Groups_Db.containsKey(group)){
            throw  new Exception("Group does not exist");
        }else{
            User admin = Admin_Db.get(group);
            if(!admin.getName().equals(approver.getName())){
                throw new Exception("Approver does not have rights");
            }else{
                List<User> users = Groups_Db.get(group);
                if(!users.contains(user)){
                    throw new Exception("User is not a participant");
                }
            }
        }
        Admin_Db.put(group,user);
        return "SUCCESS";
    }

    //Removing User
    public int removeUser(User user) throws Exception{
        //This is a bonus problem and does not contain any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        int noOfUsers = 0,noOfMsgs=0,allMsgs=0;
        if(!UserGroups_DB.containsKey(user)){
            throw new Exception("User not found");
        }
        Group grp = UserGroups_DB.get(user);
        if(Admin_Db.containsKey(grp)){
            throw new Exception("Cannot remove admin");
        }
        // Removing User from Group
        List<User> users = Groups_Db.get(grp);
        users.remove(user);
        Groups_Db.put(grp,users);
        noOfUsers = Groups_Db.get(grp).size(); //  No of Users Left After Removal in that Group.

        // Removing Messages from Group of this User by this User
        List<Message> msgs = GroupMessages_Db.get(grp);

        User_Db.remove(user.getMobile());
        UserGroups_DB.remove(user);


        // Removing Messages by User
        for(Map.Entry<Message,User> entry : SenderMsg_Db.entrySet()){
            if(entry.getValue().getName().equals(user.getName())){
                Message msg = entry.getKey();
                SenderMsg_Db.remove(msg);
                msg.setId(msg.getId()-1);
                msgs.remove(msg);
                if(AllMessages_Db.containsKey(msg.getId())) AllMessages_Db.remove(msg.getId());

            }
        }
        noOfMsgs = msgs.size();
        allMsgs = AllMessages_Db.size();
        return noOfUsers + noOfUsers + allMsgs;

    }

}
