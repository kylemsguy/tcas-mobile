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
