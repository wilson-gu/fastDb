package com.gyw.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/** 
 * Description: Unsafe Helper  <p>
 * Created  By: GuYiwei         <br>
 *          At: 2019年11月4日  		上午10:10:58  <p>
 * Modified By: GuYiwei         <br>
 *          At: 2019年11月4日  		上午10:10:58  <p>
 * @author GuYiwei (Yiwei.gu09@gmail.com)
 */
@SuppressWarnings("restriction")
public class UnsafeHelper {
	
	public static Unsafe unsafe;
	
	static {
		try {
			Field unsafeField = Unsafe.class.getDeclaredFields()[0];
			unsafeField.setAccessible(true);
			unsafe = (Unsafe) unsafeField.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @return the unsafe
	 */
	public static Unsafe getUnsafe() {
		return unsafe;
	}
}
