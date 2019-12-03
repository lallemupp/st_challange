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

import com.fridaymastermix.message.Message;
import com.fridaymastermix.message.MessageNotFoundException;
import com.fridaymastermix.message.MessageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

import java.net.URI;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class UserControllerTest {

    private UserController userController;
    private UserService userService;
    private MessageService messageService;
    private HttpServletRequest request;

    @Before
    public void setup() {
        userController = new UserController();
        userService = mock(UserService.class);
        messageService = mock(MessageService.class);
        request = mock(HttpServletRequest.class);
        userController.users = userService;
        userController.messages = messageService;
    }

    @After
    public void teardown() {
        userController = null;
        userService = null;
    }

    @Test
    public void listUsers() {
        userController.listUsers();
        verify(userService).all();
    }

    @Test
    public void getUser() {
        userController.getUser("lalle");
        verify(userService).describe("lalle");
    }

    @Test
    public void getMessages() {
        userController.getMessages("lalle");
        verify(messageService).writtenBy("lalle");
    }

    @Test
    public void postMessage() throws NonUniqueUserException {
        when(request.getRequestURI()).thenReturn("http://example.org/users/");

        var user = new User("lalle", "password");
        userController.postUser(user, request);

        verify(userService).create(user);
    }

    @Test
    public void postMessageNonUniqueUser() throws NonUniqueUserException {
        var user = new User("lalle", "password");

        when(request.getRequestURI()).thenReturn("http://example.org/users/");
        doThrow(new NonUniqueUserException("DANGER !!!!!! TERROR HORROR")).when(userService).create(user);

        var result = userController.postUser(user, request);
        var expectedStatus = HttpStatus.SEE_OTHER;
        var expectedLocation = "http://example.org/users/lalle";
        assertEquals(expectedStatus, result.getStatusCode());
        assertEquals(expectedLocation, result.getHeaders().getLocation().toString());
    }

    @Test
    public void deleteMessage() {
        when(messageService.delete("message_id", "lalle")).thenReturn(true);

        var response = userController.deleteMessage("lalle", "message_id");
        var expected = HttpStatus.OK;
        assertEquals(expected, response.getStatusCode());
    }

    @Test
    public void deleteMessageMissingMessage() {
        when(messageService.delete("message_id", "lalle")).thenReturn(false);

        var response = userController.deleteMessage("lalle", "message_id");
        var expected = HttpStatus.NOT_FOUND;
        assertEquals(expected, response.getStatusCode());
    }

    @Test
    public void updateMessage() throws MessageNotFoundException {
        var message = new Message("message_id", "this is a message");
        when(request.getRequestURI()).thenReturn("http://example.org/users/lalle/messages/message_id");

        var result = userController.putMessage("lalle", "message_id", message, request);
        var expected = HttpStatus.OK;
        assertEquals(expected, result.getStatusCode());
        verify(messageService).update(message, "lalle");
    }

    @Test
    public void updateMessageNonExistingMessage() throws MessageNotFoundException {
        var message = new Message("message_id", "this is a message");
        when(request.getRequestURI()).thenReturn("http://example.org/users/lalle/messages/message_id");
        doThrow(new MessageNotFoundException("DANGER !!!!!! TERROR HORROR")).when(messageService).update(message, "lalle");

        var result = userController.putMessage("lalle", "message_id", message, request);
        var expected = HttpStatus.NOT_FOUND;
        assertEquals(expected, result.getStatusCode());

    }
}