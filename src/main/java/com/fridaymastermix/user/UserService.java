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

import com.fridaymastermix.message.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * A service that coordinates requests for users.
 */
@Service
@Qualifier("database")
public class UserService {

    @Autowired
    @Qualifier("redis")
    UserDao users;

    /**
     * Tells the Data Access Object to create a user.
     *
     * @param user the user.
     * @throws UserAlreadyExistsException if the user already exists.
     */
    public void create(User user) throws UserAlreadyExistsException {
        users.add(user);
    }

    /**
     * Asks the Data Access Object for all users.
     * @return a list of all users.
     */
    public List<User> all() {
        return users.all();
    }

    /**
     * Asks the Data Access Object for the user.
     * @param user the user.
     * @return the user.
     */
    public User describe(String user) {
        return users.get(user);
    }
}
