package edu.ualberta.med.biobank.common.action.file;

import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.SimpleResult;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.model.FileData;
import edu.ualberta.med.biobank.model.FileMetaData;
import edu.ualberta.med.biobank.model.type.Hash.SHA1Hash;

public class FileDataGetAction
    implements Action<SimpleResult<FileData>> {
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("nls")
    private static final String QRY = "SELECT data " +
        " FROM " + FileData.class.getName() + " data" +
        " WHERE data.metaData.id = ? AND data.metaData.sha1Hash = ?"; 

    private final Integer metaDataId;
    private final SHA1Hash sha1Hash;

    public FileDataGetAction(FileMetaData metaData) {
        this.metaDataId = metaData.getId();
        this.sha1Hash = metaData.getSha1Hash();
    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        return true;
    }

    @Override
    public SimpleResult<FileData> run(ActionContext context)
        throws ActionException {

        FileData data = (FileData) context.getSession()
            .createQuery(QRY)
            .setParameter(0, metaDataId)
            .setParameter(1, sha1Hash)
            .uniqueResult();

        return new SimpleResult<FileData>(data);
    }
}
