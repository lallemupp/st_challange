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

import com.fridaymastermix.user.UserDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MessageServiceTest {

    private MessageService uut;
    private MessageDao messageDao;
    private UserDao userDao;

    @Before
    public void setup() {
        uut = new MessageService();
        messageDao = mock(MessageDao.class);
        userDao = mock(UserDao.class);

        uut.messageDao = messageDao;
        uut.userDao = userDao;
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
        uut.writtenBy("lalle");
        verify(messageDao).messagesWrittenBy("lalle");
    }

    @Test
    public void messagesWrittenByNoMatch() {
        when(messageDao.messagesWrittenBy("lalle")).thenReturn(List.of());

        var result = uut.writtenBy("lalle");
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
    public void create() throws UserNotFoundException {
        uut.create("this is a message", "lalle");
        verify(messageDao).add("this is a message", "lalle");
    }

    @Test
    public void update() throws MessageNotFoundException {
        var message = new Message("an id", "this is a test", "lalle", 0, 1);
        uut.update(message);
        verify(messageDao).update(message.getId(), message.getMessage());
    }

    @Test(expected = MessageNotFoundException.class)
    public void updateNonExistingMessage() throws MessageNotFoundException {
        var message = new Message("an id", "this is a test", "lalle", 0, 1);
        doThrow(new MessageNotFoundException("DANGER !!!!!! TERROR HORROR")).when(messageDao).update(message.getId(), message.getMessage());

        uut.update(message);
        fail("MessageNotFoundException was not thrown");
    }
}