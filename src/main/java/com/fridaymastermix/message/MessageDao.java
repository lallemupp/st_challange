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

import java.util.List;

/**
 * Interface for Data Access Objects that manipulates message data.
 */
public interface MessageDao {
    /**
     * Returns the message with the provided id or {@link Message#NONEXISTING} if the message can not be found.
     * @param id the id of the message.
     * @return the message or {@link Message#NONEXISTING}.
     */
    Message get(String id);

    /**
     * Checks if the message exists or not.
     *
     * @param message the id of the message.
     * @param forUser the user that created the message.
     * @return true if the message exists, false otherwise.
     */
    boolean exists(String message, String forUser);

    /**
     * Returns a list of all messages written by the user.
     * @param user the user.
     * @return A list of all messages written by the user.
     */
    List<Message> messagesWrittenBy(String user);

    /**
     * Returns a list of all messages.
     *
     * @return a list of all messages.
     */
    List<Message> all();

    /**
     * Adds a message.
     *
     * @param message the message to add.
     * @param user the user that adds the message.
     * @return the newly created id of the message.
     */
    String add(String message, String user);

    /**
     * Updates the content of a message.
     *
     * @param id the id of the message to update.
     * @param message the content to update to.
     * @throws MessageNotFoundException if there is no message to update.
     */
    void update(String id, String message) throws MessageNotFoundException;

    /**
     * Deletes a message.
     *
     * @param message the id of the message.
     * @param forUser the user that created the message.
     * @return true if the message was deleted, false otherwise.
     */
    boolean delete(String message, String forUser);
}
