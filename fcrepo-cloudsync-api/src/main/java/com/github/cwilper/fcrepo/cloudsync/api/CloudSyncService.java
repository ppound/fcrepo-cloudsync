package com.github.cwilper.fcrepo.cloudsync.api;

import java.io.InputStream;
import java.util.List;

public interface CloudSyncService {

    // -----------------------------------------------------------------------
    //                               Service
    // -----------------------------------------------------------------------

    ServiceInfo getServiceInfo();

    ServiceInfo initialize(ServiceInit serviceInit) throws AlreadyInitializedException;

    // -----------------------------------------------------------------------
    //                                Users
    // -----------------------------------------------------------------------

    User createUser(User user) throws NameConflictException;

    List<User> listUsers();

    User getUser(String id) throws ResourceNotFoundException;

    User getCurrentUser();

    User updateUser(String id, User user) throws ResourceNotFoundException, NameConflictException;

    void deleteUser(String id) throws ResourceInUseException;

    // -----------------------------------------------------------------------
    //                                Tasks
    // -----------------------------------------------------------------------

    Task createTask(Task task) throws NameConflictException;

    List<Task> listTasks();

    Task getTask(String id) throws ResourceNotFoundException;

    Task updateTask(String id, Task task) throws ResourceNotFoundException, NameConflictException;

    void deleteTask(String id) throws ResourceInUseException;

    // -----------------------------------------------------------------------
    //                             Object Sets
    // -----------------------------------------------------------------------

    ObjectSet createObjectSet(ObjectSet objectSet) throws NameConflictException;

    List<ObjectSet> listObjectSets();

    ObjectSet getObjectSet(String id) throws ResourceNotFoundException;

    void deleteObjectSet(String id) throws ResourceInUseException;

    // -----------------------------------------------------------------------
    //                            Object Stores
    // -----------------------------------------------------------------------

    ObjectStore createObjectStore(ObjectStore objectStore) throws NameConflictException;

    List<ObjectStore> listObjectStores();

    ObjectStore getObjectStore(String id) throws ResourceNotFoundException;

    void deleteObjectStore(String id) throws ResourceInUseException;

    // -----------------------------------------------------------------------
    //                              Task Logs
    // -----------------------------------------------------------------------

    List<TaskLog> listTaskLogs();

    TaskLog getTaskLog(String id) throws ResourceNotFoundException;

    InputStream getTaskLogContent(String id) throws ResourceNotFoundException;

    void deleteTaskLog(String id) throws ResourceInUseException;

    // -----------------------------------------------------------------------
    //                             DuraCloud
    // -----------------------------------------------------------------------

    List<ProviderAccount> listProviderAccounts(String url,
                                               String username,
                                               String password);

    List<Space> listSpaces(String url,
                           String username,
                           String password,
                           String providerAccountId);

}
