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

package org.eclipse.ecsp.vehicleprofile.constants;

/**
 * Constants class.
 */
public class Constants {

    private Constants() {}


    /**
     * VEHICLE_PROFILE_CREATED_NOTIFICATION_EVENT.
     */

    /**
     * VEHICLE_PROFILE_CREATED_NOTIFICATION_EVENT.
     */
    public static final String VEHICLE_PROFILE_CREATED_NOTIFICATION_EVENT = 
            "VEHICLE_PROFILE_CREATED_NOTIFICATION_EVENT";

    /**
     * SEARCH_QUERY_PARAMETER_NAME_SERVICEID.
     */

    /**
     * SEARCH_QUERY_PARAMETER_NAME_SERVICEID.
     */
    public static final String SEARCH_QUERY_PARAMETER_NAME_SERVICEID = "serviceId";

    /**
     * REQUEST_ID.
     */

    /**
     * REQUEST_ID.
     */
    public static final String REQUEST_ID = "requestId";

    /**
     * CLIENT_REQUEST_ID.
     */

    /**
     * CLIENT_REQUEST_ID.
     */
    public static final String CLIENT_REQUEST_ID = "clientRequestId";

    /**
     * CONTEXT_PATH.
     */

    /**
     * CONTEXT_PATH.
     */
    public static final String CONTEXT_PATH = "path";

    /**
     * TC_STATUS_DISAGREE.
     */

    /**
     * TC_STATUS_DISAGREE.
     */
    public static final String TC_STATUS_DISAGREE = "DISAGREE";

    /**
     * TC_STATUS_AGREE.
     */

    /**
     * TC_STATUS_AGREE.
     */
    public static final String TC_STATUS_AGREE = "AGREE";


    /**
     * DB_FIELD_PATH_SEPEARATOR.
     */

    /**
     * DB_FIELD_PATH_SEPEARATOR.
     */
    public static final String DB_FIELD_PATH_SEPEARATOR = ".";

    /**
     * DB_FIELD_NAME_AUTHORIZED_USERS.
     */

    /**
     * DB_FIELD_NAME_AUTHORIZED_USERS.
     */
    public static final String DB_FIELD_NAME_AUTHORIZED_USERS = "authorizedUsers";

    /**
     * DB_FIELD_NAME_VEHICLEID.
     */

    /**
     * DB_FIELD_NAME_VEHICLEID.
     */
    public static final String DB_FIELD_NAME_VEHICLEID = "vehicleId";

    /**
     * DB_FIELD_NAME_USERID.
     */

    /**
     * DB_FIELD_NAME_USERID.
     */
    public static final String DB_FIELD_NAME_USERID = "userId";

    /**
     * DB_FIELD_NAME_DISASSOCIATED.
     */

    /**
     * DB_FIELD_NAME_DISASSOCIATED.
     */
    public static final String DB_FIELD_NAME_DISASSOCIATED = "disassociated";

    /**
     * DB_FIELD_NAME_STATUS.
     */

    /**
     * DB_FIELD_NAME_STATUS.
     */
    public static final String DB_FIELD_NAME_STATUS = "status";

    /**
     * DB_FIELD_NAME_ROLE.
     */

    /**
     * DB_FIELD_NAME_ROLE.
     */
    public static final String DB_FIELD_NAME_ROLE = "role";

    /**
     * DB_FIELD_NAME_TC.
     */

    /**
     * DB_FIELD_NAME_TC.
     */
    public static final String DB_FIELD_NAME_TC = "tc";

    /**
     * DB_FIELD_PATH_USERID.
     */

    /**
     * DB_FIELD_PATH_USERID.
     */
    public static final String DB_FIELD_PATH_USERID = DB_FIELD_NAME_AUTHORIZED_USERS + DB_FIELD_PATH_SEPEARATOR
            + DB_FIELD_NAME_USERID;

    /**
     * DB_FIELD_ARRAY_ELEMENT_SEPERATOR.
     */

    /**
     * DB_FIELD_ARRAY_ELEMENT_SEPERATOR.
     */
    public static final String DB_FIELD_ARRAY_ELEMENT_SEPERATOR = ".$";

    /**
     * DB_FIELD_NAME_CLIENTID.
     */

    /**
     * DB_FIELD_NAME_CLIENTID.
     */
    public static final String DB_FIELD_NAME_CLIENTID = "clientId";

    /**
     * KAFKA_CLIENT_KEYSTORE_PASS_KEY.
     */

    /**
     * KAFKA_CLIENT_KEYSTORE_PASS_KEY.
     */
    public static final String KAFKA_CLIENT_KEYSTORE_PASS_KEY = "kafka_client_keystore_password";


    /**
     * KAFKA_CLIENT_KEY_PASS_KEY.
     */

    /**
     * KAFKA_CLIENT_KEY_PASS_KEY.
     */
    public static final String KAFKA_CLIENT_KEY_PASS_KEY = "kafka_client_key_password";


    /**
     * KAFKA_CLIENT_TRUSTSTORE_PASS_KEY.
     */

    /**
     * KAFKA_CLIENT_TRUSTSTORE_PASS_KEY.
     */
    public static final String KAFKA_CLIENT_TRUSTSTORE_PASS_KEY = "kafka_client_truststore_password";


    /**
     * VP_200.
     */

    /**
     * VP_200.
     */
    public static final String VP_200 = "vp-200xxx";

    /**
     * API_SUCCESS.
     */

    /**
     * API_SUCCESS.
     */
    public static final String API_SUCCESS = "Success";


    /**
     * VP_404.
     */

    /**
     * VP_404.
     */
    public static final String VP_404 = "vp-404";

    /**
     * RESOURCE_NOT_FOUND.
     */

    /**
     * RESOURCE_NOT_FOUND.
     */
    public static final String RESOURCE_NOT_FOUND = "Not Found";

    /**
     * USER_ID.
     */

    /**
     * USER_ID.
     */
    public static final String USER_ID = "user-id";

    /**
     * LAST_UPDATED_TIME.
     */

    /**
     * LAST_UPDATED_TIME.
     */
    public static final String LAST_UPDATED_TIME = "lastUpdatedTime";


    /**
     * CODE_VALUE_DECODER.
     */

    /**
     * CODE_VALUE_DECODER.
     */
    public static final String CODE_VALUE_DECODER = "codevalue";

    /**
     * POSITION_MATCHER_DECODER.
     */

    /**
     * POSITION_MATCHER_DECODER.
     */
    public static final String POSITION_MATCHER_DECODER = "positionmatcher";

    /**
     * DEFAULT_DECODER.
     */

    /**
     * DEFAULT_DECODER.
     */
    public static final String DEFAULT_DECODER = "default";

    /**
     * VEHICLE_SPECIFICATION.
     */

    /**
     * VEHICLE_SPECIFICATION.
     */
    public static final String VEHICLE_SPECIFICATION = "vehiclespecification";


    /**
     * SKIP_UNKOWN_PROPERTIES.
     */

    /**
     * SKIP_UNKOWN_PROPERTIES.
     */
    public static final boolean SKIP_UNKOWN_PROPERTIES = false;


    /**
     * NOT_APPLICABLE.
     */

    /**
     * NOT_APPLICABLE.
     */
    public static final String NOT_APPLICABLE = "NA";

    /**
     * MY_CAR.
     */

    /**
     * MY_CAR.
     */
    public static final String MY_CAR = "My Car";

    // Notification Event Variables

    /**
     * MMY_EVENT_ID.
     */

    /**
     * MMY_EVENT_ID.
     */
    public static final String MMY_EVENT_ID = "MMY";

    /**
     * GENERIC_EVENT_ID.
     */

    /**
     * GENERIC_EVENT_ID.
     */
    public static final String GENERIC_EVENT_ID = "GenericNotificationEvent";

    /**
     * VIN_KEY.
     */

    /**
     * VIN_KEY.
     */
    public static final String VIN_KEY = "vin";

    /**
     * NOTIFICATION_ID_KEY.
     */

    /**
     * NOTIFICATION_ID_KEY.
     */
    public static final String NOTIFICATION_ID_KEY = "notificationId";

    /**
     * BENCHMODE.
     */

    /**
     * BENCHMODE.
     */
    public static final String BENCHMODE = "1";

    /**
     * DUMMY_KEY.
     */

    /**
     * DUMMY_KEY.
     */
    public static final String DUMMY_KEY = "dummy";

    /**
     * VALUE_KEY.
     */

    /**
     * VALUE_KEY.
     */
    public static final String VALUE_KEY = "value";

    /**
     * UNKNOWN.
     */

    /**
     * UNKNOWN.
     */
    public static final String UNKNOWN = "UNKNOWN";

    /**
     * LOGICAL_OPERATOR_KEY.
     */

    /**
     * LOGICAL_OPERATOR_KEY.
     */
    public static final String LOGICAL_OPERATOR_KEY = "logicalOptr";

    /**
     * OR_OPERATOR.
     */

    /**
     * OR_OPERATOR.
     */
    public static final String OR_OPERATOR = "OR";

    /**
     * TIMEZONE_VALUE_300.
     */

    /**
     * TIMEZONE_VALUE_300.
     */
    public static final short TIMEZONE_VALUE_300 = 300;
    

    /**
     * KMS_CONTEXT_KEY.
     */

    /**
     * KMS_CONTEXT_KEY.
     */
    public static final String KMS_CONTEXT_KEY = "ignite-security-key";

    /**
     * KMS_CONTEXT_VALUE.
     */

    /**
     * KMS_CONTEXT_VALUE.
     */
    public static final String KMS_CONTEXT_VALUE = "PII";

    /**
     * VIN.
     */

    /**
     * VIN.
     */
    public static final String VIN = "VIN";

    /**
     * HCP.
     */

    /**
     * HCP.
     */
    public static final String HCP = "HCP";
}