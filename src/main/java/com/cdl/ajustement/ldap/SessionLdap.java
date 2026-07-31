package com.cdl.ajustement.ldap;

public class SessionLdap {

	private boolean isLogged;
	private String logError;
	private String employeeID;
	private String sAMAccountName;
	private String displayName;
	private String telephoneNumber;
	private String email;

	public SessionLdap() {

	}

	public boolean isLogged() {
		return isLogged;
	}

	public void setLogged(boolean isLogged) {
		this.isLogged = isLogged;
	}

	public String getLogError() {
		return logError;
	}

	public void setLogError(String logError) {
		this.logError = logError;
	}

	public String getEmployeeID() {
		return employeeID;
	}

	public void setEmployeeID(String employeeID) {
		this.employeeID = employeeID;
	}

	public String getsAMAccountName() {
		return sAMAccountName;
	}

	public void setsAMAccountName(String sAMAccountName) {
		this.sAMAccountName = sAMAccountName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "SessionLdap [isLogged=" + isLogged + ", logError=" + logError + ", employeeID=" + employeeID + ", sAMAccountName="
				+ sAMAccountName + ", displayName=" + displayName + ", telephoneNumber=" + telephoneNumber + ", email=" + email + "]";
	}

}
