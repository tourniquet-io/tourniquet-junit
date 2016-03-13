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
package io.tourniquet.junit.inject;

import javax.annotation.Resource;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * A handle for performing an injection operation of a {@link Resource} annotated field. Matching criteria can be
 * defined using the according by-methods, where multiple criterias are processes using OR (first match).
 *
 * @author Gerald Muecke, gerald@moskito.li
 */
public class ResourceInjection extends Injection {

    private final List<Resource> matchingResources = new ArrayList<>();

    /**
     * @param value
     *            the injection value
     */
    public ResourceInjection(final Object value) {

        super(value);
    }

    /**
     * Determines the injection target {@link Resource} by its name
     *
     * @param name
     *            the name for the resoure
     * @return this {@link ResourceInjection}
     */
    public ResourceInjection byName(final String name) {

        final ResourceLiteral resource = new ResourceLiteral();
        resource.setName(name);
        matchingResources.add(resource);
        return this;
    }

    /**
     * Determines the injection target {@link Resource} by its mapped name
     *
     * @param name
     *            the mapped name for the resoure
     * @return this {@link ResourceInjection}
     */
    public ResourceInjection byMappedName(final String name) {

        final ResourceLiteral resource = new ResourceLiteral();
        resource.setMappedName(name);
        matchingResources.add(resource);
        return this;
    }

    /**
     * Determines the injection target {@link Resource} by its lookup name
     *
     * @param lookup
     *            the lookup name for the resoure
     * @return this {@link ResourceInjection}
     */
    public ResourceInjection byLookup(final String lookup) {

        final ResourceLiteral resource = new ResourceLiteral();
        resource.setLookup(lookup);
        matchingResources.add(resource);
        return this;
    }

    /**
     * In addition to the type check the method checks if the field is annotated with {@link Resource} and the
     * {@link Resource} annotation matches on of the specified criteria.
     */
    @Override
    protected boolean isMatching(final Field field) {

        if (!super.isMatching(field)) {
            return false;
        }

        final Resource resourceAnnotation = field.getAnnotation(Resource.class);
        if (resourceAnnotation == null) {
            return false;
        }

        return matchesResourceAnnotation(resourceAnnotation);
    }

    /**
     * Verifies, if the specified resource annotation matches any of the resource specification for this injection.
     * @param resourceAnnotation
     *  the resource annotation of a potential injection target field
     * @return
     *  <code>true</code> if any of the resource specifications of this injection matches the field
     */
    private boolean matchesResourceAnnotation(final Resource resourceAnnotation) {

        for (final Resource expectedResources : this.matchingResources) {
            if (expectedResources.equals(resourceAnnotation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Literal for representing a {@link Resource} annotation
     *
     * @author Gerald Muecke, gerald@moskito.li
     */
    static class ResourceLiteral extends AnnotationLiteral<Resource> implements Resource {

        private static final long serialVersionUID = -1648754563401613075L;
        private String name = "";
        private String lookup = "";
        @SuppressWarnings("rawtypes")
        private Class type = Object.class;
        private String mappedName = "";
        private AuthenticationType authenticationType = AuthenticationType.CONTAINER;
        private boolean shareable = true;
        private String description = "";

        ResourceLiteral() {
            //empty
        }

        ResourceLiteral(final String name, final String lookup, final String mappedName,
                @SuppressWarnings("rawtypes") final Class type, final AuthenticationType authenticationType,
                final boolean shareable, final String description) {

            super();
            this.name = name;
            this.lookup = lookup;
            this.mappedName = mappedName;
            this.type = type;
            this.authenticationType = authenticationType;
            this.shareable = shareable;
            this.description = description;
        }

        @Override
        public String name() {

            return name;
        }

        @Override
        public String lookup() {

            return lookup;
        }

        @Override
        public String mappedName() {

            return mappedName;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Class type() {

            return type;
        }

        @Override
        public AuthenticationType authenticationType() {

            return authenticationType;
        }

        @Override
        public boolean shareable() {

            return shareable;
        }

        @Override
        public String description() {

            return description;
        }

        /**
         * @param name
         *            the name to set
         */
        public void setName(final String name) {

            this.name = name;
        }

        /**
         * @param lookup
         *            the lookup to set
         */
        public void setLookup(final String lookup) {

            this.lookup = lookup;
        }

        /**
         * @param type
         *            the type to set
         */
        public void setType(@SuppressWarnings("rawtypes") final Class type) {

            this.type = type;
        }

        /**
         * @param mappedName
         *            the mappedName to set
         */
        public void setMappedName(final String mappedName) {

            this.mappedName = mappedName;
        }

        /**
         * @param authenticationType
         *            the authenticationType to set
         */
        public void setAuthenticationType(final AuthenticationType authenticationType) {

            this.authenticationType = authenticationType;
        }

        /**
         * @param shareable
         *            the shareable to set
         */
        public void setShareable(final boolean shareable) {

            this.shareable = shareable;
        }

        /**
         * @param description
         *            the description to set
         */
        public void setDescription(final String description) {

            this.description = description;
        }



    }
}
