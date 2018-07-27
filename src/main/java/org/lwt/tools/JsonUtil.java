package org.lwt.tools;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class JsonUtil {
	/**
	 * �������ݵ�json�ַ���ΪMap����
	 * 
	 * 
	 * @param jsonStr	json�ַ���
	 * @return	Map,	��json�ַ��������õ���map����
	 */
	public static Map<String, Object> getMapFromJson(String jsonStr){
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<>();
		map = gson.fromJson(jsonStr, new com.google.gson.reflect.TypeToken<Map<String,Object>>(){}.getType());
		return map;
	}
	/**
	 * ��map����ת��Ϊһ��json�ַ���
	 * 
	 * @param map	��ת����map����
	 * @return		����map ��Ӧ��json�ַ���
	 */
	public static String getJsonFromMap(Map<String, Object> map) {
		Gson gson = new Gson();
		String jsonStr = gson.toJson(map);
		return jsonStr;
	}
}
