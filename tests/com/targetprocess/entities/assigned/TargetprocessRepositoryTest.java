package com.targetprocess.entities.assigned;

import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TargetprocessRepositoryTest extends TestCase {

    public void testGetRequestUrlWithEmptyQuery() {
        assertEquals("http://localhost/targetprocess/api/v2/assignable" +
                "?select=" + encodeUrl("{id,name,description,entityType:entityType.name}") +
                "&where=" + encodeUrl("(assignedUser.where(it.login=='user').Count>0)and(entityType.name=='Bug' or entityType.name=='UserStory' or entityType.name=='Task')and(entityState.isFinal==false)") +
                "&orderBy=" + encodeUrl("id desc"),
            getRequestUrl(null, 0, 0, false));

        assertEquals("http://localhost/targetprocess/api/v2/assignable" +
                "?select=" + encodeUrl("{id,name,description,entityType:entityType.name}") +
                "&where=" + encodeUrl("(assignedUser.where(it.login=='user').Count>0)and(entityType.name=='Bug' or entityType.name=='UserStory' or entityType.name=='Task')and(entityState.isFinal==false)") +
                "&orderBy=" + encodeUrl("id desc"),
            getRequestUrl("", 0, 0, false));
    }

    public void testGetRequestUrlWithOffsetAndLimit() {
        assertEquals("http://localhost/targetprocess/api/v2/assignable" +
                "?select=" + encodeUrl("{id,name,description,entityType:entityType.name}") +
                "&where=" + encodeUrl("(assignedUser.where(it.login=='user').Count>0)and(entityType.name=='Bug' or entityType.name=='UserStory' or entityType.name=='Task')and(entityState.isFinal==false)") +
                "&orderBy=" + encodeUrl("id desc"),
            getRequestUrl("", 0, 0, false));

        assertEquals("http://localhost/targetprocess/api/v2/assignable" +
                "?select=" + encodeUrl("{id,name,description,entityType:entityType.name}") +
                "&where=" + encodeUrl("(assignedUser.where(it.login=='user').Count>0)and(entityType.name=='Bug' or entityType.name=='UserStory' or entityType.name=='Task')and(entityState.isFinal==false)and(name.contains('code') or id.ToString().contains('code'))") +
                "&orderBy=" + encodeUrl("id desc") +
                "&take=20&skip=0",
            getRequestUrl("code", 0, 20, false));

        assertEquals("http://localhost/targetprocess/api/v2/assignable" +
                "?select=" + encodeUrl("{id,name,description,entityType:entityType.name}") +
                "&where=" + encodeUrl("(assignedUser.where(it.login=='user').Count>0)and(entityType.name=='Bug' or entityType.name=='UserStory' or entityType.name=='Task')and(entityState.isFinal==false)and(name.contains('code') or id.ToString().contains('code'))") +
                "&orderBy=" + encodeUrl("id desc") +
                "&take=100&skip=20",
            getRequestUrl("code", 20, 100, false));
    }

    public void testGetRequestUrlWithClosed() {
        assertEquals("http://localhost/targetprocess/api/v2/assignable" +
                "?select=" + encodeUrl("{id,name,description,entityType:entityType.name}") +
                "&where=" + encodeUrl("(assignedUser.where(it.login=='user').Count>0)and(entityType.name=='Bug' or entityType.name=='UserStory' or entityType.name=='Task')and(name.contains('code') or id.ToString().contains('code'))") +
                "&orderBy=" + encodeUrl("id desc") +
                "&take=20&skip=0",
            getRequestUrl("code", 0, 20, true));
    }

    public void testGetRequestUrlWithId() {
        String url = TargetprocessRepository
            .getRequestUrl("http://localhost/targetprocess", "user", "12345")
            .toString();
        assertEquals("http://localhost/targetprocess/api/v2/assignable" +
                "?select=" + encodeUrl("{id,name,description,entityType:entityType.name}") +
                "&where=" + encodeUrl("(assignedUser.where(it.login=='user').Count>0)and(entityType.name=='Bug' or entityType.name=='UserStory' or entityType.name=='Task')and(id==12345)"),
            url);
    }

    private static String getRequestUrl(@Nullable String query, int offset, int limit, boolean withClosed) {
        return TargetprocessRepository
            .getRequestUrl("http://localhost/targetprocess", "user", query, offset, limit, withClosed)
            .toString();
    }

    private static String encodeUrl(@NotNull String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
