package org.lwt.tools;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class JsonUtil {
	/**
	 * 解析传递的json字符串为Map对象
	 * 
	 * 
	 * @param jsonStr	json字符串
	 * @return	Map,	由json字符串解析得到的map对象
	 */
	public static Map<String, Object> getMapFromJson(String jsonStr){
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<>();
		map = gson.fromJson(jsonStr, new com.google.gson.reflect.TypeToken<Map<String,Object>>(){}.getType());
		return map;
	}
	/**
	 * 将map对象转换为一个json字符串
	 * 
	 * @param map	待转换的map对象
	 * @return		返回map 对应得json字符串
	 */
	public static String getJsonFromMap(Map<String, Object> map) {
		Gson gson = new Gson();
		String jsonStr = gson.toJson(map);
		return jsonStr;
	}
}
