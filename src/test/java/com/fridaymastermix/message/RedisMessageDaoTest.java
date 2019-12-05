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

import com.fridaymastermix.database.RedisFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RedisMessageDaoTest {

    private static final Map<String, String> MESSAGE_HASH = Map.of(
            "id", "message_id",
            "message", "this is a message",
            "createdBy", "lalle",
            "created", "1",
            "updated", "1");

    private RedisMessageDao uut;
    private Jedis redis;
    private RedisFactory factory;

    @Before
    public void setup() {
        uut = new RedisMessageDao();
        redis = mock(Jedis.class);
        factory = mock(RedisFactory.class);

        uut.redisFactory = factory;
        when(factory.redis()).thenReturn(redis);
    }

    @After
    public void teardown() {
        uut = null;
        redis = null;
        factory = null;
    }

    @Test
    public void get() {
        when(redis.hgetAll("message:message_id")).thenReturn(MESSAGE_HASH);
        var result = uut.get("message_id");
        var expected = new Message("message_id", "this is a message", "lalle", 1, 1);

        assertEquals(expected, result);
        verify(redis).hgetAll("message:message_id");
    }

    @Test
    public void getMessageDoesNotExist() {
        when(redis.hgetAll("message:message_id")).thenReturn(Map.of());

        var result = uut.get("message_id");
        var expected = Message.NONEXISTING;

        assertEquals(expected, result);
    }

    @Test
    public void getMissingIdKey() {
        when(redis.hgetAll("message:message_id")).thenReturn(Map.of(
                "message", "a message",
                "created", "1",
                "id", "message_id"));

        var result = uut.get("message_id");
        var expected = Message.ERROR;

        assertEquals(expected, result);
    }


    @Test
    public void getMissingMessageKey() {
        when(redis.hgetAll("message:message_id")).thenReturn(Map.of(
                "id", "message_id",
                "created", "1",
                "updated", "1"));

        var result = uut.get("message_id");
        var expected = Message.ERROR;

        assertEquals(expected, result);
    }

    @Test
    public void getMissingCreatedKey() {
        when(redis.hgetAll("message:message_id")).thenReturn(Map.of(
                "id", "message_id",
                "message", "a message",
                "updated", "1"));

        var result = uut.get("message_id");
        var expected = Message.ERROR;

        assertEquals(expected, result);
    }

    @Test
    public void getMissingUpdatedKey() {
        when(redis.hgetAll("message:message_id")).thenReturn(Map.of(
                "id", "message_id",
                "message", "a message",
                "created", "1"));

        var result = uut.get("message_id");
        var expected = Message.ERROR;

        assertEquals(expected, result);
    }

    @Test
    public void messagesWrittenBy() {
        when(redis.smembers("user:lalle:messages")).thenReturn(Set.of(
                "message_id",
                "message_id_2",
                "message_id_3"));

        when(redis.hgetAll("message:message_id")).thenReturn(MESSAGE_HASH);
        when(redis.hgetAll("message:message_id_2")).thenReturn(MESSAGE_HASH);
        when(redis.hgetAll("message:message_id_3")).thenReturn(MESSAGE_HASH);

        var message = new Message("message_id", "this is a message", "lalle", 1, 1);

        Message[] expected = {message, message, message};

        var result = uut.messagesWrittenBy("lalle");
        assertThat(result, hasItems(expected));
        verify(redis, times(3)).hgetAll(any(String.class));
    }

    @Test
    public void messagesWithMissingHash() {
        when(redis.lrange("user:lalle:mesasges", 0, -1)).thenReturn(List.of());
        var expected = 0;
        var result = uut.messagesWrittenBy("lalle");
        assertEquals(expected, result.size());
    }

    @Test
    public void all() {
        when(redis.smembers("messages:all")).thenReturn(Set.of(
                "message_id",
                "message_id_2",
                "message_id_3"));

        when(redis.hgetAll("message:message_id")).thenReturn(MESSAGE_HASH);
        when(redis.hgetAll("message:message_id_2")).thenReturn(MESSAGE_HASH);
        when(redis.hgetAll("message:message_id_3")).thenReturn(MESSAGE_HASH);

        var message = new Message("message_id", "this is a message", "lalle", 1, 1);
        Message[] expected = {message, message, message};

        var result = uut.all();
        assertThat(result, hasItems(expected));
    }

    @Test
    public void allNoMessages() {
        when(redis.lrange("messages:all", 0, -1)).thenReturn(List.of());

        var expected = 0;
        var result = uut.all();

        assertEquals(expected, result.size());
    }

    @Test
    public void add() {
        var result = uut.add( "this is a message", "lalle");
        assertNotNull(result);

        verify(redis).hset(anyString(), anyMap());
        verify(redis).sadd(eq("user:lalle:messages"), anyString());
        verify(redis).sadd(eq("messages:all"), anyString());
    }

    @Test
    public void update() throws MessageNotFoundException {
        when(redis.hgetAll("message:message_id")).thenReturn(MESSAGE_HASH);

        uut.update("message_id", "this is a new message");
        verify(redis).hmset("message:message_id", Map.of("message", "this is a new message"));
        verify(redis).hmset(eq("message:message_id"), argThat(new UpdatedMapMatcher()));
    }

    @Test(expected = MessageNotFoundException.class)
    public void updateItemNotFound() throws MessageNotFoundException {
        when(redis.hgetAll("user:lalle:message_id")).thenReturn(Map.of());

        uut.update("user:lalle:message_id", "this is a message");
        fail("MessageNotFoundException was not thrown");
    }

    @Test
    public void delete() {
        when(redis.del("message:message_id")).thenReturn(1L);
        when(redis.lrem("user:lalle:messages", 1, "message_id")).thenReturn(1L);
        when(redis.lrem("users:all", 1, "message_id")).thenReturn(1L);

        var result = uut.delete("message_id", "lalle");

        assertTrue(result);
    }

    @Test
    public void deleteWithMissingEntry() {
        when(redis.del("message:message_id")).thenReturn(1L);
        when(redis.lrem("user:lalle:messages", 1, "message_id")).thenReturn(0L);
        when(redis.lrem("users:all", 1, "message_id")).thenReturn(0L);

        var result = uut.delete("message_id", "lalle");

        assertTrue(result);
    }

    @Test
    public void deleteWithMissingItem() {
        when(redis.del("message:message_id")).thenReturn(0L);
        when(redis.lrem("user:lalle:messages", 1, "message_id")).thenReturn(0L);
        when(redis.lrem("users:all", 1, "message_id")).thenReturn(0L);

        var result = uut.delete("message_id", "lalle");

        assertFalse(result);
    }

    private static class UpdatedMapMatcher implements ArgumentMatcher<Map<String, String>> {

        @Override
        public boolean matches(Map<String, String> argument) {
            return argument.containsKey("updated");
        }
    }
}