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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.util.List;

@RestController
@SuppressWarnings("UnusedReturnValue")
public class UserController {
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    UserService users;

    @GetMapping(value = "/users", produces = JSON)
    public List<User> listUsers() {
        return users.all();
    }

    @GetMapping(value = "users/<id>", produces = JSON)
    public User getUser(@PathParam("id") String user) {
        return users.describe(user);
    }

    @GetMapping(value = "users/<id>/messages", produces = JSON)
    public List<Message> getMessages(@PathParam("id") String user) {
        return users.messagesWrittenBy(user);
    }
}
