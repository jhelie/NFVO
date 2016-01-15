/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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

package org.openbaton.nfvo.vnfm_reg.tasks;

import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
@ConfigurationProperties(prefix = "nfvo.start")
public class ModifyTask extends AbstractTask {

    @Autowired
    private VnfmRegister vnfmRegister;

    private String ordered;

    @Override
    protected NFVMessage doWork() throws Exception {
        virtualNetworkFunctionRecord.setStatus(Status.INACTIVE);
        log.info("MODIFY finished for vnfr: " + virtualNetworkFunctionRecord.getName());
        log.trace("VNFR Verison is: " + virtualNetworkFunctionRecord.getHb_version());
        saveVirtualNetworkFunctionRecord();
        log.trace("Now VNFR Verison is: " + virtualNetworkFunctionRecord.getHb_version());
        log.debug("VNFR " + virtualNetworkFunctionRecord.getName() + " Status is: " + virtualNetworkFunctionRecord.getStatus());
        boolean allVnfrInInactive = allVnfrInInactive(networkServiceRecordRepository.findFirstById(virtualNetworkFunctionRecord.getParent_ns_id()));
        log.trace("Ordered string is: \"" + ordered + "\"");
        log.debug("Is ordered? " + Boolean.parseBoolean(ordered));
        log.debug("Are all VNFR in inactive? " + allVnfrInInactive);

        if (ordered != null && Boolean.parseBoolean(ordered)) {
            if (allVnfrInInactive) {
                VirtualNetworkFunctionRecord nextToCallStart = getNextToCallStart(virtualNetworkFunctionRecord);
                if (nextToCallStart != null) {
                    log.debug("Calling start to vnfr: " + nextToCallStart.getName());
                    vnfmManager.getVnfrNames().get(virtualNetworkFunctionRecord.getParent_ns_id()).remove(nextToCallStart.getName());
                    sendStart(nextToCallStart);
                }
            } else {
                log.debug("Not calling start to next VNFR because not all VNFRs are in state INACTIVE");
            }
        } else {
            sendStart(virtualNetworkFunctionRecord);
        }
        return null;
    }

    private void sendStart(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws NotFoundException {
        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());
        vnfmSender.sendCommand(new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.START), vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    public void setOrdered(String ordered) {
        this.ordered = ordered;
    }
}
