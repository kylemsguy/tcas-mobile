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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Question extends QAObject {
	private Map<Integer, Answer> answers = new TreeMap<Integer, Answer>();

	public Question(int id, String content) {
		super(id, content);
	}

	public Set<Integer> getAnswerIDs() {
		return answers.keySet();
	}

	public Collection<Answer> getAnswers() {
		return answers.values();
	}

	public Answer getAnswerByID(int id) {
		return answers.get(id);
	}

	public void addAnswer(Answer answer) {
		answers.put(answer.getId(), answer);
	}

}
