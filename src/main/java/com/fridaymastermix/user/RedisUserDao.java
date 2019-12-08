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

import com.fridaymastermix.database.RedisFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An implementation of the {@link UserDao} that users redis to store the data.
 */
@Component
@Qualifier("redis")
public class RedisUserDao implements UserDao {

    private static String USERS_PREFIX = "users";
    private static String USER_PREFIX = "user";

    private static String USER_NICK_KEY = "nick";
    private static String USER_PASSWORD_KEY = "password";

    @Autowired
    RedisFactory redisFactory;

    /**
     * @inheritDoc
     */
    @Override
    public void add(User user) throws UserAlreadyExistsException {
        try (var redis = redisFactory.redis()) {
            var userKey = String.format("%s:%s", USER_PREFIX, user.getUser());

            if (redis.exists(userKey)) {
                var errorMessage = String.format("A user with nick %s already exist", user.getUser());
                throw new UserAlreadyExistsException(errorMessage);
            }

            redis.hmset(userKey, Map.of(USER_NICK_KEY, user.getUser(), USER_PASSWORD_KEY, user.getPassword()));

            var usersKey = String.format("%s:%s", USERS_PREFIX, "all");
            redis.lpush(usersKey, user.getUser());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<User> all() {
        try (var redis = redisFactory.redis()) {
            var userKeys = redis.lrange(String.format("%s:%s", USERS_PREFIX, "all"), 0, -1);
            return userKeys.stream().map(this::get).filter(user -> user != User.NONEXISTING).collect(Collectors.toList());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public User get(String user) {
        try (var redis = redisFactory.redis()) {
            var hash = redis.hgetAll(String.format("%s:%s", USER_PREFIX, user));
            if (!hash.isEmpty()) {
                return new User(hash.get("nick"), hash.get("password"));
            } else {
                return User.NONEXISTING;
            }
        }
    }
}
