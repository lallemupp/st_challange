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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Qualifier("database")
public class DatabaseMessageService implements MessageService {
    @Autowired
    @Qualifier("redis")
    MessageDao messageDao;

    @Override
    public Message describe(String messageId) {
        return messageDao.get(messageId);
    }

    @Override
    public List<Message> writtenBy(String user) {
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

    @Override
    public void update(Message message, String user) throws MessageNotFoundException {
        messageDao.update(message.getId(), message.getMessage());
    }

    public boolean delete(String message, String forUser) {
        return messageDao.delete(message, forUser);
    }
}
