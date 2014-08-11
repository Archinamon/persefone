package mobi.anoda.archinamon.kernel.persefone.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import mobi.anoda.archinamon.kernel.persefone.model.ContactModel;

import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.CommonDataKinds.Im;
import static android.provider.ContactsContract.CommonDataKinds.Note;
import static android.provider.ContactsContract.CommonDataKinds.Organization;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import static android.provider.ContactsContract.Contacts;
import static android.provider.ContactsContract.Data;

/**
 * author: Archinamon
 * project: FavorMe
 */
public final class ContactsProvider {

    private static ContactsProvider   INSTANCE;
    private        Context            mContext;
    private        List<ContactModel> mModels;
    private        AtomicBoolean      mReadStatus;

    public static ContactsProvider getInstance(Context c) {
        if (INSTANCE != null) {
            return INSTANCE;
        }

        INSTANCE = new ContactsProvider();
        INSTANCE.mContext = c;
        INSTANCE.mModels = new ArrayList<>();
        INSTANCE.mReadStatus = new AtomicBoolean(Boolean.FALSE);

        return INSTANCE;
    }

    private ContactsProvider() {
    }

    public List<ContactModel> getContacts() {
        return this.mModels;
    }

    public final synchronized void refresh() {
        mReadStatus.compareAndSet(Boolean.TRUE, Boolean.FALSE);
    }

    public final synchronized Boolean getStatus() {
        return !mReadStatus.get();
    }

    public void readContacts() throws IllegalAccessException {
        final Uri uri = Contacts.CONTENT_URI;
        ContentResolver cr = mContext.getContentResolver();

        if (uri != null) {
            Cursor cur = cr.query(uri, null, null, null, null);

            if (cur != null && cur.getCount() > 0) {
                if (mReadStatus.get()) {
                    throw new IllegalAccessException("Need to call refresh() first");
                }

                this.mModels.clear();
                while (cur.moveToNext()) {
                    if (Integer.parseInt(cur.getString(cur.getColumnIndex(Contacts.HAS_PHONE_NUMBER))) > 0) {
                        String id = cur.getString(cur.getColumnIndex(Contacts._ID));
                        String name = cur.getString(cur.getColumnIndex(Contacts.DISPLAY_NAME));
                        String photo = cur.getString(cur.getColumnIndex(Contacts.PHOTO_THUMBNAIL_URI));

                        obtainModels(name, photo, cr, id);
                    }
                }

                mReadStatus.set(Boolean.TRUE);
                cur.close();
            }
        }
    }

    private void obtainModels(String name, String photoUri, ContentResolver cr, String id) {
        final Uri uri = Phone.CONTENT_URI;

        if (uri != null) {
            Cursor pCur = cr.query(uri, null, Phone.CONTACT_ID + " = ?", new String[] {id}, null);
            while (pCur != null && pCur.moveToNext()) {
                ContactModel model = ContactModel.newModel();
                model.setName(name);
                model.setPhotoUri(photoUri);

                obtainPhones(model, cr, id);
                obtainEmails(model, cr, id);

                this.mModels.add(model);
            }

            if (pCur != null) {
                pCur.close();
            }
        }
    }

    private void obtainPhones(ContactModel model, ContentResolver cr, String id) {
        List<String> phones = model.getPhones();

        final Uri uri = Phone.CONTENT_URI;
        if (uri != null) {
            Cursor pCur = cr.query(uri, null, Phone.CONTACT_ID + " = ?", new String[] {id}, null);
            while (pCur != null && pCur.moveToNext()) {
                String phone = pCur.getString(pCur.getColumnIndex(Phone.NUMBER));
                if (!phones.contains(phone)) {
                    phones.add(phone);
                }
            }

            if (pCur != null) {
                pCur.close();
            }
        }
    }

    private void obtainEmails(ContactModel model, ContentResolver cr, String id) {
        List<String> emails = model.getEmails();

        final Uri uri = Email.CONTENT_URI;
        if (uri != null) {
            Cursor emailCur = cr.query(uri, null, Email.CONTACT_ID + " = ?", new String[] {id}, null);
            while (emailCur != null && emailCur.moveToNext()) {
                // This would allow you get several email addresses
                // if the email addresses were stored in an array
                String email = emailCur.getString(emailCur.getColumnIndex(Email.DATA));
                if (!emails.contains(email)) {
                    emails.add(email);
                }
            }

            if (emailCur != null) {
                emailCur.close();
            }
        }
    }

    private void obtainNotes(ContentResolver cr, String id) {
        String noteWhere = Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?";
        String[] noteWhereParams = new String[] {id, Note.CONTENT_ITEM_TYPE};
        Cursor noteCur = cr.query(Data.CONTENT_URI, null, noteWhere, noteWhereParams, null);
        if (noteCur.moveToFirst()) {
            String note = noteCur.getString(noteCur.getColumnIndex(Note.NOTE));
            System.out
                  .println("Note " + note);
        }
        noteCur.close();
    }

    private void obtainPostalAddress(ContentResolver cr, String id) {
        String addrWhere = Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?";
        String[] addrWhereParams = new String[] {id, StructuredPostal.CONTENT_ITEM_TYPE};
        Cursor addrCur = cr.query(Data.CONTENT_URI, null, null, null, null);
        while (addrCur.moveToNext()) {
            String poBox = addrCur.getString(addrCur.getColumnIndex(StructuredPostal.POBOX));
            String street = addrCur.getString(addrCur.getColumnIndex(StructuredPostal.STREET));
            String city = addrCur.getString(addrCur.getColumnIndex(StructuredPostal.CITY));
            String state = addrCur.getString(addrCur.getColumnIndex(StructuredPostal.REGION));
            String postalCode = addrCur.getString(addrCur.getColumnIndex(StructuredPostal.POSTCODE));
            String country = addrCur.getString(addrCur.getColumnIndex(StructuredPostal.COUNTRY));
            String type = addrCur.getString(addrCur.getColumnIndex(StructuredPostal.TYPE));

            // Do something with these....
        }
        addrCur.close();
    }

    private void obtainIMData(ContentResolver cr, String id) {
        String imWhere = Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?";
        String[] imWhereParams = new String[] {id, Im.CONTENT_ITEM_TYPE};
        Cursor imCur = cr.query(Data.CONTENT_URI, null, imWhere, imWhereParams, null);
        if (imCur.moveToFirst()) {
            String imName = imCur.getString(imCur.getColumnIndex(Im.DATA));
            String imType;
            imType = imCur.getString(imCur.getColumnIndex(Im.TYPE));
        }
        imCur.close();
    }

    private void obtainOrganizations(ContentResolver cr, String id) {
        String orgWhere = Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?";
        String[] orgWhereParams = new String[] {id, Organization.CONTENT_ITEM_TYPE};
        Cursor orgCur = cr.query(Data.CONTENT_URI, null, orgWhere, orgWhereParams, null);
        if (orgCur.moveToFirst()) {
            String orgName = orgCur.getString(orgCur.getColumnIndex(Organization.DATA));
            String title = orgCur.getString(orgCur.getColumnIndex(Organization.TITLE));
        }
        orgCur.close();
    }
}
