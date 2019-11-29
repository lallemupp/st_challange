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

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DatabaseMessageService implements MessageService {
    @Autowired
    MessageDao messageDao;

    @Override
    public Message describe(String messageId) {
        return messageDao.get(messageId);
    }

    @Override
    public List<Message> messagesWrittenBy(String user) {
        return messageDao.messagesWrittenBy(user);
    }

    @Override
    public List<Message> all() {
        return messageDao.all();
    }

    @Override
    public String create(String message, String user) {
        return messageDao.add(message, user);
    }

    // TODO: update updated field.
    @Override
    public String update(Message message, String user) throws MessageNotFoundException {
        return messageDao.update(message.getId(), message.getMessage());
    }
}
