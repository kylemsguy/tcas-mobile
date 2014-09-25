package com.kylemsguy.tcasmobile.backend;

import java.util.Map;
import java.util.Scanner;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String username = "AppTester";
		String password = "TesterPassword";
		
		SessionManager sm = new SessionManager();
		AnswerManager am = new AnswerManager(sm);
		
		Scanner sc = new Scanner(System.in);
		
		try {
			sm.login(username, password);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Map<String, String> question = null;
		try {
			question = am.getQuestion();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while(true){
			try {
				System.out.println(question.get("content"));
				question = am.sendAnswer(question.get("id"), sc.nextLine());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
