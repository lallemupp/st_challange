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
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RedisMessageDao implements MessageDao {
    private static final String PREFIX_MESSAGES = "messages";
    private static final String PREFIX_MESSAGE = "message";
    private static final String PREFIX_USER = "user";
    private static final String[] MESSAGE_KEYS = {"id", "message", "created", "updated"};


    @Autowired
    Jedis redis;

    @Override
    public Message get(String id) {
        var hash = redis.hgetAll(String.format("%s:%s", PREFIX_MESSAGE, id));
        return toMessage(hash);
    }

    private boolean valid(Map<String, String> hash) {
        for (var key: MESSAGE_KEYS) {
            if (!hash.containsKey(key)) {
                return false;
            }
        }

        return true;
    }

    private Message toMessage(Map<String, String> hash) {
        if (hash.isEmpty()) {
            return Message.NONEXISTING;
        } else if (!valid(hash)) {
            return Message.ERROR;
        } else {
            return new Message(
                    hash.get("id"),
                    hash.get("message"),
                    Integer.parseInt(hash.get("created")),
                    Integer.parseInt(hash.get("updated")));
        }
    }

    /**
     * Gets all messages written by a user.
     * This should probably be paged.
     *
     * @param user the id of the user to get the messages for.
     * @return the messages.
     */
    @Override
    public List<Message> messagesWrittenBy(String user) {
        String key = String.format("%s:%s:%s", PREFIX_USER, user, PREFIX_MESSAGES);
        var messageKeys = redis.lrange(key, 0, -1);
        return messageKeys.parallelStream().
                map(redis::hgetAll).
                map(this::toMessage).
                filter(this::invalidMessage).
                collect(Collectors.toList());
    }

    private boolean invalidMessage(Message message) {
        return ((message != Message.NONEXISTING) && (message != Message.ERROR));
    }

    @Override
    public List<Message> all() {
        var keys = redis.lrange(String.format("%s:all", PREFIX_MESSAGE), 0, -1);
        return keys.parallelStream().
                map(redis::hgetAll).
                map(this::toMessage).
                filter(this::invalidMessage).
                collect(Collectors.toList());
    }

    @Override
    public String add(String message, String user) {
        String id = UUID.randomUUID().toString();

        var now = epochString();

        Map<String, String> messageHash = Map.of(
                "id", id,
                "message", message,
                "created", now,
                "updated", now);
        redis.hset(String.format("%s:%s:%s", PREFIX_USER, PREFIX_MESSAGE, id), messageHash);
        redis.lpush(String.format("%s:%s:%s", PREFIX_USER, user, PREFIX_MESSAGES), id);
        return id;
    }

    private String epochString() {
        var now = System.currentTimeMillis();
        long epoch = TimeUnit.MILLISECONDS.toSeconds(now);
        return Long.toString(epoch);
    }

    @Override
    public String update(String id, String message) throws MessageNotFoundException {
        String key = String.format("%s:%s", PREFIX_MESSAGE, id);
        var messageHash = redis.hgetAll(key);

        if (!valid(messageHash)) {
            throw new MessageNotFoundException(String.format("Message with id %s could not be found and can not be updated.", id));
        }

        redis.hmset(key, Map.of("message", message));
        return id;
    }
}
