package mobi.anoda.archcore.persefone.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import mobi.anoda.archcore.persefone.annotation.Implement;
import mobi.anoda.archcore.persefone.utils.ListUtils;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public class ContactModel extends ModelPropagator implements Comparator<ContactModel> {

    public static final Creator CREATOR = new Creator() {

        @Implement
        public ContactModel createFromParcel(Parcel source) {
            ContactModel model = ContactModel.newModel();
            model.mName = source.readString();
            model.mPhotoUri = source.readString();
            source.readStringList(model.mEmails);
            source.readStringList(model.mPhones);

            return model;
        }

        @Implement
        public ContactModel[] newArray(int size) {
            return new ContactModel[size];
        }
    };
    private String       mName;
    private String       mPhotoUri;
    private List<String> mEmails = new ArrayList<>();
    private List<String> mPhones = new ArrayList<>();

    public static ContactModel newModel() {
        return new ContactModel(null);
    }

    private ContactModel(ImmutableMap map) {
        super(map);
    }

    @Implement
    public boolean isNull() {
        return mName == null || mName.isEmpty();
    }

    // PUBLIC SETTERS

    public void setName(String name) {
        this.mName = name;
    }

    public void setPhotoUri(String uri) {
        this.mPhotoUri = uri;
    }

    // PUBLIC GETTERS

    public String getName() {
        return this.mName;
    }

    public String getPhotoUri() {
        return this.mPhotoUri;
    }

    public List<String> getEmails() {
        return this.mEmails;
    }

    public List<String> getPhones() {
        return this.mPhones;
    }

    // PARCEL & DB CONNECTION

    @Implement
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeString(this.mPhotoUri);
        dest.writeStringList(this.mEmails);
        dest.writeStringList(this.mPhones);
    }

    @Implement
    public boolean updateDB(Context c) {
        return false;
    }

    @Implement
    public int compare(ContactModel o1, ContactModel o2) {
        final String nameLeft = o1.mName;
        final String nameRight = o2.mName;

        final boolean equal = nameLeft.equalsIgnoreCase(nameRight);
        if (equal) {
            if (!ListUtils.isEmpty(o2.mPhones)) {
                o1.mPhones.addAll(o2.mPhones);
            }

            if (!ListUtils.isEmpty(o2.mEmails)) {
                o1.mEmails.addAll(o2.mEmails);
            }

            return 0;
        } else {
            return nameLeft.compareTo(nameRight);
        }
    }
}
