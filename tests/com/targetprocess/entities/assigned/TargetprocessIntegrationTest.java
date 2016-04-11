package com.targetprocess.entities.assigned;

import com.intellij.tasks.integration.live.LiveIntegrationTestCase;

public class TargetprocessIntegrationTest extends LiveIntegrationTestCase<TargetprocessRepository> {
    @Override
    protected TargetprocessRepository createRepository() throws Exception {
        try {
            TargetprocessRepository repository = new TargetprocessRepository(new TargetprocessRepositoryType());
            repository.setUsername("admin");
            repository.setPassword("admin");
            return repository;
        } catch (Exception e) {
            tearDown();
            throw e;
        }
    }

    public void testTestConnection() throws Exception {
        assertNull(myRepository.createCancellableConnection().call());

        myRepository.setPassword("illegal password");
        final Exception error = myRepository.createCancellableConnection().call();
        assertNotNull(error);
        assertTrue(error.getMessage().contains("Unauthorized"));
    }

}
