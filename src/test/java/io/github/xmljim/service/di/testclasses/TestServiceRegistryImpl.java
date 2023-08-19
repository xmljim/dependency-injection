package io.github.xmljim.service.di.testclasses;

import io.github.xmljim.service.di.registry.ServiceRegistries;
import io.github.xmljim.service.di.scanner.Scanner;
import io.github.xmljim.service.di.util.ClassFilter;

import java.util.Set;

/**
 * Test registry for testing bootstrap options., not intended for use
 */
public class TestServiceRegistryImpl extends ServiceRegistries {
    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public void load(ClassFilter serviceFilter, ClassFilter providerFilter) {

    }

    @Override
    public boolean load(String scannerName, ClassFilter serviceFilter, ClassFilter providerFilter, boolean enforceProviderAssignableFromService) {
        return false;
    }

    @Override
    public boolean load(Scanner scanner) {
        return false;
    }

    @Override
    public boolean isLoaded(String scanner) {
        return false;
    }

    @Override
    public void appendScanner(Scanner scanner) {

    }

    @Override
    public <S extends Scanner> void appendScanner(String name, Class<S> scannerClass) {

    }

    @Override
    public Set<String> getScanners() {
        return null;
    }

    @Override
    public void reload(ClassFilter serviceFilter, ClassFilter providerFilter) {

    }
}
