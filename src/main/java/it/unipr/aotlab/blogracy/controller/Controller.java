package it.unipr.aotlab.blogracy.controller;

import it.unipr.aotlab.blogracy.model.messages.Message;
import it.unipr.aotlab.blogracy.model.users.User;

/**
 * User: enrico
 * Package: it.unipr.aotlab.blogracy.controller
 * Date: 10/27/11
 * Time: 1:32 PM
 */
public interface Controller {
    void sendMessage(Message m);
    boolean follow(User u);
    boolean unfollow(User u);
}
