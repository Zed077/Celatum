package com.celatum.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;



public class Hello {
	public String helloName(String name) {
		try {
			String result = new ObjectMapper().writeValueAsString(new testJson());
			return result + " " + name;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
	}
}

class testJson {
	public String title = "TestJson";
}