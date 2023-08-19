package io.github.xmljim.service.di.registry;

import io.github.xmljim.service.di.ServiceManagerException;
import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.scanner.Scanner;
import io.github.xmljim.service.di.scanner.Scanners;
import io.github.xmljim.service.di.util.ClassFilter;
import io.github.xmljim.service.di.util.ServiceLifetime;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The default ServiceRegistry implementation
 */
@SuppressWarnings("unused")
@ServiceProvider(name = "DefaultServiceRegistry", lifetime = ServiceLifetime.SINGLETON, priority = 1)
public class ServiceRegistryImpl extends ServiceRegistries {
    private boolean loaded;

    private final Map<String, Class<? extends Scanner>> scannerMap = new HashMap<>();

    private final Map<String, Boolean> scannerLoadStatus = new HashMap<>();

    public ServiceRegistryImpl() {
        super();
        scannerMap.put(Scanners.MODULE, Scanners.getModuleScannerClass());
        scannerMap.put(Scanners.CLASSPATH, Scanners.getClasspathScannerClass());
    }

    public Set<String> getScanners() {
        return scannerMap.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isLoaded() {
        return loaded;
    }

    public synchronized boolean isLoaded(String scanner) {
        return scannerLoadStatus.getOrDefault(scanner, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void load(ClassFilter serviceFilter, ClassFilter providerFilter) {
        scannerMap.entrySet().forEach(scanner -> scannerLoadStatus.put(scanner.getKey(),
            Scanners.newScanner(scanner.getValue(), serviceFilter, providerFilter, enforceProviderAssignableFromService())
                .scan(this)));

        loaded = scannerLoadStatus.values().stream().allMatch(b -> b);
    }

    @Override
    public synchronized boolean load(String scannerName, ClassFilter serviceFilter, ClassFilter providerFilter,
        boolean enforceProviderAssignableFromService) {


        var scanner = Optional.ofNullable(scannerMap.get(scannerName));
        var loaded = scanner
            .map(s -> Scanners.newScanner(s, serviceFilter, providerFilter, enforceProviderAssignableFromService)
                .scan(this))
            .orElseThrow(() -> new ServiceManagerException("No Scanner found with name %s", scannerName));

        scanner.ifPresent(s -> scannerLoadStatus.put(s.getName(), loaded));
        return loaded;
    }

    @Override
    public synchronized boolean load(Scanner scanner) {
        return scanner.scan(this);
    }

    @Override
    public synchronized void appendScanner(Scanner scanner) {
        scannerMap.put(scanner.getName(), scanner.getClass());
    }

    @Override
    public <S extends Scanner> void appendScanner(String name, Class<S> scannerClass) {
        scannerMap.put(name, scannerClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void reload(ClassFilter serviceFilter, ClassFilter providerFilter) {
        loaded = false;
        clearServices();
        load(serviceFilter, providerFilter);
    }


}
