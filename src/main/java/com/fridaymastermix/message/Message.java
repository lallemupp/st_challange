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

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.NotBlank;

/**
 * Domain class for messages.
 */
public class Message {
    public static final Message NONEXISTING = new Message(
            "NOT_AN_EXISTING_ID",
            "NONEXISTING",
            "NONEXISTING",
            0,
            0);
    public static final Message ERROR = new Message(
            "ERROR",
            "ERROR",
            "ERROR",
            0,
            0);

    @NotBlank
    private String message;
    private String id;
    private String createdBy;
    private long created;
    private long updated;

    public Message(String id, String message) {
        this(id, message, null, System.currentTimeMillis() / 1000, System.currentTimeMillis() / 1000);
    }

    @JsonCreator
    public Message(String id, String message, String createdBy, long created, long updated) {
        this.id = id;
        this.message = message;
        this.createdBy = createdBy;
        this.created = created;
        this.updated = updated;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public long getCreated() {
        return created;
    }

    public long getUpdated() {
        return updated;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", created=" + created +
                ", updated=" + updated +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Message m = (Message) o;

        return new EqualsBuilder()
                .append(created, m.created)
                .append(updated, m.updated)
                .append(id, m.id)
                .append(createdBy, m.createdBy)
                .append(message, m.message)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(message)
                .append(createdBy)
                .append(created)
                .append(updated)
                .toHashCode();
    }
}
