package com.refinedmods.refinedstorage.api.autocrafting.status;

import com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TaskStatusTest {
    @Test
    void dummyTest() {
        new TaskStatus(
            new TaskStatus.TaskInfo(
                new TaskId(UUID.randomUUID()),
                ResourceFixtures.A,
                0,
                0
            ),
            0.69F,
            List.of(
                new TaskStatus.Item(
                    TaskStatus.ItemType.NORMAL,
                    ResourceFixtures.A,
                    0,
                    0,
                    0,
                    0,
                    0
                )
            )
        );
    }
}
