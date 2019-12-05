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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * An implementation fo the {@link MessageDao} that uses redis to store the data.
 */
@Component
@Qualifier("redis")
public class RedisMessageDao implements MessageDao {
    private static final String PREFIX_MESSAGES = "messages";
    private static final String PREFIX_MESSAGE = "message";
    private static final String PREFIX_USER = "user";
    private static final String[] MESSAGE_KEYS = {"id", "message", "createdBy", "created", "updated"};

    @Autowired
    RedisFactory redisFactory;

    /**
     * @inheritDoc
     */
    @Override
    public Message get(String id) {
        try (var redis = redisFactory.redis()) {
            String key = String.format("%s:%s", PREFIX_MESSAGE, id);
            var hash = redis.hgetAll(key);
            return toMessage(hash);
        }
    }

    /**
     * @inheritDoc
     */
    private boolean notValid(Map<String, String> hash) {
        for (var key: MESSAGE_KEYS) {
            if (!hash.containsKey(key)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @inheritDoc
     */
    private Message toMessage(Map<String, String> hash) {
        if (hash.isEmpty()) {
            return Message.NONEXISTING;
        } else if (notValid(hash)) {
            return Message.ERROR;
        } else {
            return new Message(
                    hash.get("id"),
                    hash.get("message"),
                    hash.get("createdBy"),
                    Integer.parseInt(hash.get("created")),
                    Integer.parseInt(hash.get("updated")));
        }
    }

    public boolean exists(String message, String user) {
        try (var redis = redisFactory.redis()) {
            var key = String.format("%s:%s:%s", PREFIX_USER, user, PREFIX_MESSAGES);
            return redis.sismember(key, message);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<Message> messagesWrittenBy(String user) {
        try (var redis = redisFactory.redis()) {
            String key = String.format("%s:%s:%s", PREFIX_USER, user, PREFIX_MESSAGES);
            var messageKeys = redis.smembers(key);
            return messageKeys.parallelStream().
                    map(this::get).
                    filter(this::invalidMessage).
                    collect(Collectors.toList());
        }
    }

    /**
     * @inheritDoc
     */
    private boolean invalidMessage(Message message) {
        return ((message != Message.NONEXISTING) && (message != Message.ERROR));
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<Message> all() {
        try (var redis = redisFactory.redis()) {
            var keys = redis.smembers(String.format("%s:all", PREFIX_MESSAGES));
            return keys.parallelStream().
                    map(this::get).
                    filter(this::invalidMessage).
                    collect(Collectors.toList());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public String add(String message, String user) {
        try (var redis = redisFactory.redis()) {
            String id = UUID.randomUUID().toString();

            var now = epochString();

            Map<String, String> messageHash = Map.of(
                    "id", id,
                    "message", message,
                    "createdBy", user,
                    "created", now,
                    "updated", now);
            redis.hset(String.format("%s:%s", PREFIX_MESSAGE, id), messageHash);
            redis.sadd(String.format("%s:%s:%s", PREFIX_USER, user, PREFIX_MESSAGES), id);
            redis.sadd(String.format("%s:%s", PREFIX_MESSAGES, "all"), id);
            return id;
        }
    }

    private String epochString() {
        var now = System.currentTimeMillis();
        long epoch = TimeUnit.MILLISECONDS.toSeconds(now);
        return Long.toString(epoch);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void update(String id, String message) throws MessageNotFoundException {
        try (var redis = redisFactory.redis()) {
            String key = String.format("%s:%s", PREFIX_MESSAGE, id);
            var messageHash = redis.hgetAll(key);

            if (notValid(messageHash)) {
                throw new MessageNotFoundException(String.format("Message with id %s could not be found and can not be updated.", id));
            }

            redis.hmset(key, Map.of("message", message));
            redis.hmset(key, Map.of("updated", epochString()));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean delete(String message, String forUser) {
        try (var redis = redisFactory.redis()) {
            String messageKey = String.format("%s:%s", PREFIX_MESSAGE, message);
            var numberOfDeletedKeys = redis.del(messageKey);

            var userMessagesKey = String.format("%s:%s:%s", PREFIX_USER, forUser, PREFIX_MESSAGES);
            var numberOfDeletedUserEntries = redis.srem(userMessagesKey, message);

            var allMessagesKey = String.format("%s:%s", PREFIX_MESSAGE, "all");
            var numberOfDeletedEntries = redis.srem(allMessagesKey, message);

            return (numberOfDeletedKeys + numberOfDeletedUserEntries + numberOfDeletedEntries) > 0;
        }
    }
}
