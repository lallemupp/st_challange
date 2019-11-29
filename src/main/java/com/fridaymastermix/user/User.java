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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class User {
    private String nick;

    static User NONEXISTING = new User("NO", "USER");

    @JsonIgnore
    private String passwordHash;

    public User(String nick, String passwordHash) {
        this.nick = nick;
        this.passwordHash = passwordHash;
    }

    public String toString() {
        return String.format("%s:%s", nick, "*".repeat(passwordHash.length()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return new EqualsBuilder()
                .append(nick, user.nick)
                .append(passwordHash, user.passwordHash)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(nick)
                .append(passwordHash)
                .toHashCode();
    }

    public String getNick() {
        return nick;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
