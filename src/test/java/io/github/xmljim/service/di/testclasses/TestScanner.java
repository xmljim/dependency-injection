package io.github.xmljim.service.di.testclasses;

import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.scanner.Scanners;
import io.github.xmljim.service.di.util.ClassFilter;

public class TestScanner extends Scanners {
    public TestScanner(ClassFilter serviceClassFilter, ClassFilter providerClassFilter, boolean enforceProviderAssignableFromService) {
        super(serviceClassFilter, providerClassFilter, enforceProviderAssignableFromService);
    }

    @Override
    public String getName() {
        return "TEST";
    }

    @Override
    public boolean scan(ServiceRegistry serviceRegistry) {
        return true;
    }
}
