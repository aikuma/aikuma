/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Utils used for debugging 
 * 
 * @author Sangyeop Lee		<sangl1@student.unimelb.edu.au>
 *
 */
public class DebugUtils {

	/**
	 * Get all public fields' names and values for the class 'obj'
	 * @param obj	The given class
	 * @return	String of the obj's public fields used for debugging 
	 */
	public static String getAllPublicFieldValues(final Object obj) {
		Class<?> objClass = obj.getClass();
		Field[] fields = objClass.getFields();
		
		StringBuilder sb = new StringBuilder();
		sb.append(objClass.getName() + ": {");
		for(Field field : fields) {
			// IF not a constant
			if(!Modifier.isFinal(field.getModifiers())) {
				String name = field.getName();
				Object value = null;
				try {
					value = field.get(obj);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				sb.append(name + ":" + value + ", ");
			}
		}
		sb.append("}");
		return sb.toString();
	}

}
