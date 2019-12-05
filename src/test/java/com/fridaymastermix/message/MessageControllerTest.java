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
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MessageControllerTest {
    private MessageController messageController;
    private MessageService messageService;
    private HttpServletRequest request;

    @Before
    public void setup() {
        request = mock(HttpServletRequest.class);
        messageService = mock(MessageService.class);

        messageController = new MessageController();
        messageController.messages = messageService;
    }

    @After
    public void teardown() {
        request = null;
        messageService = null;
        messageController = null;
    }

    @Test
    public void postMessage() throws UserNotFoundException {
        when(request.getRequestURI()).thenReturn("http://example.org/messages?user=lalle");

        var message = new Message("message_id", "this is a message");
        messageController.postMessages(message, "lalle", request);

        verify(messageService).create("this is a message", "lalle");
    }

    @Test
    public void deleteMessage() {
        when(messageService.delete("message_id", "lalle")).thenReturn(true);

        var response = messageController.deleteMessage("message_id", "lalle");
        var expected = HttpStatus.OK;
        assertEquals(expected, response.getStatusCode());
    }

    @Test
    public void deleteMessageMissingMessage() {
        when(messageService.delete("message_id", "lalle")).thenReturn(false);

        var response = messageController.deleteMessage("message_id", "lalle");
        var expected = HttpStatus.NOT_FOUND;
        assertEquals(expected, response.getStatusCode());
    }

    @Test
    public void updateMessage() throws MessageNotFoundException {
        var message = new Message("message_id", "this is a message");
        when(request.getRequestURI()).thenReturn("http://example.org/messages/message_id");

        var result = messageController.putMessage( "message_id", message, request);
        var expected = HttpStatus.OK;
        assertEquals(expected, result.getStatusCode());
        verify(messageService).update(message);
    }

    @Test
    public void updateMessageNonExistingMessage() throws MessageNotFoundException {
        var message = new Message("message_id", "this is a message");
        when(request.getRequestURI()).thenReturn("http://example.org/messages/message_id");
        doThrow(new MessageNotFoundException("DANGER !!!!!! TERROR HORROR")).when(messageService).update(message);

        var result = messageController.putMessage("message_id", message, request);
        var expected = HttpStatus.NOT_FOUND;
        assertEquals(expected, result.getStatusCode());

    }
}
