package org.csstudio.dct.model.internal;

import java.util.UUID;

import org.csstudio.dct.model.IRecord;

/**
 * Factory that creates records.
 * 
 * @author Sven Wende
 * 
 */
public class RecordFactory {
	/**
	 * Creates a record. The record is equipped with all fields that are known
	 * for the type of record.
	 * 
	 * @param project
	 *            the project
	 * @param type
	 *            the record type
	 * @param name
	 *            the record name
	 * @param id
	 *            the id for the new record
	 * @return the record
	 */
	public static IRecord createRecord(Project project, String type, String name, UUID id) {
		assert project != null;
		assert type != null;
		assert id != null;

		IRecord base = project.getBaseRecord(type);

		if (base == null) {
			throw new IllegalArgumentException("Cannot create record of type " + type + ".");
		}

		Record result = new Record(name, type, id);
		result.setParentRecord(base);

		return result;
	}

}