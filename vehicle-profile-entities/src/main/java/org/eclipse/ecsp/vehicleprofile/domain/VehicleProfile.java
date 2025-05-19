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

package org.eclipse.ecsp.vehicleprofile.domain;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.AuditableIgniteEntity;
import org.eclipse.ecsp.entities.IgniteEntity;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * VehicleProfile.
 */
@EqualsAndHashCode
@Entity(value = "VehicleProfile")
@ToString
@Indexes({ @Index(fields = @Field(value = "vin")),
    @Index(fields = @Field(value = "ecus.hu.clientId"), options = @IndexOptions(disableValidation = true)),
    @Index(fields = @Field(value = "ecus.telematics.clientId"), options = @IndexOptions(disableValidation = true)),
    @Index(fields = @Field(value = "ecus.cadm2.clientId"), options = @IndexOptions(disableValidation = true)),
    @Index(fields = @Field(value = "ecus.sgw.clientId"), options = @IndexOptions(disableValidation = true)),
    @Index(fields = @Field(value = "ecus.dongle.clientId"), options = @IndexOptions(disableValidation = true)),
    @Index(fields = @Field(value = "ecus.hu.serialNo"), options = @IndexOptions(disableValidation = true)),
    @Index(fields = @Field(value = "ecus.telematics.serialNo"), options = @IndexOptions(disableValidation = true)),
    @Index(fields = @Field(value = "ecus.cadm2.serialNo"), options = @IndexOptions(disableValidation = true)),
    @Index(fields = @Field(value = "ecus.sgw.serialNo"), options = @IndexOptions(disableValidation = true)),
    @Index(fields = @Field(value = "modemInfo.msisdn"), options = @IndexOptions(disableValidation = true)),
    @Index(fields = @Field(value = "modemInfo.imei"), options = @IndexOptions(disableValidation = true)),
    @Index(fields = @Field(value = "modemInfo.iccid"), options = @IndexOptions(disableValidation = true)),
    @Index(fields = @Field(value = "authorizedUsers.userId"), options = @IndexOptions(disableValidation = true)) })
public class VehicleProfile implements IgniteEntity, AuditableIgniteEntity {

    private Version schemaVersion = Version.V1_0;
    private String vin;
    @Id
    private String vehicleId;
    private Date createdOn;
    private Date updatedOn;
    private Date productionDate;
    private String soldRegion;
    private String saleDate;
    private VehicleAttributes vehicleAttributes;
    private List<User> authorizedUsers;
    private ModemInfo modemInfo;
    private String vehicleArchType;
    private Map<String, ? extends Ecu> ecus;
    private Boolean dummy;
    private Map<String, Event> events;
    private Map<String, Map<String, String>> customParams;
    private VehicleCapabilities vehicleCapabilities;
    private SaleAttributes saleAttributes;
    private Boolean eolValidationInProgress;
    private Boolean blockEnrollment;
    private Map<String, ? extends AuthorizedPartner> authorizedPartners;
    private String epiddbChecksum;
    private String connectedPlatform;

    private LocalDateTime lastUpdatedTime;

    /**
     * Get customer parameters.
     *
     * @return map
     */
    public Map<String, Map<String, String>> getCustomParams() {
        return customParams;
    }

    /**
     * Set customer parameters.
     *
     * @param customParams map
     */
    public void setCustomParams(Map<String, Map<String, String>> customParams) {
        this.customParams = customParams;
    }

    /**
     * Get schema version.
     *
     * @return version
     */
    public Version getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * Get sale attributes.
     *
     * @return sale attributes
     */
    public SaleAttributes getSaleAttributes() {
        return saleAttributes;
    }

    /**
     * Set sale attributes.
     *
     * @param saleAttributes sale attributes
     */
    public void setSaleAttributes(SaleAttributes saleAttributes) {
        this.saleAttributes = saleAttributes;
    }

    /**
     * Set schema version.
     *
     * @param version version
     */
    public void setSchemaVersion(Version version) {
        this.schemaVersion = version;
    }

    /**
     * Getvin.
     *
     * @return string
     */
    public String getVin() {
        return vin;
    }

    /**
     * Get vehicle id.
     *
     * @return string
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * Get created on.
     *
     * @return date
     */
    public Date getCreatedOn() {
        return createdOn;
    }

    /**
     * Get updated on.
     *
     * @return date
     */
    public Date getUpdatedOn() {
        return updatedOn;
    }

    /**
     * Get production date.
     *
     * @return date
     */
    public Date getProductionDate() {
        return productionDate;
    }

    /**
     * Get vehicle attributes.
     *
     * @return vehicle attributes
     */
    public VehicleAttributes getVehicleAttributes() {
        return vehicleAttributes;
    }

    /**
     * Get authorized users.
     *
     * @return list of authorized users
     */
    public List<User> getAuthorizedUsers() {
        return authorizedUsers;
    }

    /**
     * Get modem info.
     *
     * @return modem info
     */
    public ModemInfo getModemInfo() {
        return modemInfo;
    }


    /**
     * Get vehicle architecture type.
     *
     * @return string
     */
    public String getVehicleArchType() {
        return vehicleArchType;
    }

    /**
     * Get ECUS.
     *
     * @return map
     */
    public Map<String, ? extends Ecu> getEcus() {
        return ecus;
    }


    /**
     * Set vin.
     *
     * @param vin string
     */
    public void setVin(String vin) {
        this.vin = vin;
    }

    /**
     * Set vehicle id.
     *
     * @param vehicleId string
     */
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * Set created on.
     *
     * @param createdOn date
     */
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * Set updated on.
     *
     * @param updatedOn date
     */
    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    /**
     * Set production date.
     *
     * @param productionDate date
     */
    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    /**
     * Set vehicle attributes.
     *
     * @param vehicleAttributes vehicle attributes
     */
    public void setVehicleAttributes(VehicleAttributes vehicleAttributes) {
        this.vehicleAttributes = vehicleAttributes;
    }

    /**
     * Set authorized users.
     *
     * @param authorizedUsers list of authorized users
     */
    public void setAuthorizedUsers(List<User> authorizedUsers) {
        this.authorizedUsers = authorizedUsers;
    }

    /**
     * Set modem info.
     *
     * @param modemInfo modem info
     */
    public void setModemInfo(ModemInfo modemInfo) {
        this.modemInfo = modemInfo;
    }

    /**
     * Set vehicle architecture type.
     *
     * @param vehicleArchType string
     */
    public void setVehicleArchType(String vehicleArchType) {
        this.vehicleArchType = vehicleArchType;
    }

    /**
     * Set ECUS.
     *
     * @param ecus map
     */
    public void setEcus(Map<String, ? extends Ecu> ecus) {
        this.ecus = ecus;
    }

    /**
     * Get isDummy.
     *
     * @return boolean
     */
    public Boolean isDummy() {
        return dummy;
    }

    /**
     * Set dummy.
     *
     * @param dummy boolean
     */
    public void setDummy(Boolean dummy) {
        this.dummy = dummy;
    }

    /**
     * Get events.
     *
     * @return map
     */
    public Map<String, Event> getEvents() {
        return events;
    }

    /**
     * Set events.
     *
     * @param events map
     */
    public void setEvents(Map<String, Event> events) {
        this.events = events;
    }

    /**
     * Get vehicle capabilities.
     *
     * @return vehicle capabilities
     */
    public VehicleCapabilities getVehicleCapabilities() {
        return vehicleCapabilities;
    }

    /**
     * Set vehicle capabilities.
     *
     * @param vehicleCapabilities vehicle capabilities
     */
    public void setVehicleCapabilities(VehicleCapabilities vehicleCapabilities) {
        this.vehicleCapabilities = vehicleCapabilities;
    }

    /**
     * Get sold region.
     *
     * @return string
     */
    public String getSoldRegion() {
        return soldRegion;
    }

    /**
     * Set sold region.
     *
     * @param soldRegion string
     */
    public void setSoldRegion(String soldRegion) {
        this.soldRegion = soldRegion;
    }

    /**
     * Get sale date.
     *
     * @return string
     */
    public String getSaleDate() {
        return saleDate;
    }

    /**
     * Set sale date.
     *
     * @param saleDate string
     */
    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    /**
     * Get last updated time.
     *
     * @return date
     */
    public Boolean getDummy() {
        return dummy;
    }

    /**
     * Get EOL validation in progress.
     *
     * @return boolean
     */
    public Boolean getEolValidationInProgress() {
        return eolValidationInProgress;
    }

    /**
     * Set EOL validation in progress.
     *
     * @param eolValidationInProgress boolean
     */
    public void setEolValidationInProgress(Boolean eolValidationInProgress) {
        this.eolValidationInProgress = eolValidationInProgress;
    }

    /**
     * Get block enrollment.
     *
     * @return boolean
     */
    public Boolean getBlockEnrollment() {
        return blockEnrollment;
    }

    /**
     * Set block enrollment.
     *
     * @param blockEnrollment boolean
     */
    public void setBlockEnrollment(Boolean blockEnrollment) {
        this.blockEnrollment = blockEnrollment;
    }

    /**
     * Get authorized partners.
     *
     * @return map
     */
    public Map<String, ? extends AuthorizedPartner> getAuthorizedPartners() {
        return authorizedPartners;
    }

    /**
     * Set authorized partners.
     *
     * @param authorizedPartners map
     */
    public void setAuthorizedPartners(Map<String, ? extends AuthorizedPartner> authorizedPartners) {
        this.authorizedPartners = authorizedPartners;
    }

    /**
     * Get epid checksum.
     *
     * @return string
     */
    public String getEpiddbChecksum() {
        return epiddbChecksum;
    }

    /**
     * Set epid checksum.
     *
     * @param epiddbChecksum string
     */
    public void setEpiddbChecksum(String epiddbChecksum) {
        this.epiddbChecksum = epiddbChecksum;
    }

    /**
     * Get connected platform.
     *
     * @return string
     */
    public String getConnectedPlatform() {
        return connectedPlatform;
    }

    /**
     * Set connected platform.
     *
     * @param connectedPlatform string
     */
    public void setConnectedPlatform(String connectedPlatform) {
        this.connectedPlatform = connectedPlatform;
    }

    @Override
    public LocalDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    @Override
    public void setLastUpdatedTime(LocalDateTime lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }
}
