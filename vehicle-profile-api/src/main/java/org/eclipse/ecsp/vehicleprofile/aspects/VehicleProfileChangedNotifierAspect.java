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

package org.eclipse.ecsp.vehicleprofile.aspects;

import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.vehicleprofile.dao.VehicleDao;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.notifier.VehicleProfileChangedNotifier;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * VehicleProfileChangedNotifierAspect class.
 */
@Component
@Aspect
public class VehicleProfileChangedNotifierAspect {

    @Autowired
    private VehicleProfileChangedNotifier notifier;
    @Autowired
    private VehicleDao vehicleDao;
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleProfileChangedNotifier.class);

    @Pointcut("execution(* org.eclipse.ecsp.dao.vehicleprofile.ecsp.VehicleDao.save(..))")
    public void createdPc() {
    }

    @Pointcut("execution(* org.eclipse.ecsp.dao.vehicleprofile.ecsp.VehicleDao.update(..))")
    public void updatedPc() {
    }

    @Pointcut("execution(* org.eclipse.ecsp.dao.vehicleprofile.ecsp.VehicleDao.merge(..))")
    public void mergePc() {
    }

    @Pointcut("execution(* org.eclipse.ecsp.dao.vehicleprofile.ecsp.VehicleDao.getAndUpdate(..))")
    public void getAndUpdatePc() {
    }

    @Pointcut("execution(* org.eclipse.ecsp.dao.vehicleprofile.ecsp.VehicleDao.associate(..))")
    public void associatePc() {
    }

    @Pointcut("execution(* org.eclipse.ecsp.dao.vehicleprofile.ecsp.VehicleDao.disassociate(..))")
    public void disassociatePc() {
    }

    @Pointcut("execution(* org.eclipse.ecsp.dao.vehicleprofile.ecsp.VehicleDao.updateAssociation(..))")
    public void updateAssociationPc() {

    }

    @AfterReturning(value = "org.eclipse.ecsp.aspects.vehicleprofile.ecsp.VehicleProfileChangedNotifierAspect"
            + ".createdPc()", returning = "vehicleProfile")
    public void notifyVehicleCreated(VehicleProfile vehicleProfile) {
        LOGGER.debug("New vehicle record created... notifying it.. {}", vehicleProfile);
        notifier.changed(null, vehicleProfile, MDC.get(Constants.REQUEST_ID));
    }

    /**
     * notifyVehicleUpdate.
     */
    @Around(value = "org.eclipse.ecsp.aspects.vehicleprofile.ecsp.VehicleProfileChangedNotifierAspect"
            + ".updatedPc() && args(vehicleProfile)")
    public boolean notifyVehicleUpdate(ProceedingJoinPoint proceedingJoinPoint, VehicleProfile vehicleProfile)
            throws Throwable {
        VehicleProfile oldVehicleProfile = vehicleDao.findById(vehicleProfile.getVehicleId());
        LOGGER.debug("existing vehicleProfile in the system {}", oldVehicleProfile);
        LOGGER.debug("modified vehicleProfile {}", vehicleProfile);
        boolean updateStatus = (boolean) proceedingJoinPoint.proceed();
        notifier.changed(oldVehicleProfile, vehicleProfile, MDC.get(Constants.REQUEST_ID));
        return updateStatus;
    }


    @Around(value = "org.eclipse.ecsp.aspects.vehicleprofile.ecsp.VehicleProfileChangedNotifierAspect"
            + ".updatedPc() && args(id, ..)")
    public boolean notifyVehicleUpdate(ProceedingJoinPoint proceedingJoinPoint, String id)
            throws Throwable {
        return notifyVehicleUpdateOldAndNewDataUnknown(proceedingJoinPoint, id);
    }
    
    @Around(value = "org.eclipse.ecsp.aspects.vehicleprofile.ecsp.VehicleProfileChangedNotifierAspect"
            + ".mergePc() && args(vehicleProfile)")
    public boolean merge(ProceedingJoinPoint proceedingJoinPoint, VehicleProfile vehicleProfile) throws Throwable {
        return notifyVehicleUpdateOldAndNewDataUnknown(proceedingJoinPoint, vehicleProfile.getVehicleId());
    }

    @Around(value = "org.eclipse.ecsp.aspects.vehicleprofile.ecsp.VehicleProfileChangedNotifierAspect"
            + ".getAndUpdatePc() && args(vehicleProfile)")
    public boolean getAndUpdate(ProceedingJoinPoint proceedingJoinPoint, VehicleProfile vehicleProfile)
            throws Throwable {
        LOGGER.debug("getAndUpdate pointcut");
        return notifyVehicleUpdateOldAndNewDataUnknown(proceedingJoinPoint, vehicleProfile.getVehicleId());
    }

    @Around(value = "org.eclipse.ecsp.aspects.vehicleprofile.ecsp.VehicleProfileChangedNotifierAspect"
            + ".associatePc() && args(vehicleId, ..)")
    public boolean associate(ProceedingJoinPoint proceedingJoinPoint, String vehicleId) throws Throwable {
        return notifyVehicleUpdateOldAndNewDataUnknown(proceedingJoinPoint, vehicleId);
    }

    @Around(value = "org.eclipse.ecsp.aspects.vehicleprofile.ecsp.VehicleProfileChangedNotifierAspect"
            + ".disassociatePc() && args(vehicleId, ..)")
    public boolean disassociate(ProceedingJoinPoint proceedingJoinPoint, String vehicleId) throws Throwable {
        return notifyVehicleUpdateOldAndNewDataUnknown(proceedingJoinPoint, vehicleId);
    }

    @Around(value = "org.eclipse.ecsp.aspects.vehicleprofile.ecsp.VehicleProfileChangedNotifierAspect"
            + ".updateAssociationPc() && args(vehicleId, ..)")
    public boolean updateAssociation(ProceedingJoinPoint proceedingJoinPoint, String vehicleId) throws Throwable {
        return notifyVehicleUpdateOldAndNewDataUnknown(proceedingJoinPoint, vehicleId);
    }

    private boolean notifyVehicleUpdateOldAndNewDataUnknown(ProceedingJoinPoint proceedingJoinPoint, String vehicleId)
            throws Throwable {
        VehicleProfile oldVehicleProfile = vehicleDao.findById(vehicleId);
        boolean status = (boolean) proceedingJoinPoint.proceed();
        VehicleProfile changedVehicleProfile = vehicleDao.findById(vehicleId);
        LOGGER.debug("vehicle profile being modified old {} - new {}", oldVehicleProfile,
                changedVehicleProfile);
        notifier.changed(oldVehicleProfile, changedVehicleProfile, MDC.get(Constants.REQUEST_ID));
        return status;
    }

}