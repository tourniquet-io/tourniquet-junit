/*
 * Copyright 2015-2016 DevCon5 GmbH, info@devcon5.ch
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

/**
 *
 */
package io.tourniquet.junit.jcr.rules;

import static org.junit.Assert.assertNotNull;

import javax.jcr.Repository;
import javax.naming.Context;

import io.tourniquet.junit.rules.RuleSetup;
import io.tourniquet.junit.rules.RuleSetup.RequirementLevel;
import io.tourniquet.junit.rules.BaseRule;

/**
 * A {@link ContentRepository} implementation for providing a repository using a JNDI lookup. The repsitory is searched
 * used the configured Context.
 *
 */
public class JNDIContentRepository extends ContentRepository {


    public JNDIContentRepository() {
        super(null);
    }

    /**
     * The lookup name used to resolve the the repository in the jndi context. The default lookup is
     * <code>java:/rules/local</code>
     */
    private String lookupName = "java:/rules/local";

    /**
     * A Naming context that is used if specified.
     */
    private Context context;

    @RuleSetup(RequirementLevel.REQUIRED)
    public void setContext(final Context context) {
        assertStateBefore(BaseRule.State.CREATED);
        this.context = context;

    }

    /**
     * Sets the lookup name for the repository. The lookup name is looked up in the {@link Context} to resolve the
     * {@link Repository}. Default is &quot;java:/rules/local&quot;
     *
     * @param lookupName
     *            the lookup name to use for jndi lookup
     */
    @RuleSetup(RequirementLevel.OPTIONAL)
    public void setLookupName(final String lookupName) {
        assertStateBefore(BaseRule.State.CREATED);
        this.lookupName = lookupName;
    }

    @Override
    protected Repository createRepository() throws Exception {
        assertNotNull("No Context set", context);
        assertNotNull("No lookup name set", lookupName);
        return (Repository) context.lookup(lookupName);
    }

    @Override
    protected void destroyRepository() { // NOSONAR

    }

}
