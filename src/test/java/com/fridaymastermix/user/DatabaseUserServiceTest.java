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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DatabaseUserServiceTest {

    private DatabaseUserService uut;
    private MessageService messageService;
    private UserDao userDao;

    @Before
    public void setup() {
        uut = new DatabaseUserService();

        userDao = mock(UserDao.class);
        messageService = mock(MessageService.class);

        uut.users = userDao;
        uut.messageService = messageService;
    }

    @After
    public void teardown() {
        uut = null;
        userDao = null;
        messageService = null;
    }

    @Test
    public void all() {
        uut.all();
        verify(userDao).all();
    }

    @Test
    public void describe() {
        uut.describe("lalle");
        verify(userDao).get("lalle");
    }

    @Test
    public void messagesWrittenBy() {
        uut.messagesWrittenBy("lalle");
        verify(messageService).messagesWrittenBy("lalle");
    }
}