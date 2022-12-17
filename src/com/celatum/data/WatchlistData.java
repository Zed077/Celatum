package com.celatum.data;

public class WatchlistData {
	private boolean defaultSystemWatchlist;
	private boolean editable;

	private String id;
	private String name;

	public WatchlistData(boolean defaultSystemWatchlist, boolean editable, String id, String name) {
		super();
		this.defaultSystemWatchlist = defaultSystemWatchlist;
		this.id = id;
		this.name = name;
		this.editable = editable;
	}

	/**
	 * This flag does not mean anything, do not use
	 * @return
	 */
	private boolean isDefaultSystemWatchlist() {
		return defaultSystemWatchlist;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isEditable() {
		return editable;
	}

	@Override
	public String toString() {
		return name + "(" + id + ")" + " editable=" + editable;
	}

}
