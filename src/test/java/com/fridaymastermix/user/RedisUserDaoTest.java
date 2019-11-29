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

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RedisUserDaoTest {

    private RedisUserDao redisUserDao;
    private Jedis jedis;
    private static final List<String> users = List.of("lalle", "kalle", "falle");

    @Before
    public void setup() {
        redisUserDao = new RedisUserDao();
        jedis = mock(Jedis.class);
        redisUserDao.redis = jedis;
    }

    @After
    public void teardown() {
        redisUserDao = null;
        jedis = null;
    }

    @Test
    public void all() {
        when(jedis.lrange("users", 0, -1)).thenReturn(users);

        when(jedis.hgetAll("lalle")).thenReturn(Map.of("nick", "lalle", "password", "password"));
        when(jedis.hgetAll("kalle")).thenReturn(Map.of("nick", "kalle", "password", "password"));
        when(jedis.hgetAll("falle")).thenReturn(Map.of("nick", "falle", "password", "password"));

        var expected = users.stream().map(name -> new User(name, "password")).toArray(User[]::new);

        var result = redisUserDao.all();

        assertThat(result, CoreMatchers.hasItems(expected));
        users.forEach(user -> verify(jedis).hgetAll(user));
    }

    @Test
    public void allNoUsers() {
        when(jedis.lrange("users", 0, -1)).thenReturn(List.of());
        var result = redisUserDao.all();
        assertEquals(0, result.size());
        verify(jedis, never()).hgetAll("lalle");
    }

    @Test
    public void get() {
        when(jedis.hgetAll("lalle")).thenReturn(Map.of("nick", "lalle", "password", "pass1"));

        var expected = new User("lalle", "pass1");
        var result = redisUserDao.get("lalle");

        assertEquals(expected, result);
    }

    @Test
    public void getNoUser() {
        when(jedis.hgetAll("lalle")).thenReturn(Map.of());

        var expected = User.NONEXISTING;
        var result = redisUserDao.get("lalle");

        assertEquals(expected, result);
    }
}