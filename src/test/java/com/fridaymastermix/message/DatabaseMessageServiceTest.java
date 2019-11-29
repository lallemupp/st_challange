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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DatabaseMessageServiceTest {

    private DatabaseMessageService uut;
    private MessageDao messageDao;

    @Before
    public void setup() {
        uut = new DatabaseMessageService();
        messageDao = mock(MessageDao.class);

        uut.messageDao = messageDao;
    }

    @After
    public void teardown() {
        uut = null;
        messageDao = null;
    }

    @Test
    public void describe() {
        uut.describe("bestOfIds");
        verify(messageDao).get("bestOfIds");
    }

    @Test
    public void describeNoMatch() {
        uut.describe("bestOfIds");
        when(messageDao.get("bestOfIds")).thenReturn(Message.NONEXISTING);
        verify(messageDao).get("bestOfIds");
    }

    @Test
    public void messagesWrittenBy() {
        uut.messagesWrittenBy("lalle");
        verify(messageDao).messagesWrittenBy("lalle");
    }

    @Test
    public void messagesWrittenByNoMatch() {
        when(messageDao.messagesWrittenBy("lalle")).thenReturn(List.of());

        var result = uut.messagesWrittenBy("lalle");
        var expected = List.of();

        assertEquals(expected, result);
        verify(messageDao).messagesWrittenBy("lalle");
    }

    @Test
    public void all() {
        messageDao.all();
        verify(messageDao).all();
    }

    @Test
    public void create() {
        uut.create("this is a message", "lalle");
        verify(messageDao).add("this is a message", "lalle");
    }

    @Test
    public void update() throws MessageNotFoundException {
        var message = new Message("an id", "this is a test", 0, 1);
        uut.update(message, "lalle");
        verify(messageDao).update(message.getId(), message.getMessage());
    }

    @Test(expected = MessageNotFoundException.class)
    public void updateNonExistingMessage() throws MessageNotFoundException {
        var message = new Message("an id", "this is a test", 0, 1);
        when(messageDao.update(message.getId(), message.getMessage())).
                thenThrow(new MessageNotFoundException("DANGER !!!!!! TERROR HORROR"));

        uut.update(message, "lalle");
        fail("MessageNotFoundException was not thrown");
    }
}