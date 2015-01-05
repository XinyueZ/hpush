package com.hpush.data;

/**
 * Request types or names
 *
 * @author Xinyue Zhao
 */
public enum FunctionType {
	Edit("edit");

	/**
	 * Name of function.
	 */
	private String mName;

	/**
	 * Constructor of {@link com.hpush.data.FunctionType}.
	 * @param name Name of function.
	 */
	FunctionType(String name) {
		mName = name;
	}

	/**
	 *
	 * @return Name of function.
	 */
	public String getName() {
		return mName;
	}

	public static FunctionType fromName(String name) {
		switch (name) {
		case "edit":
			return Edit;
		default:
			return null;
		}
	}
}
