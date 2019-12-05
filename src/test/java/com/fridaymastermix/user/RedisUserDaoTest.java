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
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RedisUserDaoTest {

    private RedisUserDao redisUserDao;
    private Jedis jedis;
    private RedisFactory factory;
    private static final List<String> users = List.of("lalle", "kalle", "falle");

    @Before
    public void setup() {
        redisUserDao = new RedisUserDao();
        jedis = mock(Jedis.class);
        factory = mock(RedisFactory.class);
        redisUserDao.redisFactory = factory;

        when(factory.redis()).thenReturn(jedis);
    }

    @After
    public void teardown() {
        redisUserDao = null;
        jedis = null;
    }

    @Test
    public void create() throws UserAlreadyExistsException {
        when(jedis.exists("user:lalle")).thenReturn(false);

        redisUserDao.add(new User("lalle", "password"));

        verify(jedis).hmset("user:lalle", Map.of("nick", "lalle", "password", "password"));
        verify(jedis).lpush("users:all", "lalle");
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void createWithExistingUser() throws UserAlreadyExistsException {
        when(jedis.exists("user:lalle")).thenReturn(true);

        redisUserDao.add(new User("lalle", "password"));
        fail();
    }

    @Test
    public void all() {
        when(jedis.lrange("users:all", 0, -1)).thenReturn(users);

        when(jedis.hgetAll("user:lalle")).thenReturn(Map.of("nick", "lalle", "password", "password"));
        when(jedis.hgetAll("user:kalle")).thenReturn(Map.of("nick", "kalle", "password", "password"));
        when(jedis.hgetAll("user:falle")).thenReturn(Map.of("nick", "falle", "password", "password"));

        var expected = users.stream().map(name -> new User(name, "password")).toArray(User[]::new);

        var result = redisUserDao.all();

        assertThat(result, CoreMatchers.hasItems(expected));
    }

    @Test
    public void allNoUsers() {
        when(jedis.lrange("users:all", 0, -1)).thenReturn(List.of());
        var result = redisUserDao.all();
        assertEquals(0, result.size());
        verify(jedis, never()).hgetAll("lalle");
    }

    @Test
    public void get() {
        when(jedis.hgetAll("user:lalle")).thenReturn(Map.of("nick", "lalle", "password", "pass1"));

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