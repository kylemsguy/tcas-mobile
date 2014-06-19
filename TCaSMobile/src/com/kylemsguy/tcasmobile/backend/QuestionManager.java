/*
	This is an API for Two Cans and String
	
    Copyright (C) 2014  Kyle Zhou <kylezhou2002@hotmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
 */
package com.kylemsguy.tcasparser;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class QuestionManager {
	private final String ASK_URL = SessionManager.BASE_URL + "apiw/qa/ask/";
	private final String QUESTION_URL = SessionManager.BASE_URL
			+ "apiw/qa/notifications/";

	private SessionManager session;
	private Map<Integer, Question> questionAns;

	public QuestionManager(SessionManager session) {
		this.session = session;
	}

	public void askQuestion(String question) throws Exception {
		String postQuestion = "text=" + URLEncoder.encode(question, "UTF-8");
		session.sendPost(ASK_URL, postQuestion);
	}

	public Map<Integer, Question> getQuestions() throws Exception {
		String rawData = session.getPageContent(QUESTION_URL);
		System.out.println(rawData);
		questionAns = QAObject.parseData(rawData);

		return questionAns;
	}

}
