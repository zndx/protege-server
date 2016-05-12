package org.protege.editor.owl.server.versioning;

import java.io.Serializable;

import edu.stanford.protege.metaproject.api.Host;

public class ServerDocument implements Serializable {

    private static final long serialVersionUID = 8705283686868339846L;

    private Host host;
    private HistoryFile historyFile;

    public ServerDocument(Host host, HistoryFile historyFile) {
        this.host = host;
        this.historyFile = historyFile;
    }

    public Host getHost() {
        return host;
    }

    public HistoryFile getHistoryFile() {
        return historyFile;
    }
}
