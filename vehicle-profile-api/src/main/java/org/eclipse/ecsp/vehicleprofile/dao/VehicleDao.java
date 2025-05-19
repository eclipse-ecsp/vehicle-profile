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

package org.eclipse.ecsp.vehicleprofile.dao;

import org.eclipse.ecsp.nosqldao.*;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.vehicleprofile.domain.User;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.exception.DataAccessOperationNotSupoorted;
import org.eclipse.ecsp.vehicleprofile.exception.FailureReason;
import org.eclipse.ecsp.vehicleprofile.exception.InternalServerException;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Stack;

/**
 * VehicleDao class.
 */
@Repository
public class VehicleDao extends IgniteBaseDAOMongoImpl<String, VehicleProfile> {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleDao.class);
    private static final String IGNORE_SCHEMA_VERSION_FIELD_TO_UPDATE_CLASS_NAME = "org.eclipse.ecsp.domain.Version";
    private static final String PROCESS_MERGING_FOR_CLASSES_IN_PACKAGE = "org.eclipse";

    @Override
    public VehicleProfile save(VehicleProfile entity) {
        if (null == entity.getVehicleId()) {
            entity.setVehicleId(new ObjectId().toString());
        }
        return super.save(entity);
    }

    /**
     * merge.
     */

    public boolean merge(VehicleProfile vehicleProfile) {
        LOGGER.info("performing merge request for {}", vehicleProfile);
        Updates updates = new Updates();
        Stack<String> keys = new Stack<>();

        try {
            generateUpdateOp(vehicleProfile, keys, updates);
        } catch (Exception e) {
            LOGGER.error("exception occured while updating record {}", e);
            throw new InternalServerException(FailureReason.UPDATE_FAILED);
        }

        return update(vehicleProfile.getVehicleId(), updates);
    }

    /**
     * associate.
     */
    public boolean associate(String vehicleId, User user, boolean isFirstAssociation) {
        user.setCreatedOn(new Date());
        user.setUpdatedOn(new Date());
        Updates updates = new Updates();
        if (isFirstAssociation) {
            updates.addFieldSet(Constants.DB_FIELD_NAME_AUTHORIZED_USERS, Arrays.asList(user));
        } else {
            updates.addListAppend(Constants.DB_FIELD_NAME_AUTHORIZED_USERS, user);
        }

        return this.update(vehicleId, updates);
    }

    /**
     * findAssociatedVehicles.
     */
    public List<VehicleProfile> findAssociatedVehicles(String userId) {
        IgniteCriteria userIdCriteria = new IgniteCriteria();
        userIdCriteria.field(Constants.DB_FIELD_PATH_USERID).op(Operator.EQ).val(userId);
        IgniteQuery igniteQuery = new IgniteQuery(new IgniteCriteriaGroup(userIdCriteria));
        return find(igniteQuery);
    }

    /**
     * findAssociatedVehiclesData.
     */
    public List<VehicleProfile> findAssociatedVehiclesData(String userId, String pageNumber, String pageSize,
            String sortby, String orderby) {
        LOGGER.info("Inside findAssociatedVehiclesData with userId:{}, pageNumber:{}, pageSize:{}, "
                + "sortby: {}, orderby: {}", userId, pageNumber, pageSize, sortby, orderby);
        IgniteCriteria criteria = new IgniteCriteria();
        criteria.field(Constants.DB_FIELD_PATH_USERID).op(Operator.EQ).val(userId);
        IgniteQuery igniteQuery = new IgniteQuery(new IgniteCriteriaGroup(criteria));
        handlePagination(pageSize, pageNumber, igniteQuery);
        handleSorting(sortby, orderby, igniteQuery);
        String[] fields = { Constants.DB_FIELD_NAME_VEHICLEID, Constants.DB_FIELD_NAME_AUTHORIZED_USERS };
        igniteQuery.setFieldNames(fields);
        LOGGER.debug("find associated vehicles data query {}", igniteQuery);
        return find(igniteQuery);
    }

    /**
     * handlePagination.
     */
    private void handlePagination(String pageSize, String pageNumber, IgniteQuery igniteQuery) {
        LOGGER.debug("Inside handlePagination with pageSize:{}, pageNumber:{}", pageSize, pageNumber);
        if (StringUtils.isBlank(pageNumber) || !NumberUtils.isCreatable(pageNumber)) {
            return;
        }
        if (StringUtils.isBlank(pageSize) || !NumberUtils.isCreatable(pageSize)) {
            return;
        }
        if (pageNumber.equals("0") || pageSize.equals("0")) {
            return;
        }
        igniteQuery.setPageNumber(NumberUtils.createInteger(pageNumber));
        igniteQuery.setPageSize(NumberUtils.createInteger(pageSize));
        LOGGER.debug("Exiting handlePagination, query {}", igniteQuery);
    }

    /**
     * handleSorting.
     */
    private void handleSorting(String sortby, String orderby, IgniteQuery igniteQuery) {
        LOGGER.debug("Inside HandleSorting with sortby: {} , orderby: {}", sortby, orderby);
        if (!StringUtils.isEmpty(sortby)) {
            IgniteOrderBy igniteOrderBy = new IgniteOrderBy();
            igniteOrderBy.byfield(sortby);

            if (orderby.equalsIgnoreCase(Order.ASC.toString())) {
                igniteOrderBy.asc();
            } else {
                igniteOrderBy.desc();
            }
            igniteQuery.orderBy(igniteOrderBy);
        }
        LOGGER.debug("Exiting handleSorting, query {}", igniteQuery);
    }

    /**
     * disassociate.
     */
    public boolean disassociate(String vehicleId, String userId) {
        IgniteCriteria vehicleIdCriteria = new IgniteCriteria();
        vehicleIdCriteria.field(Constants.DB_FIELD_NAME_VEHICLEID).op(Operator.EQ).val(vehicleId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(vehicleIdCriteria);
        IgniteQuery igniteQuery = new IgniteQuery(criteriaGroup);
        Updates updates = new Updates();
        updates.addRemoveOp(Constants.DB_FIELD_PATH_USERID, userId);
        updates.addFieldSet(Constants.LAST_UPDATED_TIME, LocalDateTime.now());
        LOGGER.debug("disassociate query {} and updates {} ", igniteQuery, updates);
        return super.removeAll(igniteQuery, updates);
    }

    /**
     * updateAssociation.
     */
    public boolean updateAssociation(String vehicleId, User user) {
        user.setUpdatedOn(new Date());

        IgniteCriteria vehicleIdCriteria = new IgniteCriteria();
        vehicleIdCriteria.field(Constants.DB_FIELD_NAME_VEHICLEID).op(Operator.EQ).val(vehicleId);
        IgniteCriteria userIdCriteria = new IgniteCriteria();
        userIdCriteria.field(Constants.DB_FIELD_NAME_AUTHORIZED_USERS + "." + Constants.DB_FIELD_NAME_USERID)
                .op(Operator.EQ).val(user.getUserId());

        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(vehicleIdCriteria);
        criteriaGroup.and(userIdCriteria);
        IgniteQuery igniteQuery = new IgniteQuery(criteriaGroup);
        Updates updates = new Updates();
        Stack<String> keys = new Stack<>();
        keys.add(Constants.DB_FIELD_NAME_AUTHORIZED_USERS + Constants.DB_FIELD_ARRAY_ELEMENT_SEPERATOR);
        try {
            generateUpdateOp(user, keys, updates);
        } catch (Exception e) {
            LOGGER.error("Exception occured while generating the db commands {}", e);
            throw new InternalServerException(FailureReason.UPDATE_FAILED);
        }
        return super.update(igniteQuery, updates);
    }

    @Override
    public boolean removeAll(IgniteQuery c, Updates updates) {
        throw new DataAccessOperationNotSupoorted("This operation is not supported");
    }

    @Override
    public boolean update(VehicleProfile entity) {
        return super.update(entity);
    }

    @Override
    public boolean update(String id, Updates updates) {
        return super.update(id, updates);
    }

    @Override
    public boolean[] updateAll(VehicleProfile... entities) {
        throw new DataAccessOperationNotSupoorted("This operation is not supported");
    }

    @Override
    public List<VehicleProfile> saveAll(VehicleProfile... entities) {
        throw new DataAccessOperationNotSupoorted("This operation is not supported");
    }

    @Override
    public int deleteByIds(String... ids) {
        throw new DataAccessOperationNotSupoorted("This operation is not supported");
    }

    @Override
    public boolean deleteAll() {
        throw new DataAccessOperationNotSupoorted("This operation is not supported");
    }

    private void generateUpdateOp(Object object, Stack<String> keysTree, Updates updates)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (object == null) {
            return;
        }

        if (object instanceof Map) {
            generateUpdateOperationValue(object, keysTree, updates);
        }

        // a bean, or a primitive/wrapper, or object that translates to
        // jsonString
        Map<String, Object> properties = PropertyUtils.describe(object);
        Iterator<Entry<String, Object>> propertiesIterator = properties.entrySet().iterator();

        while (propertiesIterator.hasNext()) {
            Entry<String, Object> entry = propertiesIterator.next();
            if (isAnIgnoreField(entry)) {
                continue;
            }
            Object obj = entry.getValue();
            if (obj != null) {
                // process it.
                if (isFurtherProcessingNeeded(obj)) {
                    keysTree.push(entry.getKey());
                    generateUpdateOp(obj, keysTree, updates);
                } else {
                    generateUpdates(entry.getKey(), obj, keysTree, updates);
                }
            }
        }

        if (!keysTree.isEmpty()) {
            keysTree.pop();
        }
    }

    private void generateUpdateOperationValue(Object object, Stack<String> keysTree, Updates updates)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        // iterate over map, and generate updateOperation each value
        Iterator<Entry<String, Object>> mapObjItr = ((Map) object).entrySet().iterator();
        while (mapObjItr.hasNext()) {
            Entry<String, Object> mapObj = mapObjItr.next();
            if (isFurtherProcessingNeeded(mapObj.getValue())) {
                keysTree.push(mapObj.getKey());
                generateUpdateOp(mapObj.getValue(), keysTree, updates);
            } else {
                generateUpdates(mapObj.getKey(), mapObj.getValue(), keysTree, updates);
            }

        }
    }

    private void generateUpdates(String key, Object value, Stack<String> keysTree, Updates updates) {
        String dbKey = generateKey(keysTree, key);
        updates.addFieldSet(dbKey, value);
        LOGGER.info("Update operation {} : {}", dbKey, CommonUtils.maskContent(Objects.toString(value, "")));
    }

    private static boolean isFurtherProcessingNeeded(Object object) {
        if (object == null) {
            return false;
        }
        return object.getClass().getPackage().getName().startsWith(PROCESS_MERGING_FOR_CLASSES_IN_PACKAGE)
                || (object instanceof Map) ? true : false;
    }

    private static boolean isAnIgnoreField(Entry<String, Object> entry) {
        return (entry.getValue() != null
                && (entry.getValue().getClass().getName().equals(IGNORE_SCHEMA_VERSION_FIELD_TO_UPDATE_CLASS_NAME)
                        || entry.getKey().equals("class"))) ? true : false;
    }

    private static String generateKey(Stack<String> keysTree, String currentKey) {
        StringBuilder documentKey = new StringBuilder();
        if (!keysTree.isEmpty()) {
            documentKey.append(StringUtils.join(keysTree, "."));
        }
        if (currentKey != null) {

            if (documentKey.length() != 0) {
                documentKey.append(".");
            }
            documentKey.append(currentKey);
        }
        return documentKey.toString();
    }

    @Override
    public boolean getAndUpdate(VehicleProfile profile) {
        return super.getAndUpdate(profile);
    }

}
