package suncertify.server;

import java.rmi.Remote;

/**
 *  This interface is the primary API through which clients may access a
 *  remote database server.
 *
 *@author     Gregory Biegel
 *@version    1.0
 *@see        suncertify.server.Services
 */
public interface RemoteServices extends Remote, Services {

}
