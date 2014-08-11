package mobi.anoda.archinamon.kernel.persefone.upload;

import java.lang.String;
import mobi.anoda.archinamon.kernel.persefone.upload.Request;

interface IUploadService {

    void addCookie(in String name, in String value, in String domain, in String path);

    oneway void upload(in Request task);
}
