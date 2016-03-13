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

package io.tourniquet.junit.jcr;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import java.util.UUID;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JCRAssertTest {

    @Mock
    private Session session;

    @Mock
    private Node node;

    @Mock
    private NodeType nodeType;

    @Mock
    private NodeTypeManager ntm;

    @Mock
    private Workspace workspace;

    @Mock
    private Property property;

    @Test
    public void testAssertNodeExistByPath_SessionString_PathFound_success() throws Exception {
        final String absPath = "root";
        when(session.getNode(absPath)).thenReturn(node);
        JCRAssert.assertNodeExistByPath(session, absPath);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = AssertionError.class)
    public void testAssertNodeExistByPath_SessionString_PathNotFound_fail() throws Exception {
        final String absPath = "root";
        when(session.getNode(absPath)).thenThrow(PathNotFoundException.class);
        JCRAssert.assertNodeExistByPath(session, absPath);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RepositoryException.class)
    public void testAssertNodeExistByPath_SessionString_exception() throws Exception {
        final String absPath = "root";
        when(session.getNode(absPath)).thenThrow(RepositoryException.class);
        JCRAssert.assertNodeExistByPath(session, absPath);
    }

    @Test(expected = AssertionError.class)
    public void testAssertNodeNotExistByPath_SessionString_PathFound_fail() throws Exception {
        final String absPath = "root";
        when(session.getNode(absPath)).thenReturn(node);
        JCRAssert.assertNodeNotExistByPath(session, absPath);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAssertNodeNotExistByPath_SessionString_PathNotFound_success() throws Exception {
        final String absPath = "root";
        when(session.getNode(absPath)).thenThrow(PathNotFoundException.class);
        JCRAssert.assertNodeNotExistByPath(session, absPath);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RepositoryException.class)
    public void testAssertNodeNotExistByPath_SessionString_exception() throws Exception {
        final String absPath = "root";
        when(session.getNode(absPath)).thenThrow(RepositoryException.class);
        JCRAssert.assertNodeNotExistByPath(session, absPath);
    }

    @Test
    public void testAssertNodeExistByPathNodeString_PathFound_success() throws Exception {
        final String relPath = "child";
        when(node.getNode(relPath)).thenReturn(node);
        JCRAssert.assertNodeExist(node, relPath);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = AssertionError.class)
    public void testAssertNodeExistByPathNodeString_PathNotFound_fail() throws Exception {
        final String relPath = "child";
        when(node.getNode(relPath)).thenThrow(PathNotFoundException.class);
        JCRAssert.assertNodeExist(node, relPath);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RepositoryException.class)
    public void testAssertNodeExistByPathNodeString_exception() throws Exception {
        final String relPath = "child";
        when(node.getNode(relPath)).thenThrow(RepositoryException.class);
        JCRAssert.assertNodeExist(node, relPath);
    }

    @Test
    public void testAssertStringPropertyEquals_equals_sucess() throws Exception {
        // prepare
        final String propertyName = "property";
        final String propertyValue = "value";
        when(node.getPath()).thenReturn("root");
        when(node.hasProperty(propertyName)).thenReturn(true);
        when(node.getProperty(propertyName)).thenReturn(property);
        when(property.getType()).thenReturn(PropertyType.STRING);
        when(property.getString()).thenReturn(propertyValue);
        // act
        JCRAssert.assertStringPropertyEquals(node, propertyName, "value");
    }

    @Test(expected = AssertionError.class)
    public void testAssertStringPropertyEquals_notEquals() throws Exception {
        // prepare
        final String propertyName = "property";
        final String propertyValue = "value";
        when(node.getPath()).thenReturn("root");
        when(node.hasProperty(propertyName)).thenReturn(true);
        when(node.getProperty(propertyName)).thenReturn(property);
        when(property.getType()).thenReturn(PropertyType.STRING);
        when(property.getString()).thenReturn(propertyValue);
        // act
        JCRAssert.assertStringPropertyEquals(node, propertyName, "anotherValue");
    }

    @Test(expected = AssertionError.class)
    public void testAssertStringPropertyEquals_wrongType_fail() throws Exception {
        // prepare
        final String propertyName = "property";
        final String propertyValue = "value";
        when(node.getPath()).thenReturn("root");
        when(node.hasProperty(propertyName)).thenReturn(true);
        when(node.getProperty(propertyName)).thenReturn(property);
        when(property.getType()).thenReturn(PropertyType.LONG);
        when(property.getString()).thenReturn(propertyValue);
        // act
        JCRAssert.assertStringPropertyEquals(node, propertyName, "value");
    }

    @Test(expected = AssertionError.class)
    public void testAssertStringPropertyEquals_noSuchProperty() throws Exception {
        // prepare
        final String propertyName = "property";
        final String propertyValue = "value";
        when(node.getPath()).thenReturn("root");
        when(node.hasProperty(propertyName)).thenReturn(false);
        when(node.getProperty(propertyName)).thenReturn(property);
        when(property.getType()).thenReturn(PropertyType.STRING);
        when(property.getString()).thenReturn(propertyValue);
        // act
        JCRAssert.assertStringPropertyEquals(node, propertyName, "value");
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RepositoryException.class)
    public void testAssertStringPropertyEquals_exception() throws Exception {
        // prepare
        final String propertyName = "property";
        final String propertyValue = "value";
        when(node.getPath()).thenReturn("root");
        when(node.hasProperty(propertyName)).thenThrow(RepositoryException.class);
        when(node.getProperty(propertyName)).thenReturn(property);
        when(property.getType()).thenReturn(PropertyType.STRING);
        when(property.getString()).thenReturn(propertyValue);
        // act
        JCRAssert.assertStringPropertyEquals(node, propertyName, "value");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAssertNodeNotExistById_notExist_success() throws Exception {
        final String itemId = UUID.randomUUID().toString();
        when(session.getNodeByIdentifier(itemId)).thenThrow(ItemNotFoundException.class);
        JCRAssert.assertNodeNotExistById(session, itemId);
    }

    @Test(expected = AssertionError.class)
    public void testAssertNodeNotExistById_exist_fail() throws Exception {
        final String itemId = UUID.randomUUID().toString();
        when(session.getNodeByIdentifier(itemId)).thenReturn(node);
        JCRAssert.assertNodeNotExistById(session, itemId);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RepositoryException.class)
    public void testAssertNodeNotExistById_exception() throws Exception {
        final String itemId = UUID.randomUUID().toString();
        when(session.getNodeByIdentifier(itemId)).thenThrow(RepositoryException.class);
        JCRAssert.assertNodeNotExistById(session, itemId);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = AssertionError.class)
    public void testAssertNodeExistById_notExist_fail() throws Exception {
        final String itemId = UUID.randomUUID().toString();
        when(session.getNodeByIdentifier(itemId)).thenThrow(ItemNotFoundException.class);
        JCRAssert.assertNodeExistById(session, itemId);
    }

    @Test
    public void testAssertNodeExistById_exist_success() throws Exception {
        final String itemId = UUID.randomUUID().toString();
        when(session.getNodeByIdentifier(itemId)).thenReturn(node);
        JCRAssert.assertNodeExistById(session, itemId);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RepositoryException.class)
    public void testAssertNodeExistById_exception() throws Exception {
        final String itemId = UUID.randomUUID().toString();
        when(session.getNodeByIdentifier(itemId)).thenThrow(RepositoryException.class);
        JCRAssert.assertNodeExistById(session, itemId);
    }

    @Test
    public void testAssertPrimaryNodeType_matching_success() throws Exception {
        final String nodeTypeName = "nt:unstructured";
        when(node.getPrimaryNodeType()).thenReturn(nodeType);
        when(nodeType.getName()).thenReturn(nodeTypeName);
        JCRAssert.assertPrimaryNodeType(node, nodeTypeName);
    }

    @Test(expected = AssertionError.class)
    public void testAssertPrimaryNodeType_notMatching_fail() throws Exception {
        final String nodeTypeName = "nt:unstructured";
        when(node.getPrimaryNodeType()).thenReturn(nodeType);
        when(nodeType.getName()).thenReturn("nt:resource");
        JCRAssert.assertPrimaryNodeType(node, nodeTypeName);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RepositoryException.class)
    public void testAssertPrimaryNodeType_exception() throws Exception {
        final String nodeTypeName = "nt:unstructured";
        when(node.getPrimaryNodeType()).thenThrow(RepositoryException.class);
        JCRAssert.assertPrimaryNodeType(node, nodeTypeName);
    }

    @Test
    public void testAssertMixinNodeType_match_success() throws Exception {
        final String nodeTypeName = "mix:title";
        final NodeType[] mixinTypes = new NodeType[] {
                nodeType
        };
        when(node.getMixinNodeTypes()).thenReturn(mixinTypes);
        when(nodeType.getName()).thenReturn(nodeTypeName);
        JCRAssert.assertMixinNodeType(node, nodeTypeName);
    }

    @Test(expected = AssertionError.class)
    public void testAssertMixinNodeType_notMatch_fail() throws Exception {
        final String nodeTypeName = "mix:title";
        final NodeType[] mixinTypes = new NodeType[] {
                nodeType
        };
        when(node.getMixinNodeTypes()).thenReturn(mixinTypes);
        when(nodeType.getName()).thenReturn("mix:versionable");
        JCRAssert.assertMixinNodeType(node, nodeTypeName);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RepositoryException.class)
    public void testAssertMixinNodeType_exception() throws Exception {
        final String nodeTypeName = "mix:title";
        when(node.getMixinNodeTypes()).thenThrow(RepositoryException.class);
        JCRAssert.assertMixinNodeType(node, nodeTypeName);
    }

    @Test
    public void testAssertNodeTypeExists_exists_success() throws Exception {
        // prepare
        final String nodeTypeName = "test:nodeType";
        when(session.getWorkspace()).thenReturn(workspace);
        when(workspace.getNodeTypeManager()).thenReturn(ntm);
        when(ntm.hasNodeType(nodeTypeName)).thenReturn(true);
        // act
        JCRAssert.assertNodeTypeExists(session, "test:nodeType");
        // nothing happens here
    }

    @Test(expected = AssertionError.class)
    public void testAssertNodeTypeExists_notExists_fail() throws Exception {
        // prepare
        final String nodeTypeName = "test:nodeType";
        when(session.getWorkspace()).thenReturn(workspace);
        when(workspace.getNodeTypeManager()).thenReturn(ntm);
        when(ntm.hasNodeType(nodeTypeName)).thenReturn(false);
        // act
        JCRAssert.assertNodeTypeExists(session, "test:nodeType");
        // nothing happens here
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RepositoryException.class)
    public void testAssertNodeTypeExists_exception() throws Exception {
        // prepare
        when(session.getWorkspace()).thenReturn(workspace);
        when(workspace.getNodeTypeManager()).thenThrow(RepositoryException.class);
        // act
        JCRAssert.assertNodeTypeExists(session, "test:nodeType");
    }

}
