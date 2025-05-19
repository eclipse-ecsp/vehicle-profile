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

package org.eclipse.ecsp.vehicleprofile.utils;

import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfileToVehicleAdapter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utils.
 */
public class Utils {
    private static String GETTER_PREFIX = "get";
    private static String SETTER_PREFIX = "set";
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(Utils.class);

    private Utils() {}

    /**
     * getNodesFromPath.
     */
    public static String[] getNodesFromPath(String requestPath, String requestId) {
        if (!requestPath.contains(requestId)) {
            return null;
        }

        String path = requestPath.substring(requestPath.indexOf(requestId) + requestId.length());
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.length() == 0) {
            return null;
        }

        return path.split("/");
    }

    /**
     * readNodeValue.
     */
    public static Object readNodeValue(Object dataObj, String[] nodes) throws IllegalArgumentException {
        if ((null == dataObj) || (null == nodes) || (nodes.length == 0)) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        Object retVal = dataObj;
        for (String node : nodes) {
            if (null == retVal) {
                throw new IllegalArgumentException("Node not accessible");
            }
            retVal = readNodeValue(retVal, node);
        }

        return retVal;
    }

    /**
     * readNodeValue.
     */
    public static Object readNodeValue(Object dataObj, String node) throws IllegalArgumentException {
        if ((null == dataObj) || (null == node) || (node.length() == 0)) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        if (dataObj instanceof Object[]) {
            try {
                Object[] array = (Object[]) dataObj;
                int index = Integer.parseInt(node);
                if (index < array.length) {
                    return array[index];
                } else {
                    throw new IllegalArgumentException("Array element does not exist");
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Array index must be numeric");
            }
        }

        String getterName = new StringBuilder(GETTER_PREFIX).append(node.substring(0, 1).toUpperCase())
                .append(node.substring(1)).toString();
        Method m = findMethodByName(dataObj.getClass(), getterName);
        if (null == m) {
            throw new IllegalArgumentException("Node not accessible");
        }

        try {
            return m.invoke(dataObj);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalArgumentException("Node not accessible");
        }
    }

    /**
     * getNodeClass.
     */
    public static Class<?> getNodeClass(Class<?> dataClass, String[] nodes) {
        if ((null == dataClass) || (null == nodes) || (nodes.length == 0)) {
            return null;
        }

        Class<?> retVal = dataClass;
        for (String node : nodes) {
            retVal = getNodeClass(retVal, node);
            if (null == retVal) {
                break;
            }
        }

        return retVal;
    }

    /**
     * getNodeClass.
     */
    public static Class<?> getNodeClass(Class<?> dataClass, String node) {
        if ((null == dataClass) || (null == node) || (node.length() == 0)) {
            return null;
        }

        if (dataClass.isArray()) {
            try {
                Integer.parseInt(node); // Make sure the node is numeric
                return dataClass.getComponentType();
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        String getterName = new StringBuilder(GETTER_PREFIX).append(node.substring(0, 1).toUpperCase())
                .append(node.substring(1)).toString();
        Method m = findMethodByName(dataClass, getterName);
        if (null == m) {
            return null;
        }

        return m.getReturnType();
    }

    /**
     * setNodeValue.
     */
    public static Object setNodeValue(Object dataObj, String[] nodes, Class<?> nodeClass, Object nodeValue)
            throws IllegalArgumentException, IllegalStateException, InvocationTargetException {

        if ((null == dataObj) || (null == nodes) || (nodes.length == 0) || (null == nodeClass)) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        Object parentNodeObj = null;
        Object nodeObj = dataObj;
        Method setter = null;
        Method getter = null;
        for (int i = 0; i < (nodes.length - 1); i++) {
            if (nodeObj instanceof Object[]) {
                try {
                    nodeObj = getNodeObj(nodeObj, nodes[i]);
                    continue;
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Array index must be numeric");
                }
            }

            String setterName = new StringBuilder(SETTER_PREFIX).append(nodes[i].substring(0, 1).toUpperCase())
                    .append(nodes[i].substring(1)).toString();
            setter = findMethodByName(nodeObj.getClass(), setterName);
            String getterName = new StringBuilder(GETTER_PREFIX).append(nodes[i].substring(0, 1).toUpperCase())
                    .append(nodes[i].substring(1)).toString();
            getter = findMethodByName(nodeObj.getClass(), getterName);
            if (null == getter) {
                throw new IllegalArgumentException("Node not accessible");
            }
            try {
                parentNodeObj = nodeObj;
                nodeObj = getter.invoke(nodeObj);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new IllegalArgumentException("Node not accessible");
            }

            if (null == nodeObj) {
                throw new IllegalArgumentException("Node not accessible");
            }
        }

        String targetNode = nodes[nodes.length - 1];
        if (nodeObj instanceof Object[]) {
            try {
                return getObjects(nodeClass, nodeValue, (Object[]) nodeObj, targetNode, setter, parentNodeObj);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Array index must be numeric");
            }
        }

        String setterName = new StringBuilder(SETTER_PREFIX).append(targetNode.substring(0, 1).toUpperCase())
                .append(targetNode.substring(1)).toString();
        setter = findMethodByName(nodeObj.getClass(), setterName);
        if (null == setter) {
            throw new IllegalArgumentException("Node not accessible");
        }

        try {
            setter.invoke(nodeObj, nodeValue);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new InvocationTargetException(ex);
        }

        return nodeObj;
    }

    private static Object getNodeObj(Object nodeObj, String nodes) {
        Object[] array = (Object[]) nodeObj;
        int index = Integer.parseInt(nodes);
        if (index < array.length) {
            nodeObj = array[index];
            return nodeObj;
        } else {
            throw new IllegalArgumentException("Array element does not exist");
        }
    }

    private static Object [] getObjects(Class<?> nodeClass,
                                                 Object nodeValue,
                                                 Object[] nodeObj,
                                                 String targetNode,
                                                 Method setter,
                                                 Object parentNodeObj) throws InvocationTargetException {
        Object[] array = nodeObj;
        int index = Integer.parseInt(targetNode);
        if (index < array.length) {
            if (null == nodeValue) {
                // Remove element from an array
                if (null == setter) {
                    // Setter is not available for some reason, so
                    // unable to set updated
                    // array. Should never hit this though.
                    throw new IllegalStateException("Something went wrong");
                }
                Object newArray = Array.newInstance(nodeClass, array.length - 1);
                for (int i = 0, j = 0; i < array.length; i++) {
                    if (i == index) {
                        continue;
                    }
                    Array.set(newArray, j++, array[i]);
                }
                try {
                    setter.invoke(parentNodeObj, newArray);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new InvocationTargetException(ex);
                }
                array = (Object[]) newArray;
            } else {
                array[index] = nodeValue;
            }
            return array;
        } else {
            throw new IllegalArgumentException("Array element does not exist");
        }
    }

    /**
     * findMethodByName.
     */
    private static Method findMethodByName(Class<?> owner, String name) {
        try {
            return owner.getMethod(name);
        } catch (NoSuchMethodException ex) {
            Method[] methods = owner.getMethods();
            for (Method method : methods) {
                if (method.getName().equalsIgnoreCase(name)) {
                    return method;
                }
            }
        }

        return null;
    }

    /**
     * getVehicleAdapters.
     */
    public static List<VehicleProfileToVehicleAdapter> getVehicleAdapters(
            VehicleProfileToVehicleAdapterProvider adapterProvider, List<VehicleProfile> vehicleProfiles) {
        if (null == vehicleProfiles) {
            return null;
        }
        List<VehicleProfileToVehicleAdapter> vehicleProfileAdapters = new ArrayList<>(vehicleProfiles.size());
        for (VehicleProfile vehicleProfile : vehicleProfiles) {
            vehicleProfileAdapters.add(adapterProvider.getAdapter(vehicleProfile));
        }
        return vehicleProfileAdapters;
    }

    /**
     * formatTopicName.
     */
    public static String formatTopicName(String topicName, String tenant, String env) {
        return MessageFormat.format(topicName, tenant, env);
    }

    /**
     * mdc.
     */
    public static void mdc(HttpServletRequest request) {
        if (request != null) {
            mdc(request.getHeader(Constants.REQUEST_ID), request.getHeader(Constants.CLIENT_REQUEST_ID), null, null,
                    request.getRequestURI());
        }
    }

    /**
     * mdc.
     */
    public static void mdc(String requestId, String clientRequestId, String messageId, String vehicleId, String path) {
        if (StringUtils.isBlank(requestId)) {
            requestId = UUID.randomUUID().toString();
            LOGGER.info("requestId not received, hence generating one {}", requestId);
        }
        if (requestId != null) {
            MDC.put(Constants.REQUEST_ID, requestId);
        }
        if (clientRequestId != null) {
            MDC.put(Constants.CLIENT_REQUEST_ID, clientRequestId);
        }
        if (path != null) {
            MDC.put(Constants.CONTEXT_PATH, path);
        }
    }

}
