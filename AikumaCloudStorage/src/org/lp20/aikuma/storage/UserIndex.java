package org.lp20.aikuma.storage;

import java.util.Enumeration;

/**
 * UserIndex allows searching users by given term.
 *
 * For instance, suppose there were groups of files, each of which is
 * contributed by different users. A user might want to get notification when
 * there is a new file added to the group. The fact that the user is following
 * that group would be recorded in a database. When a new file is added to the
 * group, the server could use this interface to find users who are following
 * the group.
 *
 * {@code
 * UserIndex idx = new MyUserIndexImpl(dbuser, dbpasswd);
 * Search res = idx.searchUser("following:group123");
 * while (res.hasMoreElements()) {
 *   JSONObject obj = res.nextElement();
 *   process(obj);
 * }
 * }
 */
public interface UserIndex {
    public Enumeration searchUser(String term);
}

