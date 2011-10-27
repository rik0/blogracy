package it.unipr.aotlab.userRss.controller;

import it.unipr.aotlab.userRss.model.messages.Message;
import it.unipr.aotlab.userRss.model.users.User;

/**
 * User: enrico
 * Package: it.unipr.aotlab.userRss.controller
 * Date: 10/27/11
 * Time: 1:32 PM
 */
public interface Controller {
    void sendMessage(Message m);
    boolean follow(User u);
    boolean unfollow(User u);
}
