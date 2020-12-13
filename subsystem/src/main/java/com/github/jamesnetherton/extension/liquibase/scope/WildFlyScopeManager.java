package com.github.jamesnetherton.extension.liquibase.scope;

import com.github.jamesnetherton.extension.liquibase.LiquibaseLogger;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/*-
 * #%L
 * wildfly-liquibase-subsystem
 * %%
 * Copyright (C) 2017 - 2020 James Netherton
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import liquibase.Scope;
import liquibase.ScopeManager;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.jboss.modules.ModuleClassLoader;

/**
 * Custom scope manager to get and set a Liquibase {@link Scope}, bound to the thread executing the change log update.
 *
 * This is required since Liquibase does not handle concurrent changelog executions within the same JVM. Therefore scopes
 * are vulnerable to being overwritten by competing threads.
 */
public class WildFlyScopeManager extends ScopeManager {

    private static final long ROOT_SCOPE = Long.MIN_VALUE;
    private static final Map<Long, Scope> scopes = new HashMap<>();

    @Override
    public Scope getCurrentScope() {
        synchronized (scopes) {
            Scope scope = scopes.get(computeScopeCacheKey());
            if (scope == null) {
                scope = scopes.get(ROOT_SCOPE);
            }
            return scope;
        }
    }

    @Override
    protected void setCurrentScope(Scope scope) {
        synchronized (scopes) {
            boolean isRoot = scope.getDatabase() == null;
            long scopeId = isRoot ? ROOT_SCOPE : computeScopeCacheKey();
            if (scopes.containsKey(scopeId)) {
                scopes.replace(scopeId, scope);
            } else {
                scopes.put(scopeId, scope);
            }
        }
    }

    @Override
    protected Scope init(Scope scope) throws Exception {
        return scope;
    }

    public static synchronized Map<Long, Scope> getScopes() {
        return scopes;
    }

    public static void removeCurrentScope() {
        synchronized (scopes) {
            scopes.remove(computeScopeCacheKey());
        }
    }

    @SuppressWarnings("unchecked")
    public static void removeCurrentScope(ModuleClassLoader classLoader) {
        synchronized (scopes) {
            // If the only scope is the root scope then there's no need to do any further work
            if (scopes.size() == 1 && scopes.containsKey(ROOT_SCOPE)) {
                return;
            }

            Set<Long> hashes = new HashSet<>();
            for (Long hash : scopes.keySet()) {
                Scope scope = scopes.get(hash);
                try {
                    ResourceAccessor resourceAccessor = scope.getResourceAccessor();
                    if (resourceAccessor instanceof ClassLoaderResourceAccessor) {
                        ClassLoaderResourceAccessor classLoaderResourceAccessor = (ClassLoaderResourceAccessor) resourceAccessor;
                        Field classLoaderField = ClassLoaderResourceAccessor.class.getDeclaredField("classLoader");
                        classLoaderField.setAccessible(true);
                        if (classLoader == classLoaderField.get(classLoaderResourceAccessor)) {
                            hashes.add(hash);
                        }
                    }

                    if (resourceAccessor instanceof CompositeResourceAccessor) {
                        CompositeResourceAccessor compositeResourceAccessor = (CompositeResourceAccessor) resourceAccessor;
                        Field resourceAccessorField = CompositeResourceAccessor.class.getDeclaredField("resourceAccessors");
                        resourceAccessorField.setAccessible(true);
                        List<ResourceAccessor> resourceAccessors = (List<ResourceAccessor>) resourceAccessorField.get(compositeResourceAccessor);

                        for (ResourceAccessor accessor : resourceAccessors) {
                            if (accessor instanceof ClassLoaderResourceAccessor) {
                                ClassLoaderResourceAccessor classLoaderResourceAccessor = (ClassLoaderResourceAccessor) accessor;
                                Field classLoaderField = ClassLoaderResourceAccessor.class.getDeclaredField("classLoader");
                                classLoaderField.setAccessible(true);
                                if (classLoader == classLoaderField.get(classLoaderResourceAccessor)) {
                                    hashes.add(hash);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LiquibaseLogger.ROOT_LOGGER.warn("Failed introspecting Liquibase scope resource accessor for deployment {}", classLoader.getName());
                }
            }
            hashes.forEach(scopes::remove);
        }
    }

    private static long computeScopeCacheKey() {
        Thread thread = Thread.currentThread();
        return thread.hashCode() + thread.getContextClassLoader().hashCode();
    }
}
