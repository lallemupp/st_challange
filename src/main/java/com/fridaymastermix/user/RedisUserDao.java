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

import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.stream.Collectors;

public class RedisUserDao implements UserDao {

    private static String USERS_KEY = "users";

    private static int LIST_START = 0;
    private static int LIST_END = -1;

    private static String USER_NICK_KEY = "nick";
    private static String USER_PASSWORD_KEY = "password";

    Jedis redis;

    @Override
    public List<User> all() {
        var userKeys = redis.lrange(USERS_KEY, LIST_START, LIST_END);
        return userKeys.stream().map(this::userHash).collect(Collectors.toList());
    }

    @Override
    public User get(String user) {
        var hash = redis.hgetAll(user);
        if (!hash.isEmpty()) {
            return new User(hash.get("nick"), hash.get("password"));
        } else {
            return User.NONEXISTING;
        }
    }

    private User userHash(String hashKey) {
        var hash = redis.hgetAll(hashKey);
        return new User(hash.get(USER_NICK_KEY), hash.get(USER_PASSWORD_KEY));
    }
}
