/*
 *    Copyright 2019 Love Löfdahl
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.fridaymastermix.message;

import com.fridaymastermix.user.User;
import com.fridaymastermix.user.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service that coordinates message requests.
 * This is where user validation should be done if implemented.
 */
@Service
@Qualifier("database")
public class MessageService {
    @Autowired
    @Qualifier("redis")
    MessageDao messageDao;

    @Autowired
    @Qualifier("redis")
    UserDao userDao;

    /**
     * Describes a message.
     * If no message can be found an {@link Message#NONEXISTING} message will be returned.
     *
     * @param messageWithId the id of the message to describe.
     * @return the message.
     */
    public Message describe(String messageWithId) {
        return messageDao.get(messageWithId);
    }

    /**
     * Returns a list of all message by the user.
     *
     * @param user the user.
     * @return a list of messages.
     */
    public List<Message> writtenBy(String user) {
        return messageDao.messagesWrittenBy(user);
    }

    /**
     * Returns all known messages.
     * This method should be paged in real life.
     *
     * @return a list of all messages.
     */
    public List<Message> all() {
        return messageDao.all();
    }

    /**
     * Creates a message for the user and returns the new message id.
     *
     * @param message the message to create.
     * @param user the user to create the message for.
     * @return the id of the newly created message.
     *
     * @throws UserNotFoundException if the user does not exist in the system. This is not needed when access to the api
     * is protected since an unauthorized user would not be able to create a message.
     */
    public String create(String message, String user) throws UserNotFoundException {
        if (userDao.get(user) != User.NONEXISTING) {
            return messageDao.add(message, user);
        } else {
            throw new UserNotFoundException(String.format("user %s was not found. Message will not be created", user));
        }
    }

    /**
     * Updates a message.
     *
     * @param message contains the id and the message to update.
     * @throws MessageNotFoundException if the message could not be found in the system.
     */
    public void update(Message message) throws MessageNotFoundException {
        messageDao.update(message.getId(), message.getMessage());
    }

    /**
     * Deletes a message for a user.
     *
     * @param message the id of the message to delete.
     * @param forUser the user to delete the message for.
     *
     * @return true if the message was deleted, false otherwise.
     */
    public boolean delete(String message, String forUser) {
        if (messageDao.exists(message, forUser)) {
            return messageDao.delete(message, forUser);
        } else {
            return false;
        }
    }
}
