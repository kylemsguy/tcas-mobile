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
package com.kylemsguy.tcasmobile.backend;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class QAObject {
	private int id;
	private String content;

	public QAObject(int id, String content) {
		this.id = id;
		this.content = content;
	}

	public int getId() {
		return id;
	}

	public String getContent() {
		return content;
	}

	public static Map<Integer, Question> parseData(String data)
			throws NoSuchQuestionException {
		Map<Integer, Question> questions = new TreeMap<Integer, Question>();

		// regex objects
		Pattern qPattern = Pattern
				.compile("lsQ\\^i(\\d+)\\^b[01]\\^i1\\^s(.*?)\\^\\^");
		Pattern aPattern = Pattern
				.compile("lsA\\^i(\\d+)\\^i(\\d+)\\^b([01])\\^s(.*?)\\^\\^");

		Matcher qMatcher = qPattern.matcher(data);
		Matcher aMatcher = aPattern.matcher(data);

		while (qMatcher.find()) {
			// get data from regex
			int id = Integer.parseInt(qMatcher.group(1));
			String strQ = qMatcher.group(2).replaceAll("\\$n", "\n");
			Question q = new Question(id, strQ);

			// insert into map
			questions.put(id, q);
		}

		while (aMatcher.find()) {
			// get data from regex
			int qId = Integer.parseInt(aMatcher.group(2));
			int aId = Integer.parseInt(aMatcher.group(1));
			int intRead = Integer.parseInt(aMatcher.group(3));
			boolean read;
			// check if read
			if (intRead == 0) {
				read = false;
			} else {
				read = true;
			}
			String ans = aMatcher.group(4).replaceAll("\\$n", "\n");

			// get relevant Question object
			Question q = questions.get(qId);

			if (q == null) {
				throw new NoSuchQuestionException(
						"No such question found. Something has gone horribly wrong.");
			}

			// create answer object
			Answer a = new Answer(aId, ans, q, read);

			// add to answer object
			q.addAnswer(a);
		}

		return questions;
	}
}
