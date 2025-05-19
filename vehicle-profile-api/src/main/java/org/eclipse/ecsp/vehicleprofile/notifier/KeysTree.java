/*
 *  *******************************************************************************
 *  Copyright (c) 2023-24 Harman International
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  *******************************************************************************
 */

package org.eclipse.ecsp.vehicleprofile.notifier;


import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *KeysTree class.
 */
public class KeysTree {
    private static final String PROPERTY_PATH_SEPERATOR = ".";
    private static final String PROPERTY_PATH_SEPERATOR_WITH_ESCAPE = "\\.";
    private final KeysTree.KeyNode root;
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(KeysTree.class);

    /**
     * KeysTree constructor.
     */
    public KeysTree() {
        super();
        root = new KeyNode("ROOT", "ROOT");
    }

    /**
     * Get root.
     *
     * @return keynode
     */
    public KeysTree.KeyNode getRoot() {
        return root;
    }

    /**
     * populate the tree.
     *
     * @param completeKey string
     * @param topicName   string
     */
    public void populate(String completeKey, String topicName) {
        if (completeKey == null || completeKey.isEmpty()) {
            return;
        }
        String[] keys = completeKey.split(PROPERTY_PATH_SEPERATOR_WITH_ESCAPE);
        // ecus.hu.provisionedServices.services,
        KeysTree.KeyNode parentNode = root;
        StringBuffer path = new StringBuffer();
        for (int i = 0; i < keys.length; i++) {
            if (path.length() != 0) {
                path.append(PROPERTY_PATH_SEPERATOR);
            }
            path.append(keys[i]);
            KeysTree.KeyNode newKeyNode = new KeyNode(keys[i], path.toString());
            KeysTree.KeyNode keyNodeInTree = parentNode.getChild(newKeyNode);
            if (keyNodeInTree == null) {
                LOGGER.debug("not found child {} hence adding it", newKeyNode);
                parentNode.addChild(newKeyNode);
                keyNodeInTree = newKeyNode;
            }
            parentNode = keyNodeInTree;
        }
        parentNode.addTopic(topicName);
    }


    public static class KeyNode {
        private final String key;
        private final String path;
        private Set<String> topicNames;
        private List<KeysTree.KeyNode> children;

        /**
         * KeyNode constructor.
         *
         * @param key string
         * @param completeKey string
         */
        public KeyNode(String key, String completeKey) {
            super();
            this.key = key;
            this.path = completeKey;
        }


        /**
         * add the topic.
         *
         * @param topicName string
         */
        public void addTopic(String topicName) {
            if (this.topicNames == null) {
                this.topicNames = new HashSet<>();
            }
            topicNames.add(topicName);
        }

        /**
         * add the child.
         *
         * @param keyNode KeyNode
         */
        public void addChild(KeysTree.KeyNode keyNode) {
            if (keyNode == null) {
                return;
            }
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(keyNode);
        }

        /**
         * get the child.
         *
         * @param keyNode KeyNode
         * @return KeyNode
         */
        public KeysTree.KeyNode getChild(KeysTree.KeyNode keyNode) {
            if (children == null) {
                return null;
            }
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i).equals(keyNode)) {
                    return children.get(i);
                }
            }
            return null;
        }

        /**
         * Is leaf.
         *
         * @return boolean
         */
        public boolean isLeaf() {
            return children == null;
        }

        /**
         * Get children.
         *
         * @return keynode list
         */
        public List<KeysTree.KeyNode> getChildren() {
            return children;
        }

        /**
         * Set children.
         *
         * @param children keynode list
         */
        public void setChildren(List<KeysTree.KeyNode> children) {
            this.children = children;
        }

        /**
         * Get the key.
         *
         * @return string
         */
        public String getKey() {
            return key;
        }

        /**
         * Get the path.
         *
         * @return string
         */
        public String getPath() {
            return path;
        }

        /**
         * Get the topic names.
         *
         * @return string set
         */
        public Set<String> getTopicNames() {
            return topicNames;
        }

        /**
         * Set the topic names.
         *
         * @param topicNames string set
         */
        public void setTopicNames(Set<String> topicNames) {
            this.topicNames = topicNames;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            KeysTree.KeyNode other = (KeysTree.KeyNode) obj;
            if (path == null) {
                if (other.path != null) {
                    return false;
                }
            } else if (!path.equals(other.path)) {
                return false;
            }
            return true;
        }

        /**
         * toString.
         */
        @Override
        public String toString() {
            return "KeyNode [key=" + key + ", hierarchyKey=" + path + ", topicNames=" + topicNames + ", children="
                    + children + "]";
        }
    }

}