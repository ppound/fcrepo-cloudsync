package com.github.cwilper.fcrepo.cloudsync.service.backend;

import java.io.PrintWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.cwilper.fcrepo.cloudsync.api.ObjectInfo;
import com.github.cwilper.fcrepo.cloudsync.api.Task;
import com.github.cwilper.fcrepo.cloudsync.service.dao.ObjectSetDao;
import com.github.cwilper.fcrepo.cloudsync.service.dao.ObjectStoreDao;
import com.github.cwilper.fcrepo.cloudsync.service.dao.TaskDao;
import com.github.cwilper.fcrepo.cloudsync.service.rest.URIMapper;
import com.github.cwilper.fcrepo.cloudsync.service.util.JSON;
import com.github.cwilper.fcrepo.httpclient.HttpClientConfig;

public class ListTaskRunner extends TaskRunner implements ObjectListHandler {

    private final String setId;
    private final String storeId;

    private final Set<String> relatedSetIds = new HashSet<String>();
    private final Set<String> relatedStoreIds = new HashSet<String>();

    private final HttpClientConfig httpClientConfig;

    private TaskCanceledException canceledException;

    public ListTaskRunner(Task task,
                          TaskDao taskDao,
                          ObjectSetDao objectSetDao,
                          ObjectStoreDao objectStoreDao,
                          PrintWriter logWriter,
                          TaskCompletionListener completionListener,
                          HttpClientConfig httpClientConfig) {
        super(task, taskDao, objectSetDao, objectStoreDao, logWriter, completionListener);
        Map<String, String> map = JSON.getMap(JSON.parse(task.getData()));
        setId = URIMapper.getId(URI.create(map.get("setUri")));
        storeId = URIMapper.getId(URI.create(map.get("storeUri")));
        relatedSetIds.add(setId);
        relatedStoreIds.add(storeId);
        this.httpClientConfig = httpClientConfig;
    }

    @Override
    public void runTask() throws Exception {
        StoreConnector connector = StoreConnector.getInstance(
                objectStoreDao.getObjectStore(storeId), httpClientConfig);
        try {
            ObjectQuery query = new ObjectQuery(
                    objectSetDao.getObjectSet(setId));
            connector.listObjects(query, this);
            if (canceledException != null) {
                throw canceledException;
            }
        } finally {
            connector.close();
        }
    }

    @Override
    public Set<String> getRelatedSetIds() {
        return relatedSetIds;
    }

    @Override
    public Set<String> getRelatedStoreIds() {
        return relatedStoreIds;
    }

    // ObjectListHandler

    @Override
    public boolean handleObject(ObjectInfo info) {
        logWriter.println(info.getPid());
        try {
            pauseOrCancelIfRequested();
            return true;
        } catch (TaskCanceledException e) {
            canceledException = e;
            return false;
        }
    }

}
