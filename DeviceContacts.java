/*
 * Class:- DeviceContacts - DTO class for Device Contacts.
 */
public class DeviceContacts {

	// Variables of contacts contact's unique Hash, contact date and contact
	// duration
	public String contactHash;
	public int contactDate;
	public int contactDuration;

	/*
	 * DeviceContacts() - Constructor to set device Contacts
	 * 
	 * params - contactHash - unique hash of contacted device, contactDate- contact
	 * date, contactDuration- number of minutes of contact
	 */
	public DeviceContacts(String contactHash, int contactDate, int contactDuration) {
		super();
		this.contactHash = contactHash;
		this.contactDate = contactDate;
		this.contactDuration = contactDuration;
	}

	// auto generated getters and setters

	public String getContactHash() {
		return contactHash;
	}

	public void setContactHash(String contactHash) {
		this.contactHash = contactHash;
	}

	public int getContactDate() {
		return contactDate;
	}

	public void setContactDate(int contactDate) {
		this.contactDate = contactDate;
	}

	public int getContactDuration() {
		return contactDuration;
	}

	public void setContactDuration(int contactDuration) {
		this.contactDuration = contactDuration;
	}

}
