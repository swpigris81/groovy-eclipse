/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.dsl.pointcuts.impl;

import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;

/**
 * Tests that the file currently being checked is in the given source folder
 * The result is cached in the pattern providing a fail/succeed fast strategy.
 * 
 * Argument should be the workspace relative path to the source folder, using '/'
 * as a path separator.
 * @author andrew
 * @created Feb 10, 2011
 */
public class SourceFolderPointcut extends AbstractPointcut {

    public SourceFolderPointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    public BindingSet matches(GroovyDSLDContext pattern) {
        if (pattern.fileName != null && pattern.fileName.startsWith((String) getFirstArgument())) {
            return new BindingSet().addDefaultBinding(pattern.fileName);
        } else {
            return null;
        }
    }
    
    @Override
    public boolean fastMatch(GroovyDSLDContext pattern) {
        return matches(pattern) != null;
    }

    @Override
    public String verify() {
        String maybeStatus = allArgsAreStrings();
        if (maybeStatus != null) {
            return maybeStatus;
        }
        maybeStatus = hasOneArg();
        if (maybeStatus != null) {
            return maybeStatus;
        }
        return super.verify();
    }
}