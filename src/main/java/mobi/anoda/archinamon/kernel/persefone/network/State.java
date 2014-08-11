package mobi.anoda.archinamon.kernel.persefone.network;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public enum State {

    ACCESS_UNKNOWN,
    ACCESS_GRANTED,
    ACCESS_DENIED;

    //default value;
    public static volatile transient State svAccessState = ACCESS_UNKNOWN;
}
