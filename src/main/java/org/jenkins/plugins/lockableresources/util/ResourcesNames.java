package org.jenkins.plugins.lockableresources.util;

import java.util.List;

public class ResourcesNames {

    public static String joinResourceNames(List<String> resourceNames) {
        StringBuilder resourceName = new StringBuilder();
        for (String res : resourceNames) {
            resourceName.append((resourceName.length() == 0) ? res : ", " + res);
        }
        return resourceName.toString();
    }

}
