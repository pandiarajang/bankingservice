package com.abcbank.bankingservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
	
	/***
	 * Utils
	 * @throws JsonProcessingException 
	 */
	
	public static String toJson(Object cust) throws JsonProcessingException {
		ObjectMapper mapper=new ObjectMapper();

            String json = mapper.writeValueAsString(cust);
            System.out.println("ResultingJSONstring = " + json);
            //System.out.println(json);

		return json;
	}

}
