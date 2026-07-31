package com.cdl.ajustement.ldap;


import java.util.Properties;
import javax.naming.CommunicationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;


public class CommonLdap {

	public static SessionLdap connectSession(String userName, String password, String ldapUrl, String domain, String domainComponent) {
		Properties env = null;
		SessionLdap result = new SessionLdap();
		result.setLogged(false);
		String employeeID = "";
		String sAMAccountName = "";
		String displayName = "";
		String telephoneNumber = "";
		String email = "";
		env = new Properties();
		env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
		env.put("java.naming.security.authentication", "simple");
		env.put("java.naming.referral", "follow");
		env.put("java.naming.provider.url", ldapUrl);
		env.put("java.naming.security.principal", domain + "\\" + userName);
		env.put("java.naming.security.credentials", password);
		DirContext ctx = null;
		try {
			ctx = new InitialDirContext(env);
			String[] attrIds = { "employeeID", "sAMAccountName", "displayName", "telephoneNumber", "mail" };
			SearchControls ctls = new SearchControls(SearchControls.SUBTREE_SCOPE, 0, 0, attrIds, false, false);
			String account = userName;
			StringBuffer build = new StringBuffer("&(objectCategory=person)(objectClass=user)");
			build.append("(");
			build.append("sAMAccountName");
			build.append("=");
			build.append(account);
			build.append(")");
			NamingEnumeration<?> answerSearch = ctx.search("dc=" + domain + ",dc=" + domainComponent,
					"(&(objectCategory=person)(objectClass=user)(sAMAccountName=" + userName + "))", ctls);
			if (answerSearch.hasMore()) {
				result.setLogged(true);
				String itemBalise = "";
				Attributes atts = ((SearchResult) answerSearch.next()).getAttributes();
				for (int i = 0; i < attrIds.length; i++) {
					itemBalise = "" + atts.get(attrIds[i]);
					if (itemBalise.indexOf(":") != -1 && itemBalise.substring(0, itemBalise.indexOf(":")).trim().equals("employeeID")) {
						employeeID = itemBalise.substring(itemBalise.indexOf(":") + 1, itemBalise.length()).trim();
					} else if (itemBalise.indexOf(":") != -1
							&& itemBalise.substring(0, itemBalise.indexOf(":")).trim().equals("sAMAccountName")) {
						sAMAccountName = itemBalise.substring(itemBalise.indexOf(":") + 1, itemBalise.length()).trim();
					} else if (itemBalise.indexOf(":") != -1
							&& itemBalise.substring(0, itemBalise.indexOf(":")).trim().equals("displayName")) {
						displayName = itemBalise.substring(itemBalise.indexOf(":") + 1, itemBalise.length()).trim();
					} else if (itemBalise.indexOf(":") != -1
							&& itemBalise.substring(0, itemBalise.indexOf(":")).trim().equals("telephoneNumber")) {
						telephoneNumber = itemBalise.substring(itemBalise.indexOf(":") + 1, itemBalise.length()).trim();
					} else if (itemBalise.indexOf(":") != -1 && itemBalise.substring(0, itemBalise.indexOf(":")).trim().equals("mail")) {
						email = itemBalise.substring(itemBalise.indexOf(":") + 1, itemBalise.length()).trim();
					}

				}

				result.setEmployeeID(employeeID);
				result.setsAMAccountName(sAMAccountName);
				result.setDisplayName(displayName);
				result.setTelephoneNumber(telephoneNumber);
				result.setEmail(email);

			} else {
				result.setLogError("Invalid username or password");
			}
		} catch (CommunicationException e) {
			result.setLogError("AD connection problem");
		} catch (NamingException e) {
			result.setLogError("Invalid username or password");
		} catch (Exception e) {
			result.setLogError(e.getMessage());
		} finally {
			try {
				if (ctx != null) {
					ctx.close();
				}
			} catch (NamingException e) {
				result.setLogError(e.getMessage());
			}
		}

		return result;

	}
/*
	public static void main(String[] args) {
		try {
			SessionLdap connectSession = CommonLdap.connectSession("session", "password", "ldap://annuaire.attijaribank.tn",
					"bank-sud", "tn");
			System.out.println(connectSession.toString());
		} catch (Exception e) {

			e.printStackTrace();
		}

	}*/

}

