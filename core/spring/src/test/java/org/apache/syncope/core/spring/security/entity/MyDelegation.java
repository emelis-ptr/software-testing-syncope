package org.apache.syncope.core.spring.security.entity;

import org.apache.syncope.core.persistence.api.entity.Delegation;
import org.apache.syncope.core.persistence.api.entity.Role;
import org.apache.syncope.core.persistence.api.entity.user.User;

import java.time.OffsetDateTime;
import java.util.Set;

public class MyDelegation implements Delegation {

    private User user;

    public MyDelegation(User user) {
        this.user = user;
    }

    @Override
    public User getDelegating() {
        return this.user;
    }

    @Override
    public void setDelegating(User delegating) {
        this.user = delegating;
    }

    @Override
    public User getDelegated() {
        return user;
    }

    @Override
    public void setDelegated(User delegated) {
        this.user = delegated;
    }

    @Override
    public void setStart(OffsetDateTime start) {

    }

    @Override
    public OffsetDateTime getStart() {
        return null;
    }

    @Override
    public void setEnd(OffsetDateTime end) {

    }

    @Override
    public OffsetDateTime getEnd() {
        return null;
    }

    @Override
    public boolean add(Role role) {
        return false;
    }

    @Override
    public Set<? extends Role> getRoles() {
        return null;
    }

    @Override
    public String getKey() {
        return null;
    }
}
