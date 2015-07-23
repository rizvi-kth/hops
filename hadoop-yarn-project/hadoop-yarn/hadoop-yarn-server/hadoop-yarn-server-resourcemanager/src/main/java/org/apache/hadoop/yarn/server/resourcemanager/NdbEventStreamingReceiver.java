/*
 * Copyright 2015 Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.yarn.server.resourcemanager;

import io.hops.metadata.yarn.entity.ContainerId;
import io.hops.metadata.yarn.entity.ContainerStatus;
import io.hops.metadata.yarn.entity.FinishedApplications;
import io.hops.metadata.yarn.entity.JustLaunchedContainers;
import io.hops.metadata.yarn.entity.NextHeartbeat;
import io.hops.metadata.yarn.entity.Node;
import io.hops.metadata.yarn.entity.NodeHBResponse;
import io.hops.metadata.yarn.entity.PendingEvent;
import io.hops.metadata.yarn.entity.RMNode;
import io.hops.metadata.yarn.entity.RMNodeComps;
import io.hops.metadata.yarn.entity.Resource;
import io.hops.metadata.yarn.entity.UpdatedContainerInfo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author sri
 */
public class NdbEventStreamingReceiver {

    //we neee to move this to configuration .
    private static final int experimentCapacity = 100000;

    public static BlockingQueue blockingQueue = new ArrayBlockingQueue(experimentCapacity);
    private static final Log LOG = LogFactory.getLog(NdbEventStreamingReceiver.class);

    private PendingEvent hopPendingEvent = null;
    private RMNode hopRMNode = null;
    private NextHeartbeat hopNextHeartbeat = null;
    private Node hopNode = null;
    private NodeHBResponse hopNodeHBResponse = null;
    private Resource hopResource = null;

    private List<JustLaunchedContainers> hopJustLaunchedContainersList = null;
    private List<UpdatedContainerInfo> hopUpdatedContainerInfoList = null;
    private List<ContainerId> hopContainerIdsToCleanList = null;
    private List<FinishedApplications> hopFinishedApplicationsList = null;
    private List<ContainerStatus> hopContainersStatusList = null;

    NdbEventStreamingReceiver() {
    }

    // building hoprmnode///////////////////////////
    private String hopRMNodeNodeId="";
    private String hopRMNodeHostName="";
    private int hopRMNodeCommandPort=0;
    private int hopRMNodeHttpPort=0;
    private String hopRMNodeNodeAddress="";
    private String hopRMNodeHttpAddress="";
    private String hopRMNodeHealthReport="";
    private long hopRMNodelastHealthReportTime=0;
    private String hopRMNodeCurrentState="";
    private String hopRMNodeNodemanagerVersion="";
    private int hopRMNodeOvercommittimeout=0;
    private int hopRMNodeUciId=0;

    public void setHopRMNodeNodeId(String hopRMNodeNodeId) {
        this.hopRMNodeNodeId = hopRMNodeNodeId;
    }

    public void setHopRMNodeHostName(String hopRMNodeHostName) {
        this.hopRMNodeHostName = hopRMNodeHostName;
    }

    public void setHopRMNodeCommandPort(int hopRMNodeCommandPort) {
        this.hopRMNodeCommandPort = hopRMNodeCommandPort;
    }

    public void setHopRMNodeHttpPort(int hopRMNodeHttpPort) {
        this.hopRMNodeHttpPort = hopRMNodeHttpPort;
    }

    public void setHopRMNodeNodeAddress(String hopRMNodeNodeAddress) {
        this.hopRMNodeNodeAddress = hopRMNodeNodeAddress;
    }

    public void setHopRMNodeHttpAddress(String hopRMNodeHttpAddress) {
        this.hopRMNodeHttpAddress = hopRMNodeHttpAddress;
    }

    public void setHopRMNodeHealthReport(String hopRMNodeHealthReport) {
        this.hopRMNodeHealthReport = hopRMNodeHealthReport;
    }

    public void setHopRMNodelastHealthReportTime(long hopRMNodelastHealthReportTime) {
        this.hopRMNodelastHealthReportTime = hopRMNodelastHealthReportTime;
    }

    public void setHopRMNodeCurrentState(String hopRMNodeCurrentState) {
        this.hopRMNodeCurrentState = hopRMNodeCurrentState;
    }

    public void setHopRMNodeNodemanagerVersion(String hopRMNodeNodemanagerVersion) {
        this.hopRMNodeNodemanagerVersion = hopRMNodeNodemanagerVersion;
    }

    public void setHopRMNodeOvercommittimeout(int hopRMNodeOvercommittimeout) {
        this.hopRMNodeOvercommittimeout = hopRMNodeOvercommittimeout;
    }

    public void setHopRMNodeUciId(int hopRMNodeUciId) {
        this.hopRMNodeUciId = hopRMNodeUciId;
    }

    public void buildHopRMNode() {
        hopRMNode = new RMNode(hopRMNodeNodeId, hopRMNodeHostName, hopRMNodeCommandPort, hopRMNodeHttpPort, hopRMNodeNodeAddress, hopRMNodeHttpAddress, hopRMNodeHealthReport, hopRMNodelastHealthReportTime, hopRMNodeCurrentState, hopRMNodeNodemanagerVersion, hopRMNodeOvercommittimeout, hopRMNodeUciId);

    }

    //build hoppending event/////////////////////////
    private String hopPendingEventRmnodeId="";
    private int hopPendingEventType=0;
    private int hopPendingEventStatus=0;
    //Used to order the events when retrieved by scheduler
    private int hopPendingEventId=0;
    private int lastHBResponseID=0;

    public void setHopPendingEventRmnodeId(String hopPendingEventRmnodeId) {
        this.hopPendingEventRmnodeId = hopPendingEventRmnodeId;
    }

    public void setHopPendingEventType(int hopPendingEventType) {
        this.hopPendingEventType = hopPendingEventType;
    }

    public void setHopPendingEventStatus(int hopPendingEventStatus) {
        this.hopPendingEventStatus = hopPendingEventStatus;
    }

    public void setHopPendingEventId(int hopPendingEventId) {
        this.hopPendingEventId = hopPendingEventId;
    }

    public void setHopPendingEventHbId(int hopPendingEventHbId) {
        this.lastHBResponseID = hopPendingEventHbId;
    }

    public void buildHopPendingEvent() {
        hopPendingEvent = new PendingEvent(hopPendingEventRmnodeId, hopPendingEventType, hopPendingEventStatus, hopPendingEventId);
    }

    /// build hopnext heart beat ///////////////////////////////////////////////////////
    private String hopNextHBRmnodeid="";
    private boolean hopNextHBNextheartbeat=true;

    public void setHopNextHBRmnodeid(String hopNextHBRmnodeid) {
        this.hopNextHBRmnodeid = hopNextHBRmnodeid;
    }

    public void setHopNextHBNextheartbeat(boolean hopNextHBNextheartbeat) {
        this.hopNextHBNextheartbeat = hopNextHBNextheartbeat;
    }

    public void buildHopNextHeartBeat() {
        hopNextHeartbeat = new NextHeartbeat(hopNextHBRmnodeid, hopNextHBNextheartbeat);
    }

    /////build heartbeat response //////////////////////////////////////////////////////
    private String hopHBResponseRmnodeId="";
    private byte[] hopHBResponse;

    public void setHopHBResponseRmnodeId(String hopHBResponseRmnodeId) {
        this.hopHBResponseRmnodeId = hopHBResponseRmnodeId;
    }

    public void setHopHBResponse(byte[] hopHBResponse) throws DataFormatException, IOException {
        if (hopHBResponse != null) {
            Inflater decompresser = new Inflater();
            decompresser.setInput(hopHBResponse);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(hopHBResponse.length);
            byte[] buffer = new byte[1024];
            while (!decompresser.finished()) {
                int count = decompresser.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            decompresser.end();
            outputStream.close();
            this.hopHBResponse = outputStream.toByteArray();
        } else {
            this.hopHBResponse = hopHBResponse;
        }

    }

    public void buildHopHBResponse() {
        hopNodeHBResponse = new NodeHBResponse(hopHBResponseRmnodeId, hopHBResponse);
    }

    /////build hop resource ////////////////////////////////////////////////////////////////
    private String hopResourceId="";
    private int hopResourceType=0;
    private int hopResourceParent=0;
    private int hopResourceMemory=0;
    private int hopResourceVirtualcores=0;

    public void setHopResourceId(String hopResourceId) {
        this.hopResourceId = hopResourceId;
    }

    public void setHopResourceType(int hopResourceType) {
        this.hopResourceType = hopResourceType;
    }

    public void setHopResourceParent(int hopResourceParent) {
        this.hopResourceParent = hopResourceParent;
    }

    public void setHopResourceMemory(int hopResourceMemory) {
        this.hopResourceMemory = hopResourceMemory;
    }

    public void setHopResourceVirtualcores(int hopResourceVirtualcores) {
        this.hopResourceVirtualcores = hopResourceVirtualcores;
    }

    public void buildHopResource() {
        hopResource = new Resource(hopResourceId, hopResourceType, hopResourceParent, hopResourceMemory, hopResourceVirtualcores);
    }

    ///build hopnode ///////////////////////////////////////////////////////
    private String hopNodeId="";
    private String hopNodeName="";
    private String hopNodeLocation="";
    private int hopNodeLevel=0;
    private String hopNodeParent="";

    public void setHopNodeId(String hopNodeId) {
        this.hopNodeId = hopNodeId;
    }

    public void setHopNodeName(String hopNodeName) {
        this.hopNodeName = hopNodeName;
    }

    public void setHopNodeLocation(String hopNodeLocation) {
        this.hopNodeLocation = hopNodeLocation;
    }

    public void setHopNodeLevel(int hopNodeLevel) {
        this.hopNodeLevel = hopNodeLevel;
    }

    public void setHopNodeParent(String hopNodeParent) {
        this.hopNodeParent = hopNodeParent;
    }

    public void buildHopNode() {
        hopNode = new Node(hopNodeId, hopNodeName, hopNodeLocation, hopNodeLevel, hopNodeParent);
    }

    ///list processing - just launched containers ///////////////////////////////////////
    public void buildHopJustLaunchedContainers() {
        hopJustLaunchedContainersList = new ArrayList<JustLaunchedContainers>();

    }

    private String hopJustLaunchedContainersRmnodeid="";
    private String hopJustLaunchedContainersContainerid="";

    public void setHopJustLaunchedContainersRmnodeid(String hopJustLaunchedContainersRmnodeid) {
        this.hopJustLaunchedContainersRmnodeid = hopJustLaunchedContainersRmnodeid;
    }

    public void setHopJustLaunchedContainersContainerid(String hopJustLaunchedContainersContainerid) {
        this.hopJustLaunchedContainersContainerid = hopJustLaunchedContainersContainerid;
    }

    public void AddJustLaunchedContainers() {
        JustLaunchedContainers hopJustLaunchedContainers = new JustLaunchedContainers(hopJustLaunchedContainersRmnodeid, hopJustLaunchedContainersContainerid);
        hopJustLaunchedContainersList.add(hopJustLaunchedContainers);
    }

    //// list building - hopupdatedcontainerinfo
    public void buildHopUpdatedContainerInfo() {
        hopUpdatedContainerInfoList = new ArrayList<UpdatedContainerInfo>();
    }

    private String hopUpdatedContainerInfoRmnodeid="";
    private String hopUpdatedContainerInfoContainerId="";
    private int hopUpdatedContainerInfoUpdatedContainerInfoId=0;

    public void setHopUpdatedContainerInfoRmnodeid(String hopUpdatedContainerInfoRmnodeid) {
        this.hopUpdatedContainerInfoRmnodeid = hopUpdatedContainerInfoRmnodeid;
    }

    public void setHopUpdatedContainerInfoContainerId(String hopUpdatedContainerInfoContainerId) {
        this.hopUpdatedContainerInfoContainerId = hopUpdatedContainerInfoContainerId;
    }

    public void setHopUpdatedContainerInfoUpdatedContainerInfoId(int hopUpdatedContainerInfoUpdatedContainerInfoId) {
        this.hopUpdatedContainerInfoUpdatedContainerInfoId = hopUpdatedContainerInfoUpdatedContainerInfoId;
    }

    public void AddHopUpdatedContainerInfo() {
        UpdatedContainerInfo hopUpdatedContainerInfo = new UpdatedContainerInfo(hopUpdatedContainerInfoRmnodeid, hopUpdatedContainerInfoContainerId, hopUpdatedContainerInfoUpdatedContainerInfoId);
        hopUpdatedContainerInfoList.add(hopUpdatedContainerInfo);
    }

    //// list building - build containeridtoclean
    public void buildHopContainerId() {
        hopContainerIdsToCleanList = new ArrayList<ContainerId>();
    }

    private String hopContainerIdToCleanRmnodeid="";
    private String hopContainerIdToCleanContainerId="";

    public void setHopContainerIdToCleanRmnodeid(String hopContainerIdToCleanRmnodeid) {
        this.hopContainerIdToCleanRmnodeid = hopContainerIdToCleanRmnodeid;
    }

    public void setHopContainerIdToCleanContainerId(String hopContainerIdToCleanContainerId) {
        this.hopContainerIdToCleanContainerId = hopContainerIdToCleanContainerId;
    }

    public void AddHopContainerIdToClean() {
        ContainerId hopContainerId = new ContainerId(hopContainerIdToCleanRmnodeid, hopContainerIdToCleanContainerId);
        hopContainerIdsToCleanList.add(hopContainerId);
    }

    //// list building - build finished applications
    public void buildHopFinishedApplications() {
        hopFinishedApplicationsList = new ArrayList<FinishedApplications>();

    }

    private String hopFinishedApplicationsRmnodeid="";
    private String hopFinishedApplicationsApplicationId="";

    public void setHopFinishedApplicationsRmnodeid(String hopFinishedApplicationsRmnodeid) {
        this.hopFinishedApplicationsRmnodeid = hopFinishedApplicationsRmnodeid;
    }

    public void setHopFinishedApplicationsApplicationId(String hopFinishedApplicationsApplicationId) {
        this.hopFinishedApplicationsApplicationId = hopFinishedApplicationsApplicationId;
    }

    public void AddHopFinishedApplications() {
        FinishedApplications hopFinishedApplications = new FinishedApplications(hopFinishedApplicationsRmnodeid, hopFinishedApplicationsApplicationId);
        hopFinishedApplicationsList.add(hopFinishedApplications);
    }

    //// list building - build container status
    private String hopContainerStatusContainerid="";
    private String hopContainerStatusState="";
    private String hopContainerStatusDiagnostics="";
    private int hopContainerStatusExitstatus=0;
    private String hopContainerStatusRMNodeId="";

    public void setHopContainerStatusContainerid(String hopContainerStatusContainerid) {
        this.hopContainerStatusContainerid = hopContainerStatusContainerid;
    }

    public void setHopContainerStatusState(String hopContainerStatusState) {
        this.hopContainerStatusState = hopContainerStatusState;
    }

    public void setHopContainerStatusDiagnostics(String hopContainerStatusDiagnostics) {
        this.hopContainerStatusDiagnostics = hopContainerStatusDiagnostics;
    }

    public void setHopContainerStatusExitstatus(int hopContainerStatusExitstatus) {
        this.hopContainerStatusExitstatus = hopContainerStatusExitstatus;
    }

    public void setHopContainerStatusRMNodeId(String hopContainerStatusRMNodeId) {
        this.hopContainerStatusRMNodeId = hopContainerStatusRMNodeId;
    }

    public void buildHopContainerStatus() {
        hopContainersStatusList = new ArrayList<ContainerStatus>();
    }

    public void AddHopContainerStatus() {
        ContainerStatus hopContainerStatus = new ContainerStatus(hopContainerStatusContainerid, hopContainerStatusState,
                hopContainerStatusDiagnostics, hopContainerStatusExitstatus, hopContainerStatusRMNodeId);
        hopContainersStatusList.add(hopContainerStatus);
    }

    //This will be called by c++ shared library, libhopsndbevent.so
    public void onEventMethod() throws InterruptedException {
        RMNodeComps hopRMNodeBDBObject = new RMNodeComps(hopRMNode, hopNextHeartbeat, hopNode, hopNodeHBResponse, hopResource,
                hopPendingEvent, hopJustLaunchedContainersList, hopUpdatedContainerInfoList, hopContainerIdsToCleanList, hopFinishedApplicationsList, hopContainersStatusList);
        blockingQueue.put(hopRMNodeBDBObject);
    }

    // this two methods are using for multi-thread version from c++ library
    RMNodeComps buildCompositeClass() {
        return new RMNodeComps(hopRMNode, hopNextHeartbeat, hopNode, hopNodeHBResponse, hopResource,
                hopPendingEvent, hopJustLaunchedContainersList, hopUpdatedContainerInfoList, hopContainerIdsToCleanList, hopFinishedApplicationsList, hopContainersStatusList);
    }

    public void onEventMethodMultiThread(RMNodeComps hopCompObject) throws InterruptedException {
        blockingQueue.put(hopCompObject);
    }
}
