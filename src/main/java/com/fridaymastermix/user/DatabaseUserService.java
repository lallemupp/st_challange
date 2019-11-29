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

package com.fridaymastermix.user;

import com.fridaymastermix.message.Message;
import com.fridaymastermix.message.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatabaseUserService implements UserService {

    @Autowired
    UserDao users;
    @Autowired
    MessageService messageService;

    public List<User> all() {
        return users.all();
    }

    @Override
    public User describe(String user) {
        return users.get(user);
    }

    @Override
    public List<Message> messagesWrittenBy(String user) {
        return messageService.messagesWrittenBy(user);
    }
}
