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

package com.fridaymastermix.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

/**
 * Factory for Jedis clients.
 *
 * Needed to enable dependency injection of Jedis.
 */
@Component
public class RedisFactory {

    private String host;
    private int port;

    /**
     * Creates and returns a jedis client with default configuration (localhost:6379)
     *
     * @return A Jedis client.
     */
    public Jedis redis() {
        return new Jedis();
    }
}
