package com.celatum.data;

public class IGCredentials {
	private String key;
	private String credentialString;
	private String name;
	public static final IGCredentials UK_Credentials = new IGCredentials("UK", "4a1a04fac44886e0811604368b983212d01ca4ce",
			"{\n  \"identifier\": \"zed077\",\n  \"password\": \"z#V!uGge!if-H4t\"\n}");
	public static final IGCredentials CH_Credentials = new IGCredentials("CH", "a13bcf2cff4789367dbd9a1a15ea01dec4a6273c",
			"{\n  \"identifier\": \"concombre\",\n  \"password\": \"skdA_22#Qnr32-u\"\n}");
	public static final IGCredentials CH_Demo_Credentials = new IGCredentials("CH-DEMO", "91805fc497adaf871265ab37f29100616e765acf",
			"{\n  \"identifier\": \"celatum\",\n  \"password\": \"Snowstorm24\"\n}");

	private IGCredentials(String name, String key, String credentialString) {
		this.key = key;
		this.name = name;
		this.credentialString = credentialString;
	}

	public String getKey() {
		return key;
	}

	public String getCredentialString() {
		return credentialString;
	}

	public String getName() {
		return name;
	}

}
