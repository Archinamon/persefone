package mobi.anoda.archinamon.kernel.persefone.model;

import com.google.common.collect.ImmutableList;
import org.apache.http.NameValuePair;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public interface ISignalEvaluable<Elem extends NameValuePair> {

    ISignalEvaluable packModel();

    ImmutableList<Elem> getPackage();
}
