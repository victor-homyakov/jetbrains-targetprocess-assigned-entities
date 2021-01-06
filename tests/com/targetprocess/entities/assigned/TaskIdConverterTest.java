package com.targetprocess.entities.assigned;

import junit.framework.TestCase;

public class TaskIdConverterTest extends TestCase {
    public void testGetPresentableId() {
        assertEquals("Bug#12345", TaskIdConverter.getPresentableId(12345, "Bug"));
        assertEquals("US#123", TaskIdConverter.getPresentableId(123, "UserStory"));
        assertEquals("Task#123", TaskIdConverter.getPresentableId(123, "Task"));
        assertEquals("Entity#2", TaskIdConverter.getPresentableId(2, "Feature"));
    }

    public void testExtractId() {
        assertEquals("12345", TaskIdConverter.extractId("12345"));

        assertEquals("12345", TaskIdConverter.extractId("Bug#12345"));
        assertEquals("123", TaskIdConverter.extractId("US#123"));
        assertEquals("123", TaskIdConverter.extractId("Task#123"));
        assertEquals("2", TaskIdConverter.extractId("Entity#2"));

        assertNull(TaskIdConverter.extractId("bug#13"));
        assertNull(TaskIdConverter.extractId("UserStory#13"));
        assertNull(TaskIdConverter.extractId("TASK#13"));
        assertNull(TaskIdConverter.extractId("ACME#13"));
    }
}
