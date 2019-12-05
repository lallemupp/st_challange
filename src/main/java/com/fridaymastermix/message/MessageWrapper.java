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
 * A class used to wrap message lists in JSON responses.
 */
public class MessageWrapper {

    private List<Message> messages;

    /**
     * Constructor.
     *
     * @param messages the list of messages to wrapp.
     */
    public MessageWrapper(List<Message> messages) {
        this.messages = messages;
    }

    /**
     * Returns the list of messages.
     *
     * @return the list of messages.
     */
    public List<Message> getMessages() {
        return messages;
    }
}
